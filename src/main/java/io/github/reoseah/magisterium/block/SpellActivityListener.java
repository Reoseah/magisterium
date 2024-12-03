package io.github.reoseah.magisterium.block;

import io.github.reoseah.magisterium.world.state.ActiveSpellTracker;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;

import java.util.UUID;

public interface SpellActivityListener {
    boolean onSpellStart(ServerWorld world, BlockPos pos, UUID player);

    boolean onSpellEnd(ServerWorld world, BlockPos pos, UUID player);
}
