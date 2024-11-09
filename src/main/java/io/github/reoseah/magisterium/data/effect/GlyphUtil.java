package io.github.reoseah.magisterium.data.effect;

import io.github.reoseah.magisterium.block.GlyphBlock;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

public class GlyphUtil {
    public static List<BlockPos> getGlyphLine(World world, BlockPos startPos, int maxWidth) {
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

    public static @Nullable BlockPos findClosestGlyph(BlockPos playerPos, World world, int maxRange) {
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
