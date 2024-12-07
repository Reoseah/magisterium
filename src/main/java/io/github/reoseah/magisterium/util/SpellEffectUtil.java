package io.github.reoseah.magisterium.util;

import io.github.reoseah.magisterium.block.GlyphBlock;
import io.github.reoseah.magisterium.item.RuneItem;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockView;
import net.minecraft.world.WorldAccess;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

public class SpellEffectUtil {
    public static List<BlockPos> getGlyphLine(BlockPos from, BlockView world, int maxWidth) {
        var list = new ArrayList<BlockPos>();

        var queue = new ArrayDeque<BlockPos>();
        queue.add(from);

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

    public static @Nullable BlockPos findClosestGlyph(BlockPos from, BlockView world, int range) {
        for (var pos : BlockPos.iterateOutwards(from, range, range, range)) {
            if (world.getBlockState(pos).isOf(GlyphBlock.INSTANCE)) {
                return pos;
            }
        }
        return null;
    }

    public static BlockPos find3x3GlyphCircle(WorldAccess world, BlockPos start, int range) {
        for (var pos : BlockPos.iterateOutwards(start, range, range, range)) {
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

    public static boolean decrementOrDischargeItem(ItemStack stack) {
        if (stack.contains(RuneItem.CHARGE)) {
            var value = stack.get(RuneItem.CHARGE);
            if (value == RuneItem.MAX_CHARGE) {
                stack.set(RuneItem.CHARGE, 0);
                return true;
            }
            return false;
        }
        stack.decrement(1);
        return false;
    }
}
