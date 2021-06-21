package com.github.reoseah.magisterium;

import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.Slot;

public class MagisteriumSlot extends Slot {
	protected final MagisteriumHandler handler;
	protected final int index;

	public MagisteriumSlot(MagisteriumHandler handler, Inventory inventory, int index, int x, int y) {
		super(inventory, index, x, y);
		this.handler = handler;
		this.index = index;
	}

	@Override
	public boolean canInsert(ItemStack stack) {
		return this.index < this.handler.getPage().slots;
	}
}
