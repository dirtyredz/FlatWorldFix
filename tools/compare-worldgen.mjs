import assert from 'node:assert/strict';
import fs from 'node:fs';
import { get, plain, readGzip } from './nbt.mjs';

const [before, after] = process.argv.slice(2);
assert(before && after, 'Usage: node tools/compare-worldgen.mjs <before-level.dat> <after-level.dat>');
const worldgen = file => plain(get(readGzip(fs.readFileSync(file)).root, 'Data', 'WorldGenSettings'));
assert.deepEqual(worldgen(after), worldgen(before), 'saved WorldGenSettings must remain unchanged');
console.log('PASS saved WorldGenSettings unchanged');
