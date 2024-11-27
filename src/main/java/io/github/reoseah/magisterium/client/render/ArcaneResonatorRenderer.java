package io.github.reoseah.magisterium.client.render;

import io.github.reoseah.magisterium.block.ArcaneResonatorBlock;
import io.github.reoseah.magisterium.block.entity.ArcaneResonatorBlockEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.RenderLayers;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.BlockRenderManager;
import net.minecraft.client.render.block.entity.BlockEntityRenderer;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RotationAxis;
import net.minecraft.util.math.random.Random;

public class ArcaneResonatorRenderer implements BlockEntityRenderer<ArcaneResonatorBlockEntity> {
    public static final Identifier CRYSTAL = Identifier.of("magisterium:block/arcane_resonator_crystal");
    public static final Identifier CRYSTAL_ON = Identifier.of("magisterium:block/arcane_resonator_crystal_on");

    private final BlockRenderManager renderManager;

    public ArcaneResonatorRenderer(BlockEntityRendererFactory.Context ctx) {
        this.renderManager = ctx.getRenderManager();
    }

    @Override
    public void render(ArcaneResonatorBlockEntity entity, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, int overlay) {
        boolean powered = entity.getCachedState().get(ArcaneResonatorBlock.POWERED);
        var model = MinecraftClient.getInstance().getBakedModelManager().getModel(powered ? CRYSTAL_ON : CRYSTAL);
        var world = entity.getWorld();
        var pos = entity.getPos();
        matrices.push();
        matrices.translate(.5F, MathHelper.sin((world.getTime() + tickDelta) / 20) * .1F, .5F);
        matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(world.getTime() + tickDelta));
        matrices.translate(-.5F, 0, -.5F);
        this.renderManager.getModelRenderer().render(world, model, entity.getCachedState(), pos, matrices, vertexConsumers.getBuffer(RenderLayers.getMovingBlockLayer(entity.getCachedState())), false, Random.create(), 1, OverlayTexture.DEFAULT_UV);
        matrices.pop();
    }
}
