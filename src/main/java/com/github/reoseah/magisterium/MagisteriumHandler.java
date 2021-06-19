package com.github.reoseah.magisterium;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;

public class MagisteriumHandler extends ScreenHandler {
	private final PlayerEntity player;
	private final ItemStack stack;
	private final int slot;

	public MagisteriumHandler(int syncId, PlayerEntity player, int slot) {
		super(Magisterium.MAGISTERIUM_SCREEN, syncId);
		this.player = player;
		this.slot = slot;
		this.stack = player.getInventory().getStack(slot);

		for (int y = 0; y < 3; y++) {
			for (int x = 0; x < 9; x++) {
				this.addSlot(new Slot(player.getInventory(), x + y * 9 + 9, 49 + x * 18, 202 + y * 18));
			}
		}
		for (int x = 0; x < 9; x++) {
			this.addSlot(new Slot(player.getInventory(), x, 49 + x * 18, 260));
		}
	}

	@Override
	public boolean canUse(PlayerEntity player) {
		return player.getInventory().getStack(this.slot) == this.stack;
	}

	public static class Client extends MagisteriumHandler {
		public Client(int syncId, PlayerInventory inventory, PacketByteBuf buf) {
			super(syncId, inventory.player, buf.readInt());
		}
	}

	public MagisteriumFold getFold() {
		return MagisteriumFold.TEST_PAGE_0;
	}
}
