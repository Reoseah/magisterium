package com.github.reoseah.magisterium.mixin;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;

import com.github.reoseah.magisterium.mixined.MutableSlot;

import net.minecraft.screen.slot.Slot;

@Mixin(Slot.class)
public class SlotMixin implements MutableSlot {
	@Shadow
	public @Final @Mutable int x;
	@Shadow
	public @Final @Mutable int y;

	@Override
	public void setPos(int newX, int newY) {
		this.x = newX;
		this.y = newY;
	}
}
