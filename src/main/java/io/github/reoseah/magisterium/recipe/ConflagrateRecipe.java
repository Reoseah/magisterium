package io.github.reoseah.magisterium.recipe;

import io.github.reoseah.magisterium.world.MagisteriumPlaygrounds;
import net.fabricmc.fabric.api.registry.FlammableBlockRegistry;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.ConnectingBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
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
        // TODO consume the items to increase the range

        // TODO make the area circular instead of square

        // TODO spawn a bunch of fire particles in the area

        final int buildUpStart = 1, buildUpFinish = 5, decayStart = 11, decayFinish = 15;

        boolean hasTargets = false;
        boolean hasLit = false;
        boolean hasFailed = false;

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

            if (entry != null && entry.getBurnChance() > 0) {
                if (world.random.nextFloat() < chance) {
                    for (var direction : Direction.values()) {
                        var side = pos.offset(direction);
                        if (world.isAir(side)) {
                            hasTargets = true;

                            if (MagisteriumPlaygrounds.trySetBlockState(world, side, getFireStateForPosition(world, side), input.player)) {
                                hasLit = true;
                            } else {
                                hasFailed = true;
                            }
                        }
                    }
                }
            }
        }

        if (!hasTargets) {
            input.player.sendMessage(Text.translatable("magisterium.gui.no_targets"), true);
            ((ServerPlayerEntity) input.player).closeHandledScreen();
        } else if (hasFailed && hasLit) {
            input.player.sendMessage(Text.translatable("magisterium.gui.partial_success"), true);
        } else if (hasFailed) {
            input.player.sendMessage(Text.translatable("magisterium.gui.no_success"), true);
            ((ServerPlayerEntity) input.player).closeHandledScreen();
        }

        return ItemStack.EMPTY;
    }

    private static BlockState getFireStateForPosition(World world, BlockPos pos) {
        var below = pos.down();
        if (world.getBlockState(below).isSideSolidFullSquare(world, below, Direction.UP)) {
            return Blocks.FIRE.getDefaultState();
        } else {
            var fireState = Blocks.FIRE.getDefaultState();
            for (var direction : Direction.values()) {
                if (direction == Direction.DOWN) {
                    continue;
                }
                var side = pos.offset(direction);
                var entry = FlammableBlockRegistry.getInstance(Blocks.FIRE).get(world.getBlockState(side).getBlock());
                if (entry != null && entry.getBurnChance() > 0) {
                    var state = world.getBlockState(side);
                    if (state.isSideSolidFullSquare(world, side, direction.getOpposite())) {
                        fireState = fireState.with(ConnectingBlock.FACING_PROPERTIES.get(direction), true);
                    }
                }
            }
            return fireState;
        }
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
