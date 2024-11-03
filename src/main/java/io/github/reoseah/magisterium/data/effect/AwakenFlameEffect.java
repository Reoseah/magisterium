package io.github.reoseah.magisterium.data.effect;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.reoseah.magisterium.block.MagisteriumBlockTags;
import io.github.reoseah.magisterium.network.SpellParticlePayload;
import io.github.reoseah.magisterium.recipe.SpellRecipeInput;
import io.github.reoseah.magisterium.world.MagisteriumPlaygrounds;
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
import net.minecraft.world.World;

import java.util.ArrayList;

// TODO make a more general set block property effect
public class AwakenFlameEffect extends SpellEffect {
    public static final MapCodec<AwakenFlameEffect> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group( //
            Identifier.CODEC.fieldOf("utterance").forGetter(effect -> effect.utterance), //
            Codec.INT.fieldOf("duration").forGetter(effect -> effect.duration), //
            Codec.INT.fieldOf("max_range").forGetter(effect -> effect.maxRange), //
            TagKey.unprefixedCodec(RegistryKeys.BLOCK).fieldOf("tag").forGetter(effect -> effect.tag) //
    ).apply(instance, AwakenFlameEffect::new));

    //    public static final int RADIUS = 16;
    public final int maxRange;
    //    public static final TagKey<Block> TAG = MagisteriumBlockTags.AWAKEN_THE_FIRE_TARGETS;
    public final TagKey<Block> tag;

    public AwakenFlameEffect(Identifier utterance, int duration, int maxRange, TagKey<Block> tag) {
        super(utterance, duration);
        this.maxRange = maxRange;
        this.tag = tag;
    }

    @Override
    public MapCodec<? extends SpellEffect> getCodec() {
        return CODEC;
    }

    @Override
    public void finish(SpellRecipeInput input, RegistryWrapper.WrapperLookup lookup) {
        var targets = new ArrayList<BlockPos>();
        boolean hasSuccess = false;
        boolean hasFailure = false;

        World world = input.player.getWorld();
        BlockPos center = input.player.getBlockPos();
        for (BlockPos pos : BlockPos.iterateOutwards(center, this.maxRange, this.maxRange, this.maxRange)) {
            BlockState state = world.getBlockState(pos);
            if (state.isIn(this.tag) && state.getProperties().contains(Properties.LIT)) {
                targets.add(pos.toImmutable());
                if (input.trySetBlockState(pos, state.with(Properties.LIT, true))) {
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
