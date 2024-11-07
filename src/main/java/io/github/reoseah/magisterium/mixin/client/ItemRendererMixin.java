package io.github.reoseah.magisterium.mixin.client;

import io.github.reoseah.magisterium.item.SpellBookItem;
import net.minecraft.client.render.*;
import net.minecraft.client.render.item.BuiltinModelItemRenderer;
import net.minecraft.client.render.item.ItemModels;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.render.model.BakedModelManager;
import net.minecraft.client.util.ModelIdentifier;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.ModelTransformationMode;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Objects;

@Mixin(ItemRenderer.class)
public abstract class ItemRendererMixin {
    @Unique
    private static final ModelIdentifier SPELL_BOOK = ModelIdentifier.ofInventoryVariant(Identifier.of("magisterium", "spell_book"));

    @Shadow
    @Final
    private BakedModelManager bakedModelManager;

    @Shadow
    protected abstract void renderItem(
            ItemStack stack,
            ModelTransformationMode transformationMode,
            boolean leftHanded,
            MatrixStack matrices,
            VertexConsumerProvider vertexConsumers,
            int light,
            int overlay,
            BakedModel model,
            boolean useInventoryModel,
            float z
    );



    @Inject(method = "Lnet/minecraft/client/render/item/ItemRenderer;renderItem(Lnet/minecraft/item/ItemStack;Lnet/minecraft/item/ModelTransformationMode;ZLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;IILnet/minecraft/client/render/model/BakedModel;Z)V", at = @At("HEAD"), cancellable = true)
    private void renderItem(
            ItemStack stack,
            ModelTransformationMode transformationMode,
            boolean leftHanded,
            MatrixStack matrices,
            VertexConsumerProvider vertexConsumers,
            int light,
            int overlay,
            BakedModel model,
            boolean useInventoryModel,
            CallbackInfo info
    ) {
        if (useInventoryModel && stack.isOf(SpellBookItem.INSTANCE)) {
            model = this.bakedModelManager.getModel(SPELL_BOOK);
            this.renderItem(stack, transformationMode, leftHanded, matrices, vertexConsumers, light, overlay, model, useInventoryModel, -0.5F);
            info.cancel();
        }
    }
}