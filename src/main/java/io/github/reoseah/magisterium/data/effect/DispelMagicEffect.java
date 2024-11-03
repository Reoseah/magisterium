package io.github.reoseah.magisterium.data.effect;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.reoseah.magisterium.block.Dispelable;
import io.github.reoseah.magisterium.block.MagisteriumBlockTags;
import io.github.reoseah.magisterium.recipe.SpellBookRecipeInput;
import io.github.reoseah.magisterium.world.MagisteriumPlaygrounds;
import net.minecraft.block.Blocks;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.util.Identifier;
import net.minecraft.util.dynamic.Codecs;
import net.minecraft.util.math.BlockPos;

public class DispelMagicEffect extends SpellEffect {
    public static final MapCodec<DispelMagicEffect> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group( //
            Identifier.CODEC.fieldOf("utterance").forGetter(effect -> effect.utterance), //
            Codecs.POSITIVE_INT.fieldOf("duration").forGetter(effect -> effect.duration) //
    ).apply(instance, DispelMagicEffect::new));

    private static final int RANGE = 16;

    public DispelMagicEffect(Identifier utterance, int duration) {
        super(utterance, duration);
    }

    @Override
    public MapCodec<? extends SpellEffect> getCodec() {
        return CODEC;
    }

    @Override
    public void finish(SpellBookRecipeInput input, RegistryWrapper.WrapperLookup lookup) {
        var world = input.getPlayer().getWorld();
        var center = input.getPlayer().getBlockPos();

        for (var pos : BlockPos.iterateOutwards(center, RANGE, RANGE, RANGE)) {
            var state = world.getBlockState(pos);
            if (state.isIn(MagisteriumBlockTags.DISPEL_MAGIC_SUSCEPTIBLE)) {
                if (state.getBlock() instanceof Dispelable dispelable) {
                    dispelable.dispel(world, pos, input.getPlayer());
                } else {
//                  world.breakBlock(pos, true);
                    MagisteriumPlaygrounds.trySetBlockState(world, pos, Blocks.AIR.getDefaultState(), input.getPlayer());
                }
                // TODO check result and send feedback
                break;
            }
        }
    }
}
