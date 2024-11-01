package io.github.reoseah.magisterium.recipe;

import io.github.reoseah.magisterium.block.ArcaneLiftBlock;
import io.github.reoseah.magisterium.block.GlyphBlock;
import io.github.reoseah.magisterium.world.MagisteriumPlaygrounds;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;

public class ArcaneLiftRecipe extends SpellBookRecipe {
    public static final RecipeSerializer<ArcaneLiftRecipe> SERIALIZER = new SpellBookRecipe.SimpleSerializer<>(ArcaneLiftRecipe::new);

    protected ArcaneLiftRecipe(Identifier utterance, int duration) {
        super(utterance, duration);
    }

    @Override
    public boolean matches(SpellBookRecipeInput input, World world) {
        return true;
    }

    @Override
    public ItemStack craft(SpellBookRecipeInput input, RegistryWrapper.WrapperLookup lookup) {
        var world = input.getPlayer().getWorld();
        var pos = input.getPlayer().getBlockPos();

        var circlePos = find3x3GlyphCircle(world, pos);
        if (circlePos == null) {
            return ItemStack.EMPTY;
        }

        if (world.isAir(circlePos)) {
//            world.setBlockState(circlePos, ArcaneLiftBlock.INSTANCE.getDefaultState());

            MagisteriumPlaygrounds.trySetBlockState(world, circlePos, ArcaneLiftBlock.INSTANCE.getDefaultState(), input.getPlayer());
            // TODO check result and send feedback
        }

        return ItemStack.EMPTY;
    }

    private static final int SEARCH_RADIUS = 8;

    private static BlockPos find3x3GlyphCircle(WorldAccess world, BlockPos start) {
        for (var pos : BlockPos.iterateOutwards(start, SEARCH_RADIUS, SEARCH_RADIUS, SEARCH_RADIUS)) {
            boolean isCircle = true;
            for (int dx = -1; dx <= 1; dx++) {
                for (int dz = -1; dz <= 1; dz++) {
                    if (dx == 0 && dz == 0) {
                        continue;
                    }
                    if (!world.getBlockState(pos.add(dx, 0, dz)).isOf(GlyphBlock.INSTANCE)) {
                        isCircle = false;
                        continue;
                    }
                }
            }
            if (isCircle) {
                return pos;
            }
        }

        return null;
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
