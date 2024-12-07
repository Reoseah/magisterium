package io.github.reoseah.magisterium.screen;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ScreenHandler;

public class SpellBookInventory extends SimpleInventory {
    private final ScreenHandler handler;

    public SpellBookInventory(ScreenHandler handler) {
        super(16);
        this.handler = handler;
    }

    public ItemStack removeStack(int slot, int amount) {
        var stack = super.removeStack(slot, amount);
        if (!stack.isEmpty()) {
            this.handler.onContentChanged(this);
        }
        return stack;
    }

    public void setStack(int slot, ItemStack stack) {
        super.setStack(slot, stack);
        this.handler.onContentChanged(this);
    }

    public void insertResult(ItemStack result, PlayerEntity player) {
        boolean inserted = false;
        for (int i = 0; i < 16; i++) {
            var properties = ((SpellBookSlot) this.handler.slots.get(i)).properties;
            if (properties != null && properties.output) {
                var excess = insertStack(i, result);
                if (!excess.isEmpty()) {
                    player.dropItem(excess, false);
                }
                inserted = true;
                break;
            }
        }
        if (!inserted) {
            player.dropItem(result, false);
        }
    }

    public ItemStack insertStack(int slot, ItemStack stack) {
        var current = this.getStack(slot);
        if (current.isEmpty()) {
            this.setStack(slot, stack);
            return ItemStack.EMPTY;
        } else if (ItemStack.areItemsAndComponentsEqual(current, stack)) {
            int amount = Math.min(stack.getCount(), current.getMaxCount() - current.getCount());
            if (amount > 0) {
                current.increment(amount);
                this.setStack(slot, current);
                stack.decrement(amount);
            }
            return stack;
        }
        return stack;
    }
}
