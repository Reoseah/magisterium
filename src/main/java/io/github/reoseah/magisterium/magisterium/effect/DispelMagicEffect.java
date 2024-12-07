package io.github.reoseah.magisterium.magisterium.effect;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.reoseah.magisterium.block.CustomDispelBehavior;
import io.github.reoseah.magisterium.block.MagisteriumBlockTags;
import io.github.reoseah.magisterium.screen.SpellBookInventory;
import io.github.reoseah.magisterium.screen.SpellBookScreenHandler;
import io.github.reoseah.magisterium.util.SpellWorldChangeTracker;
import net.minecraft.block.Blocks;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.dynamic.Codecs;
import net.minecraft.util.math.BlockPos;

public class DispelMagicEffect extends SpellEffect {
    public static final MapCodec<DispelMagicEffect> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group( //
            Codecs.POSITIVE_INT.fieldOf("duration").forGetter(effect -> effect.duration), //
            Codecs.POSITIVE_INT.fieldOf("range").forGetter(effect -> effect.range) //
    ).apply(instance, DispelMagicEffect::new));

    public final int range;

    public DispelMagicEffect(int duration, int range) {
        super(duration);
        this.range = range;
    }

    @Override
    public MapCodec<? extends SpellEffect> getCodec() {
        return CODEC;
    }

    @Override
    public void finish(ServerPlayerEntity player, SpellBookInventory inventory, SpellBookScreenHandler.Context screenContext) {
        var world = player.getWorld();
        var center = player.getBlockPos();

        var helper = new SpellWorldChangeTracker(player);
        for (var pos : BlockPos.iterate(center.add(-this.range, -this.range, -this.range), //
                center.add(this.range, this.range, this.range))) {
            var state = world.getBlockState(pos);
            if (state.isIn(MagisteriumBlockTags.DISPEL_MAGIC_SUSCEPTIBLE)) {
                if (state.getBlock() instanceof CustomDispelBehavior behavior) {
                    behavior.dispel(helper, pos);
                } else {
                    helper.trySetBlockState(pos, Blocks.AIR.getDefaultState());
                }

                break;
            }
        }
        helper.finishWorldChanges(true);
    }
}
