package io.github.reoseah.magisterium.magisterium.effect;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.reoseah.magisterium.block.GlyphBlock;
import io.github.reoseah.magisterium.screen.SpellBookInventory;
import io.github.reoseah.magisterium.screen.SpellBookScreenHandler;
import io.github.reoseah.magisterium.util.SpellWorldChangeTracker;
import net.minecraft.block.Blocks;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.dynamic.Codecs;
import net.minecraft.util.math.BlockPos;

public class GlyphicIgnitionEffect extends SpellEffect {
    public static final MapCodec<GlyphicIgnitionEffect> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group( //
            Codecs.POSITIVE_INT.fieldOf("duration").forGetter(effect -> effect.duration), //
            Codecs.POSITIVE_INT.fieldOf("range").forGetter(effect -> effect.range) //
    ).apply(instance, GlyphicIgnitionEffect::new));

    public final int range;

    public GlyphicIgnitionEffect(int duration, int range) {
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
        for (var pos : BlockPos.iterateOutwards(center, this.range, this.range, this.range)) {
            var state = world.getBlockState(pos);
            if (state.isOf(GlyphBlock.INSTANCE)) {
                helper.trySetBlockState(pos, Blocks.FIRE.getDefaultState());
            }
        }
        helper.finishWorldChanges(true);
    }
}
