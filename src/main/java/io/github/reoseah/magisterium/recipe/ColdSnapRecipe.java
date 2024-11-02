package io.github.reoseah.magisterium.recipe;

import io.github.reoseah.magisterium.data.ItemValuesLoader;
import io.github.reoseah.magisterium.world.MagisteriumPlaygrounds;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;

import java.util.HashMap;

public class ColdSnapRecipe extends SpellBookRecipe {
    public static final RecipeSerializer<ColdSnapRecipe> SERIALIZER = new SpellBookRecipe.SimpleSerializer<>(ColdSnapRecipe::new);

    protected ColdSnapRecipe(Identifier utterance, int duration) {
        super(utterance, duration);
    }

    @Override
    public boolean matches(SpellBookRecipeInput input, World world) {
        return true;
    }

    @Override
    public ItemStack craft(SpellBookRecipeInput input, RegistryWrapper.WrapperLookup lookup) {
        int totalValue = 0;
        for (int i = 0; i < input.getSize(); i++) {
            var stack = input.getStackInSlot(i);
            if (!stack.isEmpty()) {
                int value = ItemValuesLoader.getValue(stack);
                totalValue += value;

                stack.decrement(1);
                input.inventory.setStack(i, stack);
            }
        }

        int fullFreezeRadius = 3 + totalValue;
        int maxRadius = 7 + totalValue;

        final var map = new HashMap<BlockState, BlockState>();
        map.put(Blocks.WATER.getDefaultState(), Blocks.ICE.getDefaultState());
        map.put(Blocks.LAVA.getDefaultState(), Blocks.OBSIDIAN.getDefaultState());
        for (var lava : Blocks.LAVA.getStateManager().getStates()) {
            map.putIfAbsent(lava, Blocks.STONE.getDefaultState());
        }
        for (var fire : Blocks.FIRE.getStateManager().getStates()) {
            map.put(fire, Blocks.AIR.getDefaultState());
        }

        boolean hasTargets = false;
        boolean hasFrozen = false;
        boolean hasFailed = false;

        var world = input.player.getWorld();
        var center = input.player.getBlockPos();
        for (var pos : BlockPos.iterate(center.add(-maxRadius, -maxRadius, -maxRadius), center.add(maxRadius, maxRadius, maxRadius))) {
            if (world.getBlockState(pos).isAir()) {
                var below = world.getBlockState(pos.down());
                var frozen = map.get(below);
                if (frozen != null) {
                    hasTargets = true;

                    double distance = Math.sqrt(center.getSquaredDistance(pos));
                    double chance;
                    if (distance < fullFreezeRadius) {
                        chance = 1;
                    } else if (distance < maxRadius) {
                        chance = 1 - (distance - fullFreezeRadius) / (maxRadius - fullFreezeRadius);
                    } else {
                        chance = 0;
                    }
                    if (world.random.nextDouble() < chance) {
                        if (MagisteriumPlaygrounds.trySetBlockState(world, pos.down(), frozen, input.player)) {
                            hasFrozen = true;
                        } else {
                            hasFailed = true;
                        }
                    }
                } else if (below.isSideSolidFullSquare(world, pos.down(), Direction.UP)) {
                    hasTargets = true;

                    double distance = Math.sqrt(center.getSquaredDistance(pos));
                    double chance;
                    if (distance < fullFreezeRadius) {
                        chance = 1;
                    } else if (distance < maxRadius) {
                        chance = 1 - (distance - fullFreezeRadius) / (maxRadius - fullFreezeRadius);
                    } else {
                        chance = 0;
                    }
                    if (world.random.nextDouble() < chance) {
                        if (MagisteriumPlaygrounds.trySetBlockState(world, pos, Blocks.SNOW.getDefaultState(), input.player)) {
                            hasFrozen = true;
                        } else {
                            hasFailed = true;
                        }
                    }
                }
            }
        }

        if (!hasTargets) {
            input.player.sendMessage(Text.translatable("magisterium.gui.no_targets"), true);
            ((ServerPlayerEntity) input.player).closeHandledScreen();
        } else if (hasFailed && hasFrozen) {
            input.player.sendMessage(Text.translatable("magisterium.gui.partial_success"), true);
        } else if (hasFailed) {
            input.player.sendMessage(Text.translatable("magisterium.gui.no_success"), true);
            ((ServerPlayerEntity) input.player).closeHandledScreen();
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
