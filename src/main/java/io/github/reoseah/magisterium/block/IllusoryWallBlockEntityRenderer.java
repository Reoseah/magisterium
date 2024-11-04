package io.github.reoseah.magisterium.block;

import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.RenderLayers;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.BlockRenderManager;
import net.minecraft.client.render.block.entity.BlockEntityRenderer;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;

public class IllusoryWallBlockEntityRenderer implements BlockEntityRenderer<IllusoryWallBlockEntity> {
    private final BlockRenderManager blockRenderManager;

    public IllusoryWallBlockEntityRenderer(BlockEntityRendererFactory.Context ctx) {
        this.blockRenderManager = ctx.getRenderManager();
    }

    @Override
    public void render(IllusoryWallBlockEntity entity, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, int overlay) {
        var state = entity.getIllusoryState();
        if (state.getRenderType() == BlockRenderType.MODEL) {
            var world = entity.getWorld();
            var pos = entity.getPos();
            this.blockRenderManager.getModelRenderer().render(world, this.blockRenderManager.getModel(state), state, pos, matrices, vertexConsumers.getBuffer(RenderLayers.getMovingBlockLayer(state)), false, Random.create(), 1, OverlayTexture.DEFAULT_UV);
        }
    }
}
