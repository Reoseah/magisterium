package io.github.reoseah.magisterium.block;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public interface CustomDispellingHandler {
    boolean dispel(World world, BlockPos pos, PlayerEntity player);
}
