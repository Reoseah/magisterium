package io.github.reoseah.magisterium.recipe;

import net.fabricmc.fabric.api.registry.FlammableBlockRegistry;
import net.minecraft.block.Blocks;
import net.minecraft.block.ConnectingBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;

public class ConflagrateRecipe extends SpellBookRecipe {
    public static final RecipeSerializer<ConflagrateRecipe> SERIALIZER = new SpellBookRecipe.SimpleSerializer<>(ConflagrateRecipe::new);

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

        // TODO consume the items to increase the range

        // TODO make the area circular instead of square

        // TODO spawn a bunch of fire particles in the area

        int buildUpStart = 1, buildUpFinish = 5, decayStart = 11, decayFinish = 15;

        var world = input.player.getWorld();
        var center = input.player.getBlockPos();
        for (var pos : BlockPos.iterate(center.add(-decayFinish, -decayFinish, -decayFinish), center.add(decayFinish, decayFinish, decayFinish))) {
            var state = world.getBlockState(pos);
            var block = state.getBlock();
            var entry = FlammableBlockRegistry.getInstance(Blocks.FIRE).get(block);

            double distance = Math.sqrt(center.getSquaredDistance(pos));
            double chance;
            if (distance < buildUpStart) {
                chance = 0;
            } else if (distance < buildUpFinish) {
                chance = (distance - buildUpStart) / (buildUpFinish - buildUpStart);
            } else if (distance < decayStart) {
                chance = 1;
            } else if (distance < decayFinish) {
                chance = 1 - (distance - decayStart) / (decayFinish - decayStart);
            } else {
                chance = 0;
            }

            if (entry != null && entry.getBurnChance() > 0 && world.random.nextFloat() < chance) {
                for (var direction : Direction.values()) {

                    var side = pos.offset(direction);
                    if (world.isAir(side)) {
                        var below = side.down();
                        var stateBelow = world.getBlockState(below);

                        // TODO: perhaps should make FireBlock#getStateForPosition accessible
                        if (stateBelow.isSideSolidFullSquare(world, below, Direction.UP)) {
                            world.setBlockState(side, Blocks.FIRE.getDefaultState());
                        } else {
                            var fireState = Blocks.FIRE.getDefaultState();
                            for (var direction2 : Direction.values()) {
                                if (direction == Direction.DOWN) {
                                    continue;
                                }
                                var side2 = side.offset(direction2);
                                var entry2 = FlammableBlockRegistry.getInstance(Blocks.FIRE).get(world.getBlockState(side2).getBlock());
                                if (entry2 != null && entry2.getBurnChance() > 0) {
                                    var state2 = world.getBlockState(side2);
                                    if (state2.isSideSolidFullSquare(world, side2, direction2.getOpposite())) {
                                        fireState = fireState.with(ConnectingBlock.FACING_PROPERTIES.get(direction2), true);
                                    }
                                }
                            }
                            world.setBlockState(side, fireState);
                        }
                    }
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
