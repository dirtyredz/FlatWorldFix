import assert from 'node:assert/strict';
import fs from 'node:fs';
import { get, readGzip, plain } from './nbt.mjs';

const [file, expected, terralithFile] = process.argv.slice(2);
assert(file && ['flat', 'noise', 'debug'].includes(expected),
  'Usage: node tools/verify-world.mjs <level.dat> <flat|noise|debug> [terralith-overworld.json]');
const nbt = readGzip(fs.readFileSync(file));
const settings = plain(get(nbt.root, 'Data', 'WorldGenSettings'));
const dimensions = settings.dimensions;
const generator = dimensions['minecraft:overworld'].generator;
assert.equal(generator.type, `minecraft:${expected}`, 'saved overworld generator');
assert(dimensions['minecraft:the_nether'], 'Nether survives');
assert(dimensions['minecraft:the_end'], 'End survives');
if (expected === 'flat') {
  assert.equal(get(nbt.root, 'Data', 'WorldGenSettings', 'flatworldfix:keep_overworld')?.v, 1,
    'flat selection must survive saving');
  assert.deepEqual(generator.settings.layers, [
    { block: 'minecraft:bedrock', height: 1 },
    { block: 'minecraft:dirt', height: 2 },
    { block: 'minecraft:grass_block', height: 1 },
  ]);
  assert.equal(generator.settings.biome, 'minecraft:plains');
}
if (terralithFile) {
  assert.notEqual(get(nbt.root, 'Data', 'WorldGenSettings', 'flatworldfix:keep_overworld')?.v, 1,
    'default/control worlds must not opt in');
  const terralith = JSON.parse(fs.readFileSync(terralithFile, 'utf8'));
  assert.equal(generator.biome_source.type, terralith.generator.biome_source.type);
  assert.deepEqual(generator.biome_source.biomes.map(entry => entry.biome),
    terralith.generator.biome_source.biomes.map(entry => entry.biome),
    'default/control ordered biome list must still be Terralith, not just any noise generator');
  assert.equal(generator.settings, terralith.generator.settings);
}
console.log(JSON.stringify({ file, generator: generator.type,
  biomes: generator.biome_source?.biomes?.length,
  marker: get(nbt.root, 'Data', 'WorldGenSettings', 'flatworldfix:keep_overworld')?.v === 1,
  dimensions: Object.keys(dimensions), result: 'PASS' }, null, 2));
