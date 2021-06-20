package com.github.reoseah.magisterium;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.screen.Property;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;

public class MagisteriumHandler extends ScreenHandler {
	private final PlayerEntity player;
	private final ItemStack stack;
	private final int slot;

	protected int page = 1;

	public MagisteriumHandler(int syncId, PlayerEntity player, int slot) {
		super(Magisterium.MAGISTERIUM_SCREEN, syncId);
		this.player = player;
		this.slot = slot;
		this.stack = player.getInventory().getStack(slot);

		this.addProperty(new Property() {
			@Override
			public void set(int value) {
				MagisteriumHandler.this.page = value;
			}

			@Override
			public int get() {
				return MagisteriumHandler.this.page;
			}
		});

		for (int y = 0; y < 3; y++) {
			for (int x = 0; x < 9; x++) {
				this.addSlot(new Slot(player.getInventory(), x + y * 9 + 9, 49 + x * 18, 202 + y * 18));
			}
		}
		for (int x = 0; x < 9; x++) {
			this.addSlot(new Slot(player.getInventory(), x, 49 + x * 18, 260));
		}
	}

	public static class Client extends MagisteriumHandler {
		public Client(int syncId, PlayerInventory inventory, PacketByteBuf buf) {
			super(syncId, inventory.player, buf.readInt());
		}
	}

	@Override
	public boolean canUse(PlayerEntity player) {
		return player.getInventory().getStack(this.slot) == this.stack;
	}

	public int getPageIndex() {
		return this.page;
	}

	public void setPageIndex(int value) {
		this.page = value;
	}

	public MagisteriumPage getPage() {
		return MagisteriumPage.values()[this.page];
	}

	public boolean onButtonClick(PlayerEntity player, int id) {
		if (id == 0 && this.page > 0) {
			this.page--;
			return true;
		}
		if (id == 1 && this.page < MagisteriumPage.values().length - 1) {
			this.page++;
			return true;
		}
		return false;
	}
}
