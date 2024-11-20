package io.github.reoseah.magisterium.mixin.client;

import io.github.reoseah.magisterium.item.SpellBookItem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.model.BookModel;
import net.minecraft.client.render.entity.model.EntityModelLayers;
import net.minecraft.client.render.item.BuiltinModelItemRenderer;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ModelTransformationMode;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(BuiltinModelItemRenderer.class)
public class BuiltinItemModelRendererMixin {
    @Unique
    private static final Identifier TEXTURE = Identifier.of("magisterium", "textures/entity/spell_book.png");

    @Unique
    private BookModel bookModel;

    @Inject(at = @At("HEAD"), method = "reload")
    public void reload(ResourceManager manager, CallbackInfo ci) {
        this.bookModel = new BookModel(MinecraftClient.getInstance().getEntityModelLoader().getModelPart(EntityModelLayers.BOOK));
    }

    @Inject(at = @At("HEAD"), method = "render", cancellable = true)
    public void render(ItemStack stack, ModelTransformationMode mode, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, int overlay, CallbackInfo ci) {
        Item item = stack.getItem();
        if (item == SpellBookItem.SPELL_BOOK) {
            matrices.push();
            matrices.scale(1, -1, -1);
            VertexConsumer vertexConsumer = ItemRenderer.getItemGlintConsumer(vertexConsumers, this.bookModel.getLayer(TEXTURE), true, stack.hasGlint());
            this.bookModel.setPageAngles(0, 0.1F, 0.9F, 1.2F);
            this.bookModel.render(matrices, vertexConsumer, light, overlay, 0xFFFFFF);
            matrices.pop();
            ci.cancel();
        }
    }
}
