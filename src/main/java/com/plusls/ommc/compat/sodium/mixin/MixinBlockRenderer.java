package com.plusls.ommc.compat.sodium.mixin;

import com.plusls.ommc.compat.Dependencies;
import com.plusls.ommc.compat.Dependency;
import com.plusls.ommc.feature.blockModelNoOffset.BlockModelNoOffsetUtil;
import com.plusls.ommc.feature.worldEaterMineHelper.BlockModelRendererContext;
import com.plusls.ommc.feature.worldEaterMineHelper.WorldEaterMineHelperUtil;
import net.minecraft.block.BlockState;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.BlockRenderView;
import net.minecraft.world.BlockView;
import org.spongepowered.asm.mixin.Dynamic;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Dependencies(dependencyList = @Dependency(modId = "sodium", version = "*"))
@Pseudo
@Mixin(targets = "me.jellysquid.mods.sodium.client.render.pipeline.BlockRenderer", remap = false)
public class MixinBlockRenderer {
    private final ThreadLocal<BlockModelRendererContext> ommcRenderContext = ThreadLocal.withInitial(BlockModelRendererContext::new);
    private final ThreadLocal<Integer> ommcOriginalLuminance = ThreadLocal.withInitial(() -> -1);

    @SuppressWarnings("UnresolvedMixinReference")
    @Inject(method = "renderModel", at = @At(value = "HEAD"))
    private void initRenderContext(BlockRenderView world, BlockState state, BlockPos pos, BlockPos origin, BakedModel model, @Coerce Object buffers, boolean cull, long seed, CallbackInfoReturnable<Boolean> cir) {
        BlockModelRendererContext context = ommcRenderContext.get();
        context.pos = pos;
        context.state = state;
    }

    @Dynamic
    @Redirect(method = "renderModel", at = @At(value = "INVOKE", target = "Lnet/minecraft/block/BlockState;getModelOffset(Lnet/minecraft/world/BlockView;Lnet/minecraft/util/math/BlockPos;)Lnet/minecraft/util/math/Vec3d;", ordinal = 0, remap = true))
    private Vec3d blockModelNoOffset(BlockState blockState, BlockView world, BlockPos pos) {
        return BlockModelNoOffsetUtil.blockModelNoOffset(blockState, world, pos);
    }

    @SuppressWarnings("UnresolvedMixinReference")
    @ModifyVariable(method = "renderModel", at = @At(value = "HEAD"), ordinal = 0)
    private BakedModel modifyBakedModel(BakedModel bakedModel) {
        BlockModelRendererContext context = ommcRenderContext.get();
        if (WorldEaterMineHelperUtil.shouldUseCustomModel(context.state, context.pos)) {
            BakedModel customModel = WorldEaterMineHelperUtil.customFullModels.get(context.state.getBlock());
            if (customModel != null) {
                ommcOriginalLuminance.set(context.state.luminance);
                context.state.luminance = 15;
                return customModel;
            }
        }
        return bakedModel;
    }

    @SuppressWarnings("UnresolvedMixinReference")
    @Inject(method = "renderModel", at = @At(value = "RETURN"))
    private void postRenderModel(BlockRenderView world, BlockState state, BlockPos pos, BlockPos origin, BakedModel model, @Coerce Object buffers, boolean cull, long seed, CallbackInfoReturnable<Boolean> cir) {
        int originalLuminance = ommcOriginalLuminance.get();
        if (originalLuminance != -1) {
            state.luminance = originalLuminance;
            ommcOriginalLuminance.set(-1);
        }
    }
}
