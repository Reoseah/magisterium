package com.github.reoseah.magisterium;

import org.jetbrains.annotations.Nullable;

import com.mojang.datafixers.util.Pair;

import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.Slot;
import net.minecraft.util.Identifier;

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

	@Override
	@Nullable
	public Pair<Identifier, Identifier> getBackgroundSprite() {
		return this.handler.getPage().getBackgroundSprite(this.index);
	}

}
