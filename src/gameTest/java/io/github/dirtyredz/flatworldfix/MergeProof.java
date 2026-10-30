package io.github.dirtyredz.flatworldfix;

import com.google.gson.JsonParser;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.Lifecycle;
import net.minecraft.core.MappedRegistry;
import net.minecraft.core.RegistrationInfo;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.NbtOps;
import net.minecraft.resources.RegistryOps;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.dedicated.DedicatedServerProperties;
import net.minecraft.world.level.dimension.LevelStem;
import net.minecraft.world.level.levelgen.WorldDimensions;
import net.minecraft.world.level.levelgen.WorldGenSettings;
import net.minecraft.world.level.levelgen.WorldOptions;
import net.minecraft.world.level.levelgen.presets.WorldPresets;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.Properties;

/** Small real-registry regression matrix, executed inside the named dedicated-server proof. */
final class MergeProof {
    private static final ResourceKey<LevelStem> EXTRA = ResourceKey.create(
            Registries.LEVEL_STEM, ResourceLocation.fromNamespaceAndPath("flatworldfix", "proof_extra"));

    static void run(MinecraftServer server) throws IOException {
        var access = server.registryAccess();
        LevelStem terralith;
        try (var reader = server.getResourceManager().openAsReader(
                ResourceLocation.fromNamespaceAndPath("minecraft", "dimension/overworld.json"))) {
            terralith = LevelStem.CODEC.parse(RegistryOps.create(JsonOps.INSTANCE, access),
                    JsonParser.parseReader(reader)).getOrThrow(AssertionError::new);
        }
        var input = new MappedRegistry<LevelStem>(Registries.LEVEL_STEM, Lifecycle.stable());
        var serverDimensions = access.registryOrThrow(Registries.LEVEL_STEM);
        for (var entry : serverDimensions.entrySet()) {
            // Different objects expose accidental substitution of selected Nether/End entries.
            var stem = entry.getValue();
            input.register(entry.getKey(), entry.getKey().equals(LevelStem.OVERWORLD) ? terralith
                    : new LevelStem(stem.type(), stem.generator()),
                    new RegistrationInfo(Optional.empty(), Lifecycle.stable()));
        }
        var noise = (net.minecraft.world.level.levelgen.NoiseBasedChunkGenerator) terralith.generator();
        input.register(EXTRA, new LevelStem(terralith.type(),
                new net.minecraft.world.level.levelgen.NoiseBasedChunkGenerator(
                        noise.getBiomeSource(), noise.generatorSettings())),
                new RegistrationInfo(Optional.empty(), Lifecycle.experimental()));
        Registry<LevelStem> datapack = input.freeze();
        var ops = RegistryOps.create(NbtOps.INSTANCE, access);

        for (String type : List.of("flat", "single_biome_surface", "debug_all_block_states",
                "amplified", "large_biomes", "normal", "default", "unknown_preset")) {
            var properties = new Properties();
            properties.setProperty("level-type", type);
            properties.setProperty("generator-settings", "{\"biome\":\"minecraft:plains\","
                    + "\"layers\":[{\"block\":\"minecraft:bedrock\",\"height\":1},"
                    + "{\"block\":\"minecraft:gold_block\",\"height\":7}]}");
            var selected = new DedicatedServerProperties(properties).createDimensions(access);
            if (type.equals("flat")) {
                var layers = ((net.minecraft.world.level.levelgen.FlatLevelSource) selected.overworld())
                        .settings().getLayersInfo();
                require(layers.get(1).getHeight() == 7 && layers.get(1).getBlockState()
                        .is(net.minecraft.world.level.block.Blocks.GOLD_BLOCK), "custom flat layers parsed");
            }
            boolean protect = !List.of("normal", "default", "unknown_preset").contains(type);
            var baked = selected.bake(datapack);
            var expected = protect ? selected.dimensions().get(LevelStem.OVERWORLD) : terralith;
            require(baked.dimensions().get(LevelStem.OVERWORLD) == expected, type + " selected overworld");
            for (var key : List.of(LevelStem.NETHER, LevelStem.END, EXTRA)) {
                require(baked.dimensions().get(key) == datapack.get(key), type + " preserves " + key);
            }
            require(datapack.get(LevelStem.OVERWORLD) == terralith, "input registry is not mutated");
            // This is how saving reconstructs WorldDimensions from the final registry.
            var saved = WorldGenSettings.encode(ops, new WorldOptions(0, false, false),
                    baked.dimensionsRegistryAccess()).getOrThrow(AssertionError::new);
            var tag = (net.minecraft.nbt.CompoundTag) saved;
            require(tag.getBoolean("flatworldfix:keep_overworld") == protect, type + " saved opt-in");
            var loaded = WorldGenSettings.CODEC.parse(ops, saved).getOrThrow(AssertionError::new);
            var reopened = loaded.dimensions().bake(datapack);
            require(protect ? reopened.dimensions().get(LevelStem.OVERWORLD)
                    == loaded.dimensions().dimensions().get(LevelStem.OVERWORLD)
                    : reopened.dimensions().get(LevelStem.OVERWORLD) == terralith, type + " reload precedence");
            System.out.println("FLATWORLDFIX_MATRIX PASS " + type);
        }

        // Old saves and other callers never opted in: even an old flat generator stays vanilla.
        var unmarked = access.registryOrThrow(Registries.WORLD_PRESET)
                .getHolderOrThrow(WorldPresets.FLAT).value().createWorldDimensions();
        require(unmarked.bake(datapack).dimensions().get(LevelStem.OVERWORLD) == terralith,
                "unmarked existing flat world retains vanilla merge");
        var legacySaved = WorldGenSettings.encode(ops, new WorldOptions(0, false, false), unmarked)
                .getOrThrow(AssertionError::new);
        ((net.minecraft.nbt.CompoundTag) legacySaved).remove("flatworldfix:keep_overworld");
        var legacyLoaded = WorldGenSettings.CODEC.parse(ops, legacySaved).getOrThrow(AssertionError::new);
        require(legacyLoaded.dimensions().bake(datapack).dimensions().get(LevelStem.OVERWORLD) == terralith,
                "pre-mod saved data without the optional marker retains vanilla merge");
        var empty = new MappedRegistry<LevelStem>(Registries.LEVEL_STEM, Lifecycle.stable()).freeze();
        require(unmarked.bake(empty).dimensions().get(LevelStem.OVERWORLD)
                == unmarked.dimensions().get(LevelStem.OVERWORLD), "absent datapack overworld falls back");
        System.out.println("FLATWORLDFIX_MATRIX PASS unmarked-save and absent-override");
    }

    private static void require(boolean value, String description) {
        if (!value) fail(description);
    }

    private static void fail(String description) {
        throw new AssertionError(description);
    }
}
