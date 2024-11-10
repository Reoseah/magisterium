package io.github.reoseah.magisterium.data.effect;

import com.google.common.collect.ImmutableMap;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.reoseah.magisterium.screen.SpellBookScreenHandler;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.inventory.Inventory;
import net.minecraft.recipe.Ingredient;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.Pair;
import net.minecraft.util.dynamic.Codecs;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;

import java.util.List;
import java.util.Map;

public class ColdSnapEffect extends SpellEffect {
    public static final MapCodec<ColdSnapEffect> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group( //
            Identifier.CODEC.fieldOf("utterance").forGetter(effect -> effect.utterance), //
            Codecs.POSITIVE_INT.fieldOf("duration").forGetter(effect -> effect.duration), //
            Codecs.POSITIVE_INT.fieldOf("range").forGetter(effect -> effect.range), //
            Codecs.POSITIVE_INT.fieldOf("ramp_length").forGetter(effect -> effect.rampLength), //
            ConflagrateEffect.BONUS_CODEC.codec().listOf().fieldOf("bonuses").forGetter(effect -> effect.bonuses) //
    ).apply(instance, ColdSnapEffect::new));

    public final int range;
    public final int rampLength;
    public final List<Pair<Ingredient, Integer>> bonuses;

    public static final Map<BlockState, BlockState> FROZEN_STATES;

    static {
        var map = new ImmutableMap.Builder<BlockState, BlockState>();
        map.put(Blocks.WATER.getDefaultState(), Blocks.ICE.getDefaultState());
        map.put(Blocks.LAVA.getDefaultState(), Blocks.OBSIDIAN.getDefaultState());
        for (var lava : Blocks.LAVA.getStateManager().getStates()) {
            if (lava != Blocks.LAVA.getDefaultState()) {
                map.put(lava, Blocks.STONE.getDefaultState());
            }
        }
        for (var fire : Blocks.FIRE.getStateManager().getStates()) {
            map.put(fire, Blocks.AIR.getDefaultState());
        }
        FROZEN_STATES = map.build();
    }

    public ColdSnapEffect(Identifier utterance, int duration, int range, int rampLength, List<Pair<Ingredient, Integer>> bonuses) {
        super(utterance, duration);
        this.range = range;
        this.rampLength = rampLength;
        this.bonuses = bonuses;
    }

    @Override
    public MapCodec<? extends SpellEffect> getCodec() {
        return CODEC;
    }

    @Override
    public void finish(ServerPlayerEntity player, Inventory inventory, SpellBookScreenHandler.Context screenContext) {
        int bonusRange = 0;
        for (int i = 0; i < inventory.size(); i++) {
            var stack = inventory.getStack(i);
            if (!stack.isEmpty()) {
                for (var bonus : this.bonuses) {
                    if (bonus.getLeft().test(stack) && SpellEffectUtil.decrementOrDischargeItem(stack)) {
                        bonusRange += bonus.getRight();
                        break;
                    }
                }
            }
        }
        int max = this.range + bonusRange;

        var world = player.getWorld();
        var center = player.getBlockPos();
        var helper = new SpellWorldChangeTracker(player);
        for (var pos : BlockPos.iterate(center.add(-max, -max, -max), center.add(max, max, max))) {
            if (!world.getBlockState(pos).isAir()) {
                continue;
            }
            var surface = world.getBlockState(pos.down());
            var surfaceFrozen = FROZEN_STATES.get(surface);
            if (surfaceFrozen != null) {
                double distance = Math.sqrt(center.getSquaredDistance(pos));
                double chance = getChance(distance, max - this.rampLength, max);
                if (world.random.nextDouble() < chance) {
                    helper.trySetBlockState(pos.down(), surfaceFrozen);
                }
            } else if (!surface.isIn(BlockTags.SNOW_LAYER_CANNOT_SURVIVE_ON) //
                    && surface.isSideSolidFullSquare(world, pos.down(), Direction.UP)) {
                double distance = Math.sqrt(center.getSquaredDistance(pos));
                double chance = getChance(distance, max - this.rampLength, max);
                if (world.random.nextDouble() < chance) {
                    helper.trySetBlockState(pos, Blocks.SNOW.getDefaultState());
                }
            }
        }

        helper.finishWorldChanges(true);
    }

    private static double getChance(double distance, int rampDownStart, int max) {
        if (distance < rampDownStart) {
            return 1;
        } else if (distance < max) {
            return 1 - (distance - rampDownStart) / (max - rampDownStart);
        } else {
            return 0;
        }
    }
}
