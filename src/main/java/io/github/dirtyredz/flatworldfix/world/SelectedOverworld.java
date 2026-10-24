package io.github.dirtyredz.flatworldfix.world;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.MappedRegistry;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.dimension.LevelStem;
import net.minecraft.world.level.levelgen.WorldDimensions;

import java.util.Collections;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.function.BiFunction;

/** Creation intent belongs to a world, and survives saving without opting in pre-existing saves. */
public final class SelectedOverworld {
    public static final String MARKER = "flatworldfix:keep_overworld";

    // MappedRegistry uses object identity. Weak keys do not retain abandoned creation screens/worlds.
    private static final Set<Registry<LevelStem>> SELECTED =
            Collections.synchronizedSet(Collections.newSetFromMap(new WeakHashMap<>()));

    private SelectedOverworld() {}

    /** The ID must come from a resolved registry holder, never raw server.properties text. */
    public static boolean isNonDefault(String resolvedPresetId) {
        return resolvedPresetId != null && !resolvedPresetId.equals("minecraft:normal");
    }

    public static WorldDimensions select(WorldDimensions dimensions, String resolvedPresetId) {
        return restore(dimensions, isNonDefault(resolvedPresetId));
    }

    private static WorldDimensions restore(WorldDimensions dimensions, boolean keep) {
        if (keep) SELECTED.add(dimensions.dimensions());
        else SELECTED.remove(dimensions.dimensions());
        return dimensions;
    }

    public static void inherit(Registry<LevelStem> source, Registry<LevelStem> result) {
        if (SELECTED.contains(source)) SELECTED.add(result);
    }

    public static MapCodec<WorldDimensions> withMarker(MapCodec<WorldDimensions> original) {
        return RecordCodecBuilder.<WorldDimensions>mapCodec(instance -> instance.group(
                original.forGetter(dimensions -> dimensions),
                Codec.BOOL.optionalFieldOf(MARKER, false)
                        .forGetter(dimensions -> SELECTED.contains(dimensions.dimensions()))
        ).apply(instance, instance.stable(
                (BiFunction<WorldDimensions, Boolean, WorldDimensions>) SelectedOverworld::restore)));
    }

    public static Registry<LevelStem> datapackFor(WorldDimensions selected, Registry<LevelStem> datapack) {
        if (!SELECTED.contains(selected.dimensions()) || !datapack.containsKey(LevelStem.OVERWORLD)) {
            return datapack;
        }
        var filtered = new MappedRegistry<LevelStem>(Registries.LEVEL_STEM, datapack.registryLifecycle());
        for (var entry : datapack.entrySet()) {
            if (!entry.getKey().equals(LevelStem.OVERWORLD)) {
                filtered.register(entry.getKey(), entry.getValue(), datapack.lifecycle(entry.getValue()));
            }
        }
        // Let vanilla bake supply the selected overworld and perform all ordering/stability checks.
        return filtered.freeze();
    }
}
