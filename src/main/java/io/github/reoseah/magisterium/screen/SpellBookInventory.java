package io.github.reoseah.magisterium.screen;

import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ScreenHandler;

class SpellBookInventory extends SimpleInventory {
    private final ScreenHandler handler;

    public SpellBookInventory(ScreenHandler handler) {
        super(16);
        this.handler = handler;
    }

    public ItemStack removeStack(int slot, int amount) {
        ItemStack stack = super.removeStack(slot, amount);
        if (!stack.isEmpty()) {
            this.handler.onContentChanged(this);
        }
        return stack;
    }

    public void setStack(int slot, ItemStack stack) {
        super.setStack(slot, stack);
        this.handler.onContentChanged(this);
    }
}
