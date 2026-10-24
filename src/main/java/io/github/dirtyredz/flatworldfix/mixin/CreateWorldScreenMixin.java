package io.github.dirtyredz.flatworldfix.mixin;

import io.github.dirtyredz.flatworldfix.world.SelectedOverworld;
import net.minecraft.client.gui.screens.worldselection.CreateWorldScreen;
import net.minecraft.core.Registry;
import net.minecraft.world.level.dimension.LevelStem;
import net.minecraft.world.level.levelgen.WorldDimensions;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(CreateWorldScreen.class)
abstract class CreateWorldScreenMixin {
    @Redirect(method = "onCreate", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/level/levelgen/WorldDimensions;bake(Lnet/minecraft/core/Registry;)Lnet/minecraft/world/level/levelgen/WorldDimensions$Complete;"))
    private WorldDimensions.Complete flatworldfix$rememberSelectedPreset(
            WorldDimensions selected, Registry<LevelStem> datapack) {
        var preset = ((CreateWorldScreen) (Object) this).getUiState().getWorldType().preset();
        String id = preset == null ? null : preset.unwrapKey().map(key -> key.location().toString()).orElse(null);
        return SelectedOverworld.select(selected, id).bake(datapack);
    }
}
