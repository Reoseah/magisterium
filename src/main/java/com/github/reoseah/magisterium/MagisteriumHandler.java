package com.github.reoseah.magisterium;

import com.github.reoseah.magisterium.pages.MagisteriumPage;

import net.fabricmc.fabric.api.util.NbtType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.screen.Property;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;

public class MagisteriumHandler extends ScreenHandler {
	private final PlayerEntity player;
	private final ItemStack stack;
	private final int bookSlot;

	protected int page;

	protected Inventory inventory = new SimpleInventory(16) {
		public ItemStack removeStack(int slot, int amount) {
			ItemStack stack = super.removeStack(slot, amount);
			if (!stack.isEmpty()) {
				MagisteriumHandler.this.onContentChanged(this);
			}
			return stack;
		}

		public void setStack(int slot, ItemStack stack) {
			super.setStack(slot, stack);
			MagisteriumHandler.this.onContentChanged(this);
		}
	};

	public MagisteriumHandler(int syncId, PlayerEntity player, int bookSlot) {
		super(Magisterium.MAGISTERIUM_SCREEN, syncId);
		this.player = player;
		this.bookSlot = bookSlot;
		this.stack = player.getInventory().getStack(bookSlot);
		if (this.stack.hasTag() && this.stack.getTag().contains("Page", NbtType.INT)) {
			this.page = Math.min(this.stack.getTag().getInt("Page"), MagisteriumPage.all().size() - 1);
		}

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
		for (int i = 0; i < 16; i++) {
			this.addSlot(new MagisteriumSlot(this, this.inventory, i, Integer.MIN_VALUE, Integer.MIN_VALUE));
		}

		for (int y = 0; y < 3; y++) {
			for (int x = 0; x < 9; x++) {
				this.addSlot(new Slot(player.getInventory(), x + y * 9 + 9, 49 + x * 18, 202 + y * 18));
			}
		}
		for (int x = 0; x < 9; x++) {
			this.addSlot(new Slot(player.getInventory(), x, 49 + x * 18, 260));
		}

		this.setPageIndex(this.page);
	}

	@Override
	public boolean canUse(PlayerEntity player) {
		return player.getInventory().getStack(this.bookSlot) == this.stack;
	}

	@Override
	public ItemStack transferSlot(PlayerEntity player, int index) {
		Slot slot = this.slots.get(index);
		if (slot != null && slot.hasStack()) {
			ItemStack stack = slot.getStack();
			ItemStack previous = stack.copy();
			if (index < 16) {
				if (!this.insertItem(stack, 16, 16 + 36, true)) {
					return ItemStack.EMPTY;
				}
				slot.onQuickTransfer(stack, previous);
			} else {
				MagisteriumPage page = this.getPage();
				for (int i = 0; i < page.slots; i++) {
					if (page.canQuickTransfer(this.getSlot(i), i, stack)) {
						this.insertItem(stack, i, i + 1, false);
					}
				}
				if (index < 16 + 27) {
					if (!this.insertItem(stack, 16 + 27, 16 + 36, false)) {
						return ItemStack.EMPTY;
					}
				} else {
					if (!this.insertItem(stack, 16, 16 + 27, false)) {
						return ItemStack.EMPTY;
					}
				}
			}

			if (stack.isEmpty()) {
				slot.setStack(ItemStack.EMPTY);
			} else {
				slot.markDirty();
			}

			if (stack.getCount() == previous.getCount()) {
				return ItemStack.EMPTY;
			}

			slot.onTakeItem(player, stack);

			return previous;
		}

		return ItemStack.EMPTY;
	}

	@Override
	public void close(PlayerEntity player) {
		super.close(player);
		this.stack.getOrCreateTag().putInt("Page", this.page);
		this.dropInventory(player, this.inventory);
	}

	public int getPageIndex() {
		return this.page;
	}

	public void setPageIndex(int value) {
		this.page = value;
		this.getPage().onSelected(this);
	}

	public MagisteriumPage getPage() {
		return MagisteriumPage.fromIndex(this.page);
	}

	@Override
	public boolean onButtonClick(PlayerEntity player, int id) {
		if (id == 0 && this.page > 0 && !this.inventory.isEmpty()) {
			this.setPageIndex(this.page - 1);
			return true;
		}
		if (id == 1 && this.page < MagisteriumPage.all().size() - 1 && !this.inventory.isEmpty()) {
			this.setPageIndex(this.page + 1);
			return true;
		}
		return false;
	}

	public static class Client extends MagisteriumHandler {
		public Client(int syncId, PlayerInventory inventory, PacketByteBuf buf) {
			super(syncId, inventory.player, buf.readInt());
		}

	}
}
