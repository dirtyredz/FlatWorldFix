param(
    [Parameter(Mandatory)][ValidatePattern('^[a-z][a-z0-9-]*$')][string]$Name,
    [string]$WorldType = 'flat',
    [string]$TerralithJar
)
$ErrorActionPreference = 'Stop'
$repo = (Resolve-Path (Join-Path $PSScriptRoot '..')).Path
Set-Location -LiteralPath $repo
if ($TerralithJar -and !(Test-Path -LiteralPath $TerralithJar -PathType Leaf)) {
    throw "Missing local input: $TerralithJar"
}
$deadline = (Get-Date).AddMinutes(45)
do {
    $busy = @(Get-CimInstance Win32_Process | Where-Object {
        # Include @argument-file launches, whose command line can hide the main class.
        $_.Name -match '^javaw?\.exe$'
    })
    if (!$busy.Count) { break }
    if ((Get-Date) -ge $deadline) { throw 'Build/server slot occupied for 45 minutes; proof not run' }
    Write-Output "Waiting for machine slot: Java PIDs $($busy.ProcessId -join ', ')"
    Start-Sleep -Seconds 60
} while ($true)
if ([Net.NetworkInformation.IPGlobalProperties]::GetIPGlobalProperties().GetActiveTcpListeners() |
        Where-Object Port -eq 9) {
    throw 'Local port 9 is occupied; cannot use it as the disconnected proof HTTP proxy'
}

$process = [Diagnostics.Process]::GetCurrentProcess()
$oldPriority = $process.PriorityClass
try {
    $process.PriorityClass = 'BelowNormal'
    $receipt = Join-Path $repo "run/$Name/proof-result.txt"
    if (Test-Path -LiteralPath $receipt) { Remove-Item -LiteralPath $receipt }
    New-Item -ItemType Directory -Force build/inspection | Out-Null
    $gradleArgs = @('runServer', "-Pproof=$Name", "-PworldType=$WorldType")
    if ($TerralithJar) { $gradleArgs += "-PterralithJar=$TerralithJar" }
    # Windows PowerShell treats native stderr as an error record even for harmless JVM messages.
    $ErrorActionPreference = 'Continue'
    & ./gradlew @gradleArgs 2>&1 | ForEach-Object { $_.ToString() } |
        Tee-Object -FilePath "build/inspection/$Name.log"
    $gradleExit = $LASTEXITCODE
    $ErrorActionPreference = 'Stop'
    if ($gradleExit -ne 0) { throw "Gradle failed with exit code $gradleExit" }
    if (!(Test-Path -LiteralPath $receipt) -or (Get-Content -Raw -LiteralPath $receipt).Trim() -ne 'PASS') {
        throw "Proof failed; inspect run/$Name/proof-result.txt and build/inspection/$Name.log"
    }
} finally {
    $process.PriorityClass = $oldPriority
}
