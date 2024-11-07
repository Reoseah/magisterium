package io.github.reoseah.magisterium.data.effect;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.reoseah.magisterium.block.ArcaneLiftBlock;
import io.github.reoseah.magisterium.block.GlyphBlock;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.dynamic.Codecs;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.WorldAccess;

public class ArcaneLiftEffect extends SpellEffect {
    public static final MapCodec<ArcaneLiftEffect> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group( //
            Identifier.CODEC.fieldOf("utterance").forGetter(effect -> effect.utterance), //
            Codecs.POSITIVE_INT.fieldOf("duration").forGetter(effect -> effect.duration), //
            Codecs.POSITIVE_INT.fieldOf("max_range").forGetter(effect -> effect.maxRange) //
    ).apply(instance, ArcaneLiftEffect::new));

    public final int maxRange;

    public ArcaneLiftEffect(Identifier utterance, int duration, int maxRange) {
        super(utterance, duration);
        this.maxRange = maxRange;
    }

    @Override
    public MapCodec<? extends SpellEffect> getCodec() {
        return CODEC;
    }

    @Override
    public void finish(SpellEffectContext input, RegistryWrapper.WrapperLookup lookup) {
        if (input.getStackInSlot(0).isEmpty()) {
            input.player.sendMessage(Text.translatable("magisterium.gui.missing_ingredients"), true);
            ((ServerPlayerEntity) input.player).closeHandledScreen();
            return;
        }
        var stack = input.getStackInSlot(0);
        stack.decrement(1);

        var world = input.getWorld();
        var pos = input.getPos();

        var circlePos = find3x3GlyphCircle(world, pos, this.maxRange);
        if (circlePos == null) {
            input.player.sendMessage(Text.translatable("magisterium.gui.no_targets"), true);
            ((ServerPlayerEntity) input.player).closeHandledScreen();
            return;
        }

        if (world.isAir(circlePos)) {
            boolean success = input.trySetBlockState(circlePos, ArcaneLiftBlock.INSTANCE.getDefaultState());
            if (!success) {
                input.player.sendMessage(Text.translatable("magisterium.gui.no_success"), true);
                ((ServerPlayerEntity) input.player).closeHandledScreen();
            }
        }
    }

    private static BlockPos find3x3GlyphCircle(WorldAccess world, BlockPos start, int maxRange) {
        for (var pos : BlockPos.iterateOutwards(start, maxRange, maxRange, maxRange)) {
            boolean isCircle = true;
            for (int dx = -1; dx <= 1; dx++) {
                for (int dz = -1; dz <= 1; dz++) {
                    if (dx == 0 && dz == 0) {
                        if (!world.isAir(pos.add(dx, 0, dz))) {
                            isCircle = false;
                        }
                        continue;
                    }
                    if (!world.getBlockState(pos.add(dx, 0, dz)).isOf(GlyphBlock.INSTANCE)) {
                        isCircle = false;
                    }
                }
            }
            if (isCircle) {
                return pos;
            }
        }

        return null;
    }
}
