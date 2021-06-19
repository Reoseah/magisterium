package com.github.reoseah.magisterium.mixin.client;

import java.util.Objects;

import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.github.reoseah.magisterium.Magisterium;

import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.RenderLayers;
import net.minecraft.client.render.TexturedRenderLayers;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.item.BuiltinModelItemRenderer;
import net.minecraft.client.render.item.ItemModels;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.render.model.json.ModelTransformation;
import net.minecraft.client.util.ModelIdentifier;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

@Mixin(ItemRenderer.class)
public class ItemRendererMixin {
	@Shadow
	private @Final ItemModels models;
	@Shadow
	private @Final BuiltinModelItemRenderer builtinModelItemRenderer;

	@Inject(at = @At("HEAD"), method = "getHeldItemModel", cancellable = true)
	public void getHeldItemModel(ItemStack stack, @Nullable World world, @Nullable LivingEntity entity, int seed,
			CallbackInfoReturnable<BakedModel> ci) {
		Item item = stack.getItem();
		if (item == Magisterium.MAGISTERIUM) {
			BakedModel model = this.models.getModelManager()
					.getModel(new ModelIdentifier("magisterium:magisterium_in_hand#inventory"));
			ClientWorld clientWorld = world instanceof ClientWorld ? (ClientWorld) world : null;
			model = model.getOverrides().apply(model, stack, clientWorld, entity, seed);
			ci.setReturnValue(model == null ? this.models.getModelManager().getMissingModel() : model);
		}
	}

	@Inject(at = @At("HEAD"), method = "renderItem", cancellable = true)
	public void renderItem(ItemStack stack, ModelTransformation.Mode mode, boolean leftHanded, MatrixStack matrices,
			VertexConsumerProvider vertexConsumers, int light, int overlay, BakedModel model, CallbackInfo ci) {
		if (!stack.isEmpty() && stack.getItem() == Magisterium.MAGISTERIUM) {
			matrices.push();
			boolean bl2 = mode == ModelTransformation.Mode.GUI;
			boolean bl3 = bl2 || mode == ModelTransformation.Mode.GROUND || mode == ModelTransformation.Mode.FIXED;
			if (bl3) {
				model = this.models.getModelManager()
						.getModel(new ModelIdentifier("magisterium:magisterium#inventory"));
			}
			model.getTransformation().getTransformation(mode).apply(leftHanded, matrices);
			matrices.translate(-0.5D, -0.5D, -0.5D);
			if (!model.isBuiltin() && bl3) {
				RenderLayer renderLayer = RenderLayers.getItemLayer(stack, true);
				RenderLayer renderLayer3;
				if (bl2 && Objects.equals(renderLayer, TexturedRenderLayers.getEntityTranslucentCull())) {
					renderLayer3 = TexturedRenderLayers.getEntityTranslucentCull();
				} else {
					renderLayer3 = renderLayer;
				}

				VertexConsumer vertexConsumer = ItemRenderer.getDirectItemGlintConsumer(vertexConsumers, renderLayer3,
						true, stack.hasGlint());
				this.renderBakedItemModel(model, stack, light, overlay, matrices, vertexConsumer);
			} else {
				builtinModelItemRenderer.render(stack, mode, matrices, vertexConsumers, light, overlay);
			}
			matrices.pop();
			ci.cancel();
		}
	}

	@Shadow
	private void renderBakedItemModel(BakedModel model, ItemStack stack, int light, int overlay, MatrixStack matrices,
			VertexConsumer vertices) {
		throw new Error();
	}
}
