package io.github.reoseah.magisterium.data.effect;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.reoseah.magisterium.network.SpellParticlePayload;
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.state.property.Properties;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.dynamic.Codecs;
import net.minecraft.util.math.BlockPos;

import java.util.ArrayList;

public class QuenchFlameEffect extends SpellEffect {
    public static final MapCodec<QuenchFlameEffect> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group( //
            Identifier.CODEC.fieldOf("utterance").forGetter(effect -> effect.utterance), //
            Codecs.POSITIVE_INT.fieldOf("duration").forGetter(effect -> effect.duration), //
            Codec.INT.fieldOf("max_range").forGetter(effect -> effect.maxRange), //
            TagKey.unprefixedCodec(RegistryKeys.BLOCK).fieldOf("tag").forGetter(effect -> effect.tag) //
    ).apply(instance, QuenchFlameEffect::new));

    public final int maxRange;
    public final TagKey<Block> tag;

    public QuenchFlameEffect(Identifier utterance, int duration, int maxRange, TagKey<Block> tag) {
        super(utterance, duration);
        this.maxRange = maxRange;
        this.tag = tag;
    }

    @Override
    public MapCodec<? extends SpellEffect> getCodec() {
        return CODEC;
    }

    @Override
    public void finish(SpellEffectContext input, RegistryWrapper.WrapperLookup lookup) {
//        boolean hasTargets = false;
        var targets = new ArrayList<BlockPos>();
        boolean hasSuccess = false;
        boolean hasFailure = false;

        var world = input.getWorld();
        var center = input.getPos();
        for (BlockPos pos : BlockPos.iterate(center.add(-this.maxRange, -this.maxRange, -this.maxRange), center.add(this.maxRange, this.maxRange, this.maxRange))) {
            BlockState state = world.getBlockState(pos);
            if (state.isIn(this.tag) && state.getProperties().contains(Properties.LIT)) {
                targets.add(pos.toImmutable());
                if (input.trySetBlockState(pos, state.with(Properties.LIT, false))) {
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
