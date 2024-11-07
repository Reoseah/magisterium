package io.github.reoseah.magisterium.data.effect;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.reoseah.magisterium.recipe.SpellRecipeInput;
import io.github.reoseah.magisterium.world.WorldHelper;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.dynamic.Codecs;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;

import java.util.HashMap;

public class ColdSnapEffect extends SpellEffect {
    public static final MapCodec<ColdSnapEffect> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group( //
            Identifier.CODEC.fieldOf("utterance").forGetter(effect -> effect.utterance), //
            Codecs.POSITIVE_INT.fieldOf("duration").forGetter(effect -> effect.duration) //
    ).apply(instance, ColdSnapEffect::new));

    public ColdSnapEffect(Identifier utterance, int duration) {
        super(utterance, duration);
    }

    @Override
    public MapCodec<? extends SpellEffect> getCodec() {
        return CODEC;
    }

    @Override
    public void finish(SpellRecipeInput input, RegistryWrapper.WrapperLookup lookup) {
        int totalValue = 0;
//        for (int i = 0; i < input.size(); i++) {
//            var stack = input.getStackInSlot(i);
//            if (!stack.isEmpty()) {
//                int value = ItemValuesLoader.getValue(stack);
//                totalValue += value;
//
//                stack.decrement(1);
//                input.inventory.setStack(i, stack);
//            }
//        }

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
                        if (WorldHelper.trySetBlockState(world, pos.down(), frozen, input.player)) {
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
                        if (WorldHelper.trySetBlockState(world, pos, Blocks.SNOW.getDefaultState(), input.player)) {
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
    }
}
