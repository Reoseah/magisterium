package io.github.reoseah.magisterium.world;

import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class WorldHelper {
    public static boolean canModifyWorld(World world, BlockPos pos, PlayerEntity player) {
        if (world instanceof ServerWorld serverWorld) {
            return player.canModifyBlocks() && player.canModifyAt(serverWorld, pos);
        }
        return player.canModifyBlocks();
    }

    public static boolean trySetBlockState(World world, BlockPos pos, BlockState state, PlayerEntity player) {
        if (canModifyWorld(world, pos, player)) {
            return world.setBlockState(pos, state);
        }
        return false;
    }
}
