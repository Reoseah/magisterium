package io.github.reoseah.magisterium.recipe;

import io.github.reoseah.magisterium.block.ArcaneLiftBlock;
import io.github.reoseah.magisterium.screen.SpellBookScreenHandler;
import io.github.reoseah.magisterium.world.MagisteriumPlaygrounds;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.input.RecipeInput;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class SpellRecipeInput implements RecipeInput {
    public final Inventory inventory;
    public final PlayerEntity player;
    public final SpellBookScreenHandler.Context context;

    public SpellRecipeInput(Inventory inventory, PlayerEntity player, SpellBookScreenHandler.Context context) {
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

    public World getWorld() {
        return this.player.getWorld();
    }

    public BlockPos getPos() {
        return this.player.getBlockPos();
    }

    public SpellBookScreenHandler.Context getContext() {
        return this.context;
    }

    public boolean trySetBlockState(BlockPos pos, BlockState state) {
        return MagisteriumPlaygrounds.trySetBlockState(this.getWorld(), pos, state, this.player);
    }
}
