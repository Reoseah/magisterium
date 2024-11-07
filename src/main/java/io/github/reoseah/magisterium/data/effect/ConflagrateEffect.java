package io.github.reoseah.magisterium.data.effect;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.reoseah.magisterium.recipe.SpellRecipeInput;
import io.github.reoseah.magisterium.world.WorldHelper;
import net.fabricmc.fabric.api.registry.FlammableBlockRegistry;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.ConnectingBlock;
import net.minecraft.recipe.Ingredient;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.Pair;
import net.minecraft.util.dynamic.Codecs;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;

import java.util.List;

public class ConflagrateEffect extends SpellEffect {
    public static final MapCodec<Pair<Ingredient, Integer>> BONUS_CODEC = RecordCodecBuilder.mapCodec(bonus -> bonus.group( //
            Ingredient.CODEC.fieldOf("ingredient").forGetter(Pair::getLeft), //
            Codecs.POSITIVE_INT.fieldOf("range_increase").forGetter(Pair::getRight) //
    ).apply(bonus, Pair::new));

    public static final MapCodec<ConflagrateEffect> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group( //
            Identifier.CODEC.fieldOf("utterance").forGetter(effect -> effect.utterance), //
            Codecs.POSITIVE_INT.fieldOf("duration").forGetter(effect -> effect.duration), //
            Codecs.POSITIVE_INT.fieldOf("offset").forGetter(effect -> effect.offset), //
            Codecs.POSITIVE_INT.fieldOf("range").forGetter(effect -> effect.range), //
            Codecs.POSITIVE_INT.fieldOf("ramp_length").forGetter(effect -> effect.rampLength), //
            BONUS_CODEC.codec().listOf().fieldOf("bonuses").forGetter(effect -> effect.bonuses) //
    ).apply(instance, ConflagrateEffect::new));

    public final int offset;
    public final int range;
    public final int rampLength;
    public final List<Pair<Ingredient, Integer>> bonuses;

    public ConflagrateEffect(Identifier utterance,
                             int duration,
                             int offset,
                             int range,
                             int rampLength,
                             List<Pair<Ingredient, Integer>> bonuses
    ) {
        super(utterance, duration);
        this.offset = offset;
        this.range = range;
        this.rampLength = rampLength;
        this.bonuses = bonuses;
    }

    @Override
    public MapCodec<? extends SpellEffect> getCodec() {
        return CODEC;
    }

    @Override
    public void finish(SpellRecipeInput input, RegistryWrapper.WrapperLookup lookup) {
        int bonusRange = 0;
        for (int i = 0; i < input.size(); i++) {
            var stack = input.getStackInSlot(i);
            if (!stack.isEmpty()) {
                for (var bonus : bonuses) {
                    if (bonus.getLeft().test(stack)) {
                        bonusRange += bonus.getRight();
                        stack.decrement(1);
                        break;
                    }
                }
            }
        }

        int max = this.offset + this.range + bonusRange;

        boolean hasTargets = false;
        boolean hasLit = false;
        boolean hasFailed = false;

        var world = input.player.getWorld();
        var center = input.player.getBlockPos();
        for (var pos : BlockPos.iterate(center.add(-max, -max, -max), center.add(max, max, max))) {
            var state = world.getBlockState(pos);
            var block = state.getBlock();
            var entry = FlammableBlockRegistry.getInstance(Blocks.FIRE).get(block);

            double distance = Math.sqrt(center.getSquaredDistance(pos));
            double chance = getChance(distance, this.offset, this.offset + this.rampLength, max - this.rampLength, max);

            if (entry != null && entry.getBurnChance() > 0) {
                if (world.random.nextFloat() < chance) {
                    for (var direction : Direction.values()) {
                        var side = pos.offset(direction);
                        if (world.isAir(side)) {
                            hasTargets = true;

                            if (WorldHelper.trySetBlockState(world, side, getFireStateForPosition(world, side), input.player)) {
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
    }

    private static double getChance(double distance, int min, int plateauStart, int plateauEnd, int max) {
        if (distance < min) {
            return 0;
        } else if (distance < plateauStart) {
            return (distance - min) / (plateauStart - min);
        } else if (distance < plateauEnd) {
            return 1;
        } else if (distance < max) {
            return 1 - (distance - plateauEnd) / (max - plateauEnd);
        } else {
            return 0;
        }
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
}
