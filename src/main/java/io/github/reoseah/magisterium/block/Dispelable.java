package io.github.reoseah.magisterium.block;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public interface Dispelable {
    void dispel(World world, BlockPos pos, PlayerEntity player);
}
