package io.github.reoseah.magisterium.data.effect;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.reoseah.magisterium.block.GlyphBlock;
import io.github.reoseah.magisterium.block.IllusoryWallBlock;
import io.github.reoseah.magisterium.screen.SpellBookScreenHandler;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.Blocks;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.BlockItem;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.dynamic.Codecs;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

public class IllusoryWallEffect extends SpellEffect {
    public static final MapCodec<IllusoryWallEffect> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group( //
            Identifier.CODEC.fieldOf("utterance").forGetter(effect -> effect.utterance), //
            Codecs.POSITIVE_INT.fieldOf("duration").forGetter(effect -> effect.duration), //
            Codecs.POSITIVE_INT.fieldOf("glyph_search_radius").forGetter(effect -> effect.glyphSearchRadius), //
            Codecs.POSITIVE_INT.fieldOf("max_width").forGetter(effect -> effect.maxWidth), //
            Codecs.POSITIVE_INT.fieldOf("max_height").forGetter(effect -> effect.maxHeight) //
    ).apply(instance, IllusoryWallEffect::new));

    public static final MutableText INVALID_ILLUSION_BLOCK = Text.translatable("magisterium.invalid_illusion_block");

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
    public void finish(ServerPlayerEntity player, Inventory inventory, SpellBookScreenHandler.Context screenContext) {
        var world = player.getWorld();
        var playerPos = player.getBlockPos();

        var illusionState = Blocks.AIR.getDefaultState();
        var stack = inventory.getStack(0);
        if (stack.getItem() instanceof BlockItem blockItem) {
            // TODO maybe try calling getPlacementState for each illusory wall position?
            illusionState = blockItem.getBlock().getDefaultState();
        }

        if (illusionState.isAir() || illusionState.getRenderType() != BlockRenderType.MODEL) {
            player.sendMessage(INVALID_ILLUSION_BLOCK, true);
            player.closeHandledScreen();
            return;
        }

        BlockPos startPos = findClosestGlyph(playerPos, world, this.glyphSearchRadius);

        if (startPos == null) {
            player.sendMessage(Text.translatable("magisterium.no_glyphs_found"), true);
            player.closeHandledScreen();
            return;
        }

        var glyphs = getGlyphLine(world, startPos, this.maxWidth);
        var helper = new SpellWorldChangeTracker(player);
        for (var glyph : glyphs) {
            for (int dy = 0; dy < this.maxHeight; dy++) {
                var pos = glyph.up(dy);
                if (dy == 0 || world.getBlockState(pos).isAir()) {
                    IllusoryWallBlock.setBlock(helper, pos, illusionState);
                }
            }
        }

        helper.finishWorldChanges(true);
    }

    private static List<BlockPos> getGlyphLine(World world, BlockPos startPos, int maxWidth) {
        var list = new ArrayList<BlockPos>();

        var queue = new ArrayDeque<BlockPos>();
        queue.add(startPos);

        var visited = new HashSet<BlockPos>();

        while (!queue.isEmpty() && list.size() < maxWidth) {
            var pos = queue.poll();
            if (visited.contains(pos)) {
                continue;
            }
            visited.add(pos);

            var state = world.getBlockState(pos);
            if (state.isOf(GlyphBlock.INSTANCE)) {
                list.add(pos.toImmutable());
                for (var offset : BlockPos.iterate(-1, -1, -1, 1, 1, 1)) {
                    var nextPos = pos.add(offset);
                    if (visited.contains(nextPos)) {
                        continue;
                    }
                    queue.add(nextPos);
                }
            }
        }
        return list;
    }

    private static @Nullable BlockPos findClosestGlyph(BlockPos playerPos, World world, int maxRange) {
        BlockPos startPos = null;
        for (var pos : BlockPos.iterateOutwards(playerPos, maxRange, maxRange, maxRange)) {
            if (world.getBlockState(pos).isOf(GlyphBlock.INSTANCE)) {
                startPos = pos;
                break;
            }
        }
        return startPos;
    }
}
