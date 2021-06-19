package com.github.reoseah.magisterium;

import org.jetbrains.annotations.Nullable;

import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerFactory;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;

public class MagisteriumItem extends Item {
	public MagisteriumItem(Item.Settings settings) {
		super(settings);
	}

	@Override
	public TypedActionResult<ItemStack> use(World world, PlayerEntity player, Hand hand) {
		ItemStack stack = player.getStackInHand(hand);

		if (player.world != null && !player.world.isClient) {
			int slot = hand == Hand.OFF_HAND ? 41 : player.getInventory().selectedSlot;

			player.openHandledScreen(new ExtendedScreenHandlerFactory() {
				@Override
				public void writeScreenOpeningData(ServerPlayerEntity serverPlayerEntity, PacketByteBuf buffer) {
					buffer.writeInt(slot);
				}

				@Override
				public Text getDisplayName() {
					return new TranslatableText(stack.getItem().getTranslationKey(stack));
				}

				@Override
				public @Nullable ScreenHandler createMenu(int syncId, PlayerInventory inv, PlayerEntity player) {
					return new MagisteriumHandler(syncId, player, slot);
				}
			});
		}

		return TypedActionResult.success(stack);
	}
}
