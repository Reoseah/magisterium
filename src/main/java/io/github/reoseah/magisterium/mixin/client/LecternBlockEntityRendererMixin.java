package io.github.reoseah.magisterium.mixin.client;


import io.github.reoseah.magisterium.block.MagisteriumProperties;
import net.minecraft.block.LecternBlock;
import net.minecraft.block.entity.LecternBlockEntity;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.entity.LecternBlockEntityRenderer;
import net.minecraft.client.render.entity.model.BookModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.RotationAxis;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LecternBlockEntityRenderer.class)
public class LecternBlockEntityRendererMixin {
    @Unique
    private static final Identifier TEXTURE = Identifier.of("magisterium", "textures/entity/spell_book.png");

    @Shadow
    private @Final BookModel book;

    @Inject(at = @At("HEAD"), cancellable = true, method = "render")
    public void render(LecternBlockEntity lectern, float partialTicks, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, int overlay, CallbackInfo ci) {
        if (lectern.getCachedState().get(MagisteriumProperties.HOLDS_SPELL_BOOK)) {
            matrices.push();
            matrices.translate(0.5F, 1.0625F, 0.5F);
            float rotation = lectern.getCachedState().get(LecternBlock.FACING).rotateYClockwise().asRotation();
            matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(-rotation));
            matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(67.5F));
            matrices.translate(0, -0.125F, 0);
            this.book.setPageAngles(0, 0.1F, 0.9F, 1.2F);
            VertexConsumer vertexConsumer = vertexConsumers.getBuffer(this.book.getLayer(TEXTURE));
            this.book.renderBook(matrices, vertexConsumer, light, overlay, 0xFFFFFFFF);
            matrices.pop();

            ci.cancel();
        }
    }
}
