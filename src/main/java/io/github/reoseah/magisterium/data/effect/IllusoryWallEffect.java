package io.github.reoseah.magisterium.data.effect;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.reoseah.magisterium.block.GlyphBlock;
import io.github.reoseah.magisterium.block.IllusoryWallBlock;
import io.github.reoseah.magisterium.recipe.SpellRecipeInput;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.Blocks;
import net.minecraft.item.BlockItem;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.dynamic.Codecs;
import net.minecraft.util.math.BlockPos;
import org.apache.commons.lang3.stream.Streams;

import java.util.ArrayDeque;
import java.util.HashSet;

public class IllusoryWallEffect extends SpellEffect {
    public static final MapCodec<IllusoryWallEffect> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group( //
            Identifier.CODEC.fieldOf("utterance").forGetter(effect -> effect.utterance), //
            Codecs.POSITIVE_INT.fieldOf("duration").forGetter(effect -> effect.duration), //
            Codecs.POSITIVE_INT.fieldOf("glyph_search_radius").forGetter(effect -> effect.glyphSearchRadius), //
            Codecs.POSITIVE_INT.fieldOf("max_width").forGetter(effect -> effect.maxWidth), //
            Codecs.POSITIVE_INT.fieldOf("max_height").forGetter(effect -> effect.maxHeight) //
    ).apply(instance, IllusoryWallEffect::new));

    public final int glyphSearchRadius;
    public final int maxWidth;
    public final int maxHeight;

    public IllusoryWallEffect(Identifier utterance, int duration, int glyphSearchRadius, int maxWidth, int maxHeight) {
        super(utterance, duration);
        this.glyphSearchRadius = glyphSearchRadius;
        this.maxWidth = maxWidth;
        this.maxHeight = maxHeight;
    }

    @Override
    public MapCodec<? extends SpellEffect> getCodec() {
        return CODEC;
    }

    @Override
    public void finish(SpellRecipeInput input, RegistryWrapper.WrapperLookup lookup) {
        var world = input.player.getWorld();
        var playerPos = input.player.getBlockPos();

        var illusionState = Blocks.AIR.getDefaultState();
        var stack = input.getStackInSlot(0);
        if (stack.getItem() instanceof BlockItem blockItem) {
            // TODO maybe try calling getPlacementState for each illusory wall position?
            illusionState = blockItem.getBlock().getDefaultState();
        }

        if (illusionState.isAir() || illusionState.getRenderType() != BlockRenderType.MODEL) {
            input.player.sendMessage(Text.translatable("magisterium.gui.invalid_illusion_block"), true);
            ((ServerPlayerEntity) input.player).closeHandledScreen();
            return;
        }

        var startPos = Streams.of(BlockPos.iterateOutwards(playerPos, this.glyphSearchRadius, this.glyphSearchRadius, this.glyphSearchRadius))
                .filter(pos -> world.getBlockState(pos).isOf(GlyphBlock.INSTANCE))
                .findFirst()
                .orElse(null);

        if (startPos == null) {
            input.player.sendMessage(Text.translatable("magisterium.gui.no_glyphs_found"), true);
            ((ServerPlayerEntity) input.player).closeHandledScreen();
            return;
        }

        boolean hasSuccess = false, hasFailure = false;

        var queue = new ArrayDeque<BlockPos>();
        queue.add(startPos);
        var visited = new HashSet<BlockPos>();

        var glyphsConverted = 0;
        while (!queue.isEmpty() && glyphsConverted < this.maxWidth) {
            var pos = queue.poll();
            if (visited.contains(pos)) {
                continue;
            }
            visited.add(pos);

            var state = world.getBlockState(pos);
            if (state.isOf(GlyphBlock.INSTANCE)) {
                if (IllusoryWallBlock.setBlock(world, pos, illusionState, input.player)) {
                    hasSuccess = true;
                } else {
                    hasFailure = true;
                }
                for (int dy = 1; dy < this.maxHeight; dy++) {
                    var wallPos = pos.up(dy);
                    if (world.getBlockState(wallPos).isAir()) {
                        if (IllusoryWallBlock.setBlock(world, wallPos, illusionState, input.player)) {
                            hasSuccess = true;
                        } else {
                            hasFailure = true;
                        }
                    }
                }
                glyphsConverted++;

                for (var offset : BlockPos.iterate(-1, -1, -1, 1, 1, 1)) {
                    var nextPos = pos.add(offset);
                    if (visited.contains(nextPos)) {
                        continue;
                    }
                    queue.add(nextPos);
                }
            }
        }
        if (hasFailure && hasSuccess) {
            input.player.sendMessage(Text.translatable("magisterium.gui.partial_success"), true);
        } else if (hasFailure) {
            input.player.sendMessage(Text.translatable("magisterium.gui.no_success"), true);
            ((ServerPlayerEntity) input.player).closeHandledScreen();
        }
    }
}
