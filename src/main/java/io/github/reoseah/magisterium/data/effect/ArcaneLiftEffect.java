package io.github.reoseah.magisterium.data.effect;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.reoseah.magisterium.block.ArcaneLiftBlock;
import io.github.reoseah.magisterium.screen.SpellBookScreenHandler;
import net.minecraft.inventory.Inventory;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.dynamic.Codecs;

public class ArcaneLiftEffect extends SpellEffect {
    public static final MapCodec<ArcaneLiftEffect> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group( //
            Codecs.POSITIVE_INT.fieldOf("duration").forGetter(effect -> effect.duration), //
            Codecs.POSITIVE_INT.fieldOf("range").forGetter(effect -> effect.range) //
    ).apply(instance, ArcaneLiftEffect::new));

    public final int range;

    public ArcaneLiftEffect(int duration, int range) {
        super(duration);
        this.range = range;
    }

    @Override
    public MapCodec<? extends SpellEffect> getCodec() {
        return CODEC;
    }

    @Override
    public void finish(ServerPlayerEntity player, Inventory inventory, SpellBookScreenHandler.Context screenContext) {
        var world = player.getWorld();
        var pos = player.getBlockPos();

        var circlePos = SpellEffectUtil.find3x3GlyphCircle(world, pos, this.range);
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
}
