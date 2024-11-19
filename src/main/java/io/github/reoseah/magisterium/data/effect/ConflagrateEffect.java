package io.github.reoseah.magisterium.data.effect;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.reoseah.magisterium.screen.SpellBookScreenHandler;
import net.fabricmc.fabric.api.registry.FlammableBlockRegistry;
import net.minecraft.block.AbstractFireBlock;
import net.minecraft.block.Blocks;
import net.minecraft.inventory.Inventory;
import net.minecraft.recipe.Ingredient;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Pair;
import net.minecraft.util.dynamic.Codecs;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;

import java.util.List;

public class ConflagrateEffect extends SpellEffect {
    public static final MapCodec<Pair<Ingredient, Integer>> BONUS_CODEC = RecordCodecBuilder.mapCodec(bonus -> bonus.group( //
            Ingredient.CODEC.fieldOf("ingredient").forGetter(Pair::getLeft), //
            Codecs.POSITIVE_INT.fieldOf("range_increase").forGetter(Pair::getRight) //
    ).apply(bonus, Pair::new));

    public static final MapCodec<ConflagrateEffect> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group( //
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

    public ConflagrateEffect(int duration, //
                             int offset, //
                             int range, //
                             int rampLength, //
                             List<Pair<Ingredient, Integer>> bonuses //
    ) {
        super(duration);
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
        inventory.markDirty();

        int max = this.offset + this.range + bonusRange;

        var world = player.getWorld();
        var center = player.getBlockPos();
        var helper = new SpellWorldChangeTracker(player);
        for (var pos : BlockPos.iterate(center.add(-max, -max, -max), center.add(max, max, max))) {
            var state = world.getBlockState(pos);
            var block = state.getBlock();
            var entry = FlammableBlockRegistry.getInstance(Blocks.FIRE).get(block);

            double distance = Math.sqrt(center.getSquaredDistance(pos));
            double chance = getChance(distance, this.offset, this.offset + this.rampLength, max - this.rampLength, max);

            if (entry != null && entry.getBurnChance() > 0) {
                if (world.random.nextFloat() < chance) {
                    for (var direction : Direction.values()) {
                        var neighbor = pos.offset(direction);
                        if (world.isAir(neighbor) && AbstractFireBlock.canPlaceAt(world, neighbor, direction.getOpposite())) {
                            helper.trySetBlockState(neighbor, AbstractFireBlock.getState(world, neighbor));
                        }
                    }
                }
            }
        }

        helper.finishWorldChanges(true);
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

}
