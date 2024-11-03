package io.github.reoseah.magisterium.data.effect;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.reoseah.magisterium.block.MagisteriumBlockTags;
import io.github.reoseah.magisterium.recipe.SpellBookRecipeInput;
import io.github.reoseah.magisterium.world.MagisteriumPlaygrounds;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.state.property.Properties;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

// TODO make a more general set block property effect
public class AwakenFlameEffect extends SpellEffect {
    public static final MapCodec<AwakenFlameEffect> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Identifier.CODEC.fieldOf("utterance").forGetter(effect -> effect.utterance),
            Codec.INT.fieldOf("duration").forGetter(effect -> effect.duration)
    ).apply(instance, AwakenFlameEffect::new));

    public static final int RADIUS = 16;
    public static final TagKey<Block> TAG = MagisteriumBlockTags.AWAKEN_THE_FIRE_TARGETS;

    public AwakenFlameEffect(Identifier utterance, int duration) {
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
            if (state.isIn(TAG) && state.getProperties().contains(Properties.LIT)) {
                hasTargets = true;
                if (MagisteriumPlaygrounds.trySetBlockState(world, pos, state.with(Properties.LIT, true), input.player)) {
                    hasLit = true;
                } else {
                    hasFailed = true;
                }
            }
        }

        if (!hasTargets) {
            input.player.sendMessage(Text.translatable("magisterium.gui.no_targets"), true);
            ((ServerPlayerEntity) input.player).closeHandledScreen();
        } else if (hasFailed && hasLit) {
            input.player.sendMessage(Text.translatable("magisterium.gui.partial_success"), true);
        } else if (hasFailed) {
            input.player.sendMessage(Text.translatable("magisterium.gui.no_success"), true);
            ((ServerPlayerEntity) input.player).closeHandledScreen();
        }
    }
}
