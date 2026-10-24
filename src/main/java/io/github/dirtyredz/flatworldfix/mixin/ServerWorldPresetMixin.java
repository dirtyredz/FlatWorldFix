package io.github.dirtyredz.flatworldfix.mixin;

import io.github.dirtyredz.flatworldfix.world.SelectedOverworld;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.levelgen.WorldDimensions;
import net.minecraft.world.level.levelgen.presets.WorldPreset;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(targets = "net.minecraft.server.dedicated.DedicatedServerProperties$WorldDimensionData")
abstract class ServerWorldPresetMixin {
    @Redirect(method = "create", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/level/levelgen/presets/WorldPreset;createWorldDimensions()Lnet/minecraft/world/level/levelgen/WorldDimensions;"))
    private WorldDimensions flatworldfix$rememberResolvedPreset(WorldPreset preset, RegistryAccess access) {
        String id = access.registryOrThrow(Registries.WORLD_PRESET).getResourceKey(preset)
                .map(key -> key.location().toString()).orElse(null);
        return SelectedOverworld.select(preset.createWorldDimensions(), id);
    }
}
