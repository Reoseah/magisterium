package io.github.reoseah.magisterium.recipe;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.input.RecipeInput;

public class SpellBookRecipeInput implements RecipeInput {
    protected final Inventory inventory;
    protected final PlayerEntity player;

    public SpellBookRecipeInput(Inventory inventory, PlayerEntity player) {
        this.inventory = inventory;
        this.player = player;
    }

    @Override
    public ItemStack getStackInSlot(int slot) {
        return this.inventory.getStack(slot);
    }

    @Override
    public int getSize() {
        return this.inventory.size();
    }

    public ItemStack removeStack(int slot, int count) {
        return this.inventory.removeStack(slot, count);
    }

    public PlayerEntity getPlayer() {
        return this.player;
    }
}
