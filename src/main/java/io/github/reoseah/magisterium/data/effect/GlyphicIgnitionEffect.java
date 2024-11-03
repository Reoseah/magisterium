package io.github.reoseah.magisterium.data.effect;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.reoseah.magisterium.block.GlyphBlock;
import io.github.reoseah.magisterium.recipe.SpellBookRecipeInput;
import io.github.reoseah.magisterium.world.MagisteriumPlaygrounds;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.dynamic.Codecs;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class GlyphicIgnitionEffect extends SpellEffect {
    public static final MapCodec<GlyphicIgnitionEffect> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group( //
            Identifier.CODEC.fieldOf("utterance").forGetter(effect -> effect.utterance), //
            Codecs.POSITIVE_INT.fieldOf("duration").forGetter(effect -> effect.duration) //
    ).apply(instance, GlyphicIgnitionEffect::new));

    public static final int RADIUS = 16;

    public GlyphicIgnitionEffect(Identifier utterance, int duration) {
        super(utterance, duration);
    }

    @Override
    public MapCodec<? extends SpellEffect> getCodec() {
        return CODEC;
    }

    @Override
    public void finish(SpellBookRecipeInput input, RegistryWrapper.WrapperLookup lookup) {
        boolean hasTargets = false;
        boolean hasLit = false;
        boolean hasFailed = false;

        World world = input.player.getWorld();
        BlockPos center = input.player.getBlockPos();
        for (BlockPos pos : BlockPos.iterate(center.add(-RADIUS, -RADIUS, -RADIUS), center.add(RADIUS, RADIUS, RADIUS))) {
            BlockState state = world.getBlockState(pos);
            if (state.isOf(GlyphBlock.INSTANCE)) {
                hasTargets = true;
                if (MagisteriumPlaygrounds.trySetBlockState(world, pos, Blocks.FIRE.getDefaultState(), input.player)) {
                    hasLit = true;
                } else {
                    hasFailed = true;
                }
            }
        }

        if (!hasTargets) {
            input.player.sendMessage(Text.translatable("magisterium.gui.no_glyphs_found"), true);
            ((ServerPlayerEntity) input.player).closeHandledScreen();
        } else if (hasFailed && hasLit) {
            input.player.sendMessage(Text.translatable("magisterium.gui.partial_success"), true);
        } else if (hasFailed) {
            input.player.sendMessage(Text.translatable("magisterium.gui.no_success"), true);
            ((ServerPlayerEntity) input.player).closeHandledScreen();
        }

    }
}
