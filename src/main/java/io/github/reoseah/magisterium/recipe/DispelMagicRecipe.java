package io.github.reoseah.magisterium.recipe;

import io.github.reoseah.magisterium.block.Dispelable;
import io.github.reoseah.magisterium.block.MagisteriumBlockTags;
import io.github.reoseah.magisterium.world.MagisteriumPlaygrounds;
import net.minecraft.block.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class DispelMagicRecipe extends SpellBookRecipe {
    public static final RecipeSerializer<DispelMagicRecipe> SERIALIZER = new SpellBookRecipe.SimpleSerializer<>(DispelMagicRecipe::new);

    protected DispelMagicRecipe(Identifier utterance, int duration) {
        super(utterance, duration);
    }

    @Override
    public boolean matches(SpellBookRecipeInput input, World world) {
        return true;
    }

    private static final int RANGE = 16;

    @Override
    public ItemStack craft(SpellBookRecipeInput input, RegistryWrapper.WrapperLookup lookup) {
        var world = input.getPlayer().getWorld();
        var center = input.getPlayer().getBlockPos();

        for (var pos : BlockPos.iterateOutwards(center, RANGE, RANGE, RANGE)) {
            var state = world.getBlockState(pos);
            if (state.isIn(MagisteriumBlockTags.DISPEL_MAGIC_SUSCEPTIBLE)) {
                if (state.getBlock() instanceof Dispelable dispelable) {
                    dispelable.dispel(world, pos, input.getPlayer());
                } else {
//                  world.breakBlock(pos, true);
                    MagisteriumPlaygrounds.trySetBlockState(world, pos, Blocks.AIR.getDefaultState(), input.getPlayer());
                }
                // TODO check result and send feedback
                break;
            }
        }
        return ItemStack.EMPTY;
    }

    @Override
    public ItemStack getResult(RegistryWrapper.WrapperLookup registriesLookup) {
        return ItemStack.EMPTY;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return SERIALIZER;
    }
}
