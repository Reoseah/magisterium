package io.github.reoseah.magisterium.mixin;

import io.github.reoseah.magisterium.screen.MutableSlot;
import net.minecraft.screen.slot.Slot;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(Slot.class)
public class SlotMixin implements MutableSlot {
    @Shadow
    @Mutable
    public @Final int x;
    @Shadow
    @Mutable
    public @Final int y;

    @Override
    public void magisterium$setPos(int newX, int newY) {
        this.x = newX;
        this.y = newY;
    }
}