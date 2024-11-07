package io.github.reoseah.magisterium.data.effect;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.reoseah.magisterium.block.ArcaneLiftBlock;
import io.github.reoseah.magisterium.block.GlyphBlock;
import io.github.reoseah.magisterium.screen.SpellBookScreenHandler;
import net.minecraft.inventory.Inventory;
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
    public void finish(ServerPlayerEntity player, Inventory inventory, SpellBookScreenHandler.Context screenContext) {
        if (inventory.getStack(0).isEmpty()) {
            player.sendMessage(Text.translatable("magisterium.missing_spell_ingredients"), true);
            player.closeHandledScreen();
            return;
        }
        var stack = inventory.getStack(0);
        stack.decrement(1);

        var world = player.getWorld();
        var pos = player.getBlockPos();

        var circlePos = find3x3GlyphCircle(world, pos, this.maxRange);
        if (circlePos == null) {
            player.sendMessage(SpellWorldChangeTracker.NO_TARGETS_FOUND, true);
            player.closeHandledScreen();
            return;
        }

        var helper = new SpellWorldChangeTracker(player);
        if (world.isAir(circlePos)) {
            helper.trySetBlockState(circlePos, ArcaneLiftBlock.INSTANCE.getDefaultState());
        }
        helper.finishWorldChanges(true);
    }

    public static BlockPos find3x3GlyphCircle(WorldAccess world, BlockPos start, int maxRange) {
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
