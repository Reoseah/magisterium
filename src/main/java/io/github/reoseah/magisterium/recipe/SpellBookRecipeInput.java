package io.github.reoseah.magisterium.recipe;

import io.github.reoseah.magisterium.screen.SpellBookScreenHandler;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.input.RecipeInput;

public class SpellBookRecipeInput implements RecipeInput {
    public final Inventory inventory;
    public final PlayerEntity player;
    public final SpellBookScreenHandler.Context context;

    public SpellBookRecipeInput(Inventory inventory, PlayerEntity player, SpellBookScreenHandler.Context context) {
        this.inventory = inventory;
        this.player = player;
        this.context = context;
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

    public SpellBookScreenHandler.Context getContext() {
        return this.context;
    }
}
