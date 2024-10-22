package io.github.reoseah.magisterium.recipe;

import io.github.reoseah.magisterium.MagisteriumBlockTags;
import net.fabricmc.fabric.api.registry.FlammableBlockRegistry;
import net.minecraft.block.*;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.state.property.Properties;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class ConflagrateRecipe extends SpellBookRecipe {
    public static final RecipeSerializer<ConflagrateRecipe> SERIALIZER = new SpellBookRecipe.SimpleSerializer<>(ConflagrateRecipe::new);

    public static final int RADIUS = 16;

    protected ConflagrateRecipe(Identifier utterance, int duration) {
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

        // TODO make the area circular instead of square

        // TODO have a trapezoid-shaped probability for setting blocks on fire
        //      which starts a few blocks away from the caster, linearly increasing
        //      to 100% and flattening out there, then linearly decreasing to 0%
        //      so the edges of the spell are a bit more fuzzy

        // TODO spawn a bunch of fire particles in the area

        var world = input.player.getWorld();
        var center = input.player.getBlockPos();
        for (var pos : BlockPos.iterate(center.add(-RADIUS, -RADIUS, -RADIUS), center.add(RADIUS, RADIUS, RADIUS))) {
            var state = world.getBlockState(pos);
            var block = state.getBlock();
            var entry = FlammableBlockRegistry.getInstance(Blocks.FIRE).get(block);
            if (entry != null && entry.getBurnChance() > 0) {
                var above = pos.up();
                if (world.isAir(above)) {
                    world.setBlockState(above, Blocks.FIRE.getDefaultState());
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
