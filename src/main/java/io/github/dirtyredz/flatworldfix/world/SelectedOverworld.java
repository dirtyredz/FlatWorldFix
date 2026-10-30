package io.github.dirtyredz.flatworldfix.world;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.MappedRegistry;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.dimension.LevelStem;
import net.minecraft.world.level.levelgen.WorldDimensions;

import java.util.function.BiFunction;

/** Creation intent belongs to a world, and survives saving without opting in pre-existing saves. */
public final class SelectedOverworld {
    public static final String MARKER = "flatworldfix:keep_overworld";

    // WorldDimensions is a record in 1.21, so equals() compares its map. Identity is essential:
    // two different worlds with equal settings must not share creation intent.
    private static final IdentityWeakSet<WorldDimensions> SELECTED_DIMENSIONS = new IdentityWeakSet<>();
    private static final IdentityWeakSet<Registry<LevelStem>> SELECTED_REGISTRIES = new IdentityWeakSet<>();

    private SelectedOverworld() {}

    /** The ID must come from a resolved registry holder, never raw server.properties text. */
    public static boolean isNonDefault(String resolvedPresetId) {
        return resolvedPresetId != null && !resolvedPresetId.equals("minecraft:normal");
    }

    public static WorldDimensions select(WorldDimensions dimensions, String resolvedPresetId) {
        return restore(dimensions, isNonDefault(resolvedPresetId));
    }

    private static WorldDimensions restore(WorldDimensions dimensions, boolean keep) {
        SELECTED_DIMENSIONS.set(dimensions, keep);
        return dimensions;
    }

    public static void inherit(WorldDimensions source, WorldDimensions result) {
        if (SELECTED_DIMENSIONS.contains(source)) SELECTED_DIMENSIONS.add(result);
    }

    public static void inherit(WorldDimensions source, Registry<LevelStem> result) {
        if (SELECTED_DIMENSIONS.contains(source)) SELECTED_REGISTRIES.add(result);
    }

    /** Carries intent through the saving path's new WorldDimensions(bakedRegistry) copy. */
    public static void inherit(Registry<LevelStem> source, WorldDimensions result) {
        if (SELECTED_REGISTRIES.contains(source)) SELECTED_DIMENSIONS.add(result);
    }

    public static MapCodec<WorldDimensions> withMarker(MapCodec<WorldDimensions> original) {
        return RecordCodecBuilder.<WorldDimensions>mapCodec(instance -> instance.group(
                original.forGetter(dimensions -> dimensions),
                Codec.BOOL.optionalFieldOf(MARKER, false)
                        .forGetter(SELECTED_DIMENSIONS::contains)
        ).apply(instance, instance.stable(
                (BiFunction<WorldDimensions, Boolean, WorldDimensions>) SelectedOverworld::restore)));
    }

    public static Registry<LevelStem> datapackFor(WorldDimensions selected, Registry<LevelStem> datapack) {
        if (!SELECTED_DIMENSIONS.contains(selected) || !datapack.containsKey(LevelStem.OVERWORLD)) {
            return datapack;
        }
        var filtered = new MappedRegistry<LevelStem>(Registries.LEVEL_STEM, datapack.registryLifecycle());
        for (var entry : datapack.entrySet()) {
            if (!entry.getKey().equals(LevelStem.OVERWORLD)) {
                filtered.register(entry.getKey(), entry.getValue(),
                        datapack.registrationInfo(entry.getKey()).orElseThrow());
            }
        }
        // Let vanilla bake supply the selected overworld and perform all ordering/stability checks.
        return filtered.freeze();
    }
}
