package io.github.reoseah.magisterium.screen;

import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.input.RecipeInput;

public class SpellBookRecipeInput implements RecipeInput {
    protected final Inventory inventory;

    public SpellBookRecipeInput(Inventory inventory) {
        this.inventory = inventory;
    }

    @Override
    public ItemStack getStackInSlot(int slot) {
        return this.inventory.getStack(slot);
    }

    @Override
    public int getSize() {
        return this.inventory.size();
    }
}
