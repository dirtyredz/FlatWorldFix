package io.github.dirtyredz.flatworldfix.world;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SelectedOverworldTest {
    @Test
    void normalAndUnidentifiedSelectionsKeepDatapackPrecedence() {
        assertFalse(SelectedOverworld.isNonDefault("minecraft:normal"));
        assertFalse(SelectedOverworld.isNonDefault(null));
    }

    @Test
    void everyResolvedNonDefaultPresetKeepsItsOverworld() {
        for (String id : List.of("minecraft:flat", "minecraft:single_biome_surface",
                "minecraft:debug_all_block_states", "minecraft:amplified", "minecraft:large_biomes",
                "example:custom_world")) {
            assertTrue(SelectedOverworld.isNonDefault(id), id);
        }
    }
}
