package io.github.reoseah.magisterium.data.effect;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.reoseah.magisterium.block.GlyphBlock;
import io.github.reoseah.magisterium.network.SpellParticlePayload;
import io.github.reoseah.magisterium.world.WorldHelper;
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.dynamic.Codecs;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.ArrayList;

public class GlyphicIgnitionEffect extends SpellEffect {
    public static final MapCodec<GlyphicIgnitionEffect> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group( //
            Identifier.CODEC.fieldOf("utterance").forGetter(effect -> effect.utterance), //
            Codecs.POSITIVE_INT.fieldOf("duration").forGetter(effect -> effect.duration), //
            Codecs.POSITIVE_INT.fieldOf("max_range").forGetter(effect -> effect.maxRange) //
    ).apply(instance, GlyphicIgnitionEffect::new));

    public final int maxRange;

    public GlyphicIgnitionEffect(Identifier utterance, int duration, int maxRange) {
        super(utterance, duration);
        this.maxRange = maxRange;
    }

    @Override
    public MapCodec<? extends SpellEffect> getCodec() {
        return CODEC;
    }

    @Override
    public void finish(SpellEffectContext input, RegistryWrapper.WrapperLookup lookup) {
        var targets = new ArrayList<BlockPos>();
        boolean hasSuccess = false;
        boolean hasFailure = false;

        World world = input.player.getWorld();
        BlockPos center = input.player.getBlockPos();
        for (BlockPos pos : BlockPos.iterateOutwards(center, this.maxRange, this.maxRange, this.maxRange)) {
            BlockState state = world.getBlockState(pos);
            if (state.isOf(GlyphBlock.INSTANCE)) {
                targets.add(pos.toImmutable());
                if (WorldHelper.trySetBlockState(world, pos, Blocks.FIRE.getDefaultState(), input.player)) {
                    hasSuccess = true;
                } else {
                    hasFailure = true;
                }
            }
        }
        if (!targets.isEmpty()) {
            var payload = new SpellParticlePayload(targets);
            for (var player : PlayerLookup.tracking((ServerWorld) world, center)) {
                ServerPlayNetworking.send(player, payload);
            }
        } else {
            input.player.sendMessage(Text.translatable("magisterium.gui.no_targets"), true);
            ((ServerPlayerEntity) input.player).closeHandledScreen();
            return;
        }

        if (hasFailure) {
            if (hasSuccess) {
                input.player.sendMessage(Text.translatable("magisterium.gui.partial_success"), true);
            } else {
                input.player.sendMessage(Text.translatable("magisterium.gui.no_success"), true);
                ((ServerPlayerEntity) input.player).closeHandledScreen();
            }
        }
    }
}
