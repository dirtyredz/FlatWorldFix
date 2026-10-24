param([string]$Jar = 'build/libs/flatworldfix-1.20.1-1.0.0.jar')
$ErrorActionPreference = 'Stop'
Add-Type -AssemblyName System.IO.Compression.FileSystem
$archive = [IO.Compression.ZipFile]::OpenRead((Resolve-Path -LiteralPath $Jar))
try {
    $names = @($archive.Entries | ForEach-Object FullName)
    foreach ($required in @('META-INF/mods.toml', 'META-INF/LICENSE', 'META-INF/MANIFEST.MF',
        'flatworldfix.mixins.json', 'flatworldfix.refmap.json')) {
        if ($names -notcontains $required) { throw "Jar missing $required" }
    }
    if ($names | Where-Object { $_ -match 'WorldCreationProof|MergeProof|Test\.class|org/junit|terralith' }) {
        throw 'Test classes or dependencies leaked into release jar'
    }
    foreach ($name in @('META-INF/mods.toml', 'META-INF/MANIFEST.MF',
        'flatworldfix.mixins.json', 'flatworldfix.refmap.json')) {
        $reader = [IO.StreamReader]::new($archive.GetEntry($name).Open())
        try { $content = $reader.ReadToEnd() } finally { $reader.Dispose() }
        if ($name -eq 'META-INF/mods.toml' -and ($content.Contains('${') -or !$content.Contains('version="1.0.0"'))) {
            throw 'Unexpanded or wrong mod metadata'
        }
        if ($name -eq 'META-INF/MANIFEST.MF' -and !$content.Contains('MixinConfigs: flatworldfix.mixins.json')) {
            throw 'Missing manifest mixin activation'
        }
        if ($name -eq 'flatworldfix.refmap.json' -and !$content.Contains('m_')) {
            throw 'Missing production SRG mappings'
        }
        Write-Output "$name`n$content"
    }
} finally { $archive.Dispose() }
Get-FileHash -LiteralPath $Jar -Algorithm SHA256
Write-Output 'PASS release metadata, mixins, SRG refmap, and test isolation'
