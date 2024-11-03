package io.github.reoseah.magisterium.data.effect;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.reoseah.magisterium.block.CustomDispellingHandler;
import io.github.reoseah.magisterium.block.MagisteriumBlockTags;
import io.github.reoseah.magisterium.recipe.SpellRecipeInput;
import net.minecraft.block.Blocks;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.dynamic.Codecs;
import net.minecraft.util.math.BlockPos;

public class DispelMagicEffect extends SpellEffect {
    public static final MapCodec<DispelMagicEffect> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group( //
            Identifier.CODEC.fieldOf("utterance").forGetter(effect -> effect.utterance), //
            Codecs.POSITIVE_INT.fieldOf("duration").forGetter(effect -> effect.duration), //
            Codecs.POSITIVE_INT.fieldOf("max_range").forGetter(effect -> effect.maxRange) //
    ).apply(instance, DispelMagicEffect::new));

    public final int maxRange;

    public DispelMagicEffect(Identifier utterance, int duration, int maxRange) {
        super(utterance, duration);
        this.maxRange = maxRange;
    }

    @Override
    public MapCodec<? extends SpellEffect> getCodec() {
        return CODEC;
    }

    @Override
    public void finish(SpellRecipeInput input, RegistryWrapper.WrapperLookup lookup) {
        var world = input.getPlayer().getWorld();
        var center = input.getPlayer().getBlockPos();

        for (var pos : BlockPos.iterateOutwards(center, this.maxRange, this.maxRange, this.maxRange)) {
            var state = world.getBlockState(pos);
            if (state.isIn(MagisteriumBlockTags.DISPEL_MAGIC_SUSCEPTIBLE)) {
                boolean success = (state.getBlock() instanceof CustomDispellingHandler dispelable) ? dispelable.dispel(world, pos, input.getPlayer()) : input.trySetBlockState(pos, Blocks.AIR.getDefaultState());

                if (success) {
                    // TODO send a packet spawn particles, play sound
                } else {
                    input.player.sendMessage(Text.translatable("magisterium.gui.no_success"), true);
                    ((ServerPlayerEntity) input.player).closeHandledScreen();
                }
                return;
            }
        }
    }
}
