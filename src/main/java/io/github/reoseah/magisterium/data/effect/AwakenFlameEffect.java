package io.github.reoseah.magisterium.data.effect;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.reoseah.magisterium.screen.SpellBookScreenHandler;
import net.minecraft.block.Block;
import net.minecraft.inventory.Inventory;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.state.property.Properties;
import net.minecraft.util.math.BlockPos;

// TODO make a more general set block property effect
public class AwakenFlameEffect extends SpellEffect {
    public static final MapCodec<AwakenFlameEffect> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group( //
            Codec.INT.fieldOf("duration").forGetter(effect -> effect.duration), //
            Codec.INT.fieldOf("range").forGetter(effect -> effect.range), //
            TagKey.unprefixedCodec(RegistryKeys.BLOCK).fieldOf("tag").forGetter(effect -> effect.tag) //
    ).apply(instance, AwakenFlameEffect::new));

    public final int range;
    public final TagKey<Block> tag;

    public AwakenFlameEffect(int duration, int range, TagKey<Block> tag) {
        super(duration);
        this.range = range;
        this.tag = tag;
    }

    @Override
    public MapCodec<? extends SpellEffect> getCodec() {
        return CODEC;
    }

    @Override
    public void finish(ServerPlayerEntity player, Inventory inventory, SpellBookScreenHandler.Context screenContext) {
        var world = player.getWorld();
        var center = player.getBlockPos();
        var helper = new SpellWorldChangeTracker(player);
        for (var pos : BlockPos.iterateOutwards(center, this.range, this.range, this.range)) {
            var state = world.getBlockState(pos);
            if (state.isIn(this.tag) && state.getProperties().contains(Properties.LIT)) {
                helper.trySetBlockState(pos, state.with(Properties.LIT, true));
            }
        }
        helper.finishWorldChanges(true);
    }
}
