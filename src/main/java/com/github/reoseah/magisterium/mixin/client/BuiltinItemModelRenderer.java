package com.github.reoseah.magisterium.mixin.client;

import java.util.ArrayList;
import java.util.HashMap;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.github.reoseah.magisterium.Magisterium;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.entity.SkullBlockEntityRenderer;
import net.minecraft.client.render.entity.model.BookModel;
import net.minecraft.client.render.entity.model.EntityModelLayers;
import net.minecraft.client.render.entity.model.ShieldEntityModel;
import net.minecraft.client.render.entity.model.TridentEntityModel;
import net.minecraft.client.render.item.BuiltinModelItemRenderer;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.client.render.model.json.ModelTransformation;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;

@Mixin(BuiltinModelItemRenderer.class)
public class BuiltinItemModelRenderer {
	@Unique
	private BookModel bookModel;

	@Inject(at = @At("HEAD"), method = "reload")
	public void reload(ResourceManager manager, CallbackInfo ci) {
		this.bookModel = new BookModel(
				MinecraftClient.getInstance().getEntityModelLoader().getModelPart(EntityModelLayers.BOOK));
	}

	@Inject(at = @At("HEAD"), method = "render", cancellable = true)
	public void render(ItemStack stack, ModelTransformation.Mode mode, MatrixStack matrices,
			VertexConsumerProvider vertexConsumers, int light, int overlay, CallbackInfo ci) {
		Item item = stack.getItem();
		if (item == Magisterium.MAGISTERIUM) {
			matrices.push();
			matrices.scale(1.0F, -1.0F, -1.0F);
			VertexConsumer vertexConsumer = ItemRenderer.getDirectItemGlintConsumer(vertexConsumers,
					this.bookModel.getLayer(new Identifier("minecraft:textures/entity/enchanting_table_book.png")),
					false, stack.hasGlint());
			this.bookModel.setPageAngles(0.0F, 0.1F, 0.9F, 1.2F);
			this.bookModel.render(matrices, vertexConsumer, light, overlay, 1.0F, 1.0F, 1.0F, 1.0F);
			matrices.pop();
			ci.cancel();
		}
	}
}
