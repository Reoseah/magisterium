package io.github.reoseah.magisterium.recipe;

import io.github.reoseah.magisterium.MagisteriumBlockTags;
import net.minecraft.block.BlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.state.property.Properties;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class AwakenFlameRecipe extends SpellBookRecipe {
    public static final RecipeSerializer<AwakenFlameRecipe> SERIALIZER = new SpellBookRecipe.SimpleSerializer<>(AwakenFlameRecipe::new);

    public static final int RADIUS = 16;

    protected AwakenFlameRecipe(Identifier utterance, int duration) {
        super(utterance, duration);
    }

    @Override
    public boolean matches(SpellBookRecipeInput input, World world) {
        return true;
    }

    @Override
    public ItemStack craft(SpellBookRecipeInput input, RegistryWrapper.WrapperLookup lookup) {
        // TODO check if the player can edit the blocks in the area
        //      show a message if they can't, stylized to fit the theme
        //      like "There is a force preventing you from altering the world here."

        World world = input.player.getWorld();
        BlockPos center = input.player.getBlockPos();
        for (BlockPos pos : BlockPos.iterate(center.add(-RADIUS, -RADIUS, -RADIUS), center.add(RADIUS, RADIUS, RADIUS))) {
            BlockState state = world.getBlockState(pos);
            if (state.getProperties().contains(Properties.LIT)) {
                if (state.isIn(MagisteriumBlockTags.AWAKEN_THE_FIRE_TARGETS)) {
                    world.setBlockState(pos, state.with(Properties.LIT, true));
                }
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
