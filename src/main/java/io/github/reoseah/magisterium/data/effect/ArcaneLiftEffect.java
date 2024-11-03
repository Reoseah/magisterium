package io.github.reoseah.magisterium.data.effect;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.reoseah.magisterium.block.ArcaneLiftBlock;
import io.github.reoseah.magisterium.block.GlyphBlock;
import io.github.reoseah.magisterium.recipe.SpellBookRecipeInput;
import io.github.reoseah.magisterium.world.MagisteriumPlaygrounds;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.WorldAccess;

public class ArcaneLiftEffect extends SpellEffect {
    public static final MapCodec<ArcaneLiftEffect> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group( //
            Identifier.CODEC.fieldOf("utterance").forGetter(effect -> effect.utterance), //
            Codec.INT.fieldOf("duration").forGetter(effect -> effect.duration) //
    ).apply(instance, ArcaneLiftEffect::new));

    private static final int SEARCH_RADIUS = 8;

    public ArcaneLiftEffect(Identifier utterance, int duration) {
        super(utterance, duration);
    }

    @Override
    public MapCodec<? extends SpellEffect> getCodec() {
        return CODEC;
    }

    @Override
    public void finish(SpellBookRecipeInput input, RegistryWrapper.WrapperLookup lookup) {
        var world = input.getPlayer().getWorld();
        var pos = input.getPlayer().getBlockPos();

        var circlePos = find3x3GlyphCircle(world, pos);
        if (circlePos == null) {
            return;
        }

        if (world.isAir(circlePos)) {
//            world.setBlockState(circlePos, ArcaneLiftBlock.INSTANCE.getDefaultState());

            MagisteriumPlaygrounds.trySetBlockState(world, circlePos, ArcaneLiftBlock.INSTANCE.getDefaultState(), input.getPlayer());
            // TODO check result and send feedback
        }
    }

    private static BlockPos find3x3GlyphCircle(WorldAccess world, BlockPos start) {
        for (var pos : BlockPos.iterateOutwards(start, SEARCH_RADIUS, SEARCH_RADIUS, SEARCH_RADIUS)) {
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
