package io.github.dirtyredz.flatworldfix.mixin;

import com.mojang.datafixers.kinds.App;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.dirtyredz.flatworldfix.world.SelectedOverworld;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.dimension.LevelStem;
import net.minecraft.world.level.levelgen.WorldDimensions;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.function.Function;

@Mixin(WorldDimensions.class)
abstract class WorldDimensionsMixin {
    @Redirect(method = "<clinit>", at = @At(value = "INVOKE", remap = false,
            target = "Lcom/mojang/serialization/codecs/RecordCodecBuilder;mapCodec(Ljava/util/function/Function;)Lcom/mojang/serialization/MapCodec;"))
    private static MapCodec<WorldDimensions> flatworldfix$persistIntent(
            Function<RecordCodecBuilder.Instance<WorldDimensions>,
                    ? extends App<RecordCodecBuilder.Mu<WorldDimensions>, WorldDimensions>> factory) {
        return SelectedOverworld.withMarker(RecordCodecBuilder.mapCodec(factory));
    }

    @ModifyVariable(method = "bake", at = @At("HEAD"), argsOnly = true, ordinal = 0)
    private Registry<LevelStem> flatworldfix$keepSelectedOverworld(Registry<LevelStem> datapack) {
        return SelectedOverworld.datapackFor((WorldDimensions) (Object) this, datapack);
    }

    @Inject(method = "bake", at = @At("RETURN"))
    private void flatworldfix$rememberBakedRegistry(Registry<LevelStem> datapack,
            CallbackInfoReturnable<WorldDimensions.Complete> callback) {
        SelectedOverworld.inherit(((WorldDimensions) (Object) this).dimensions(),
                callback.getReturnValue().dimensions());
    }

    @Inject(method = "replaceOverworldGenerator", at = @At("RETURN"))
    private void flatworldfix$keepIntentAfterCustomization(RegistryAccess access, ChunkGenerator generator,
            CallbackInfoReturnable<WorldDimensions> callback) {
        SelectedOverworld.inherit(((WorldDimensions) (Object) this).dimensions(),
                callback.getReturnValue().dimensions());
    }
}
