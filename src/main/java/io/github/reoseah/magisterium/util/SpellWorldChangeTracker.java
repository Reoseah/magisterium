package io.github.reoseah.magisterium.util;

import io.github.reoseah.magisterium.network.s2c.SpellParticlePayload;
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.block.BlockState;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;

import java.util.ArrayList;
import java.util.List;

public class SpellWorldChangeTracker {
    public static final Text NO_TARGETS_FOUND = Text.translatable("magisterium.no_targets_found");
    public static final Text FAILED_SOME_WORLD_CHANGES = Text.translatable("magisterium.failed_some_world_changes");
    public static final Text FAILED_ALL_WORLD_CHANGES = Text.translatable("magisterium.failed_all_world_changes");

    public final ServerPlayerEntity player;
    public final ServerWorld world;

    protected final List<BlockPos> targets = new ArrayList<>();
    protected boolean hasSucceeded;
    protected boolean hasFailed;

    public SpellWorldChangeTracker(ServerPlayerEntity player) {
        this.player = player;
        this.world = player.getServerWorld();
    }

    public boolean trySetBlockState(BlockPos pos, BlockState state) {
        this.targets.add(pos);
        if (WorldUtil.trySetBlockState(this.world, pos, state, this.player)) {
            this.hasSucceeded = true;
            return true;
        } else {
            this.hasFailed = true;
            return false;
        }
    }

    public void finishWorldChanges(boolean sendParticles) {
        if (sendParticles && !this.targets.isEmpty()) {
            var payload = new SpellParticlePayload(this.targets);
            for (var player : PlayerLookup.tracking(this.player.getServerWorld(), this.player.getBlockPos())) {
                ServerPlayNetworking.send(player, payload);
            }
        }

        if (this.targets.isEmpty()) {
            this.player.sendMessage(NO_TARGETS_FOUND, true);
            this.player.closeHandledScreen();
            return;
        }
        if (this.hasFailed) {
            if (this.hasSucceeded) {
                this.player.sendMessage(FAILED_SOME_WORLD_CHANGES, true);
            } else {
                this.player.sendMessage(FAILED_ALL_WORLD_CHANGES, true);
                this.player.closeHandledScreen();
            }
        }
    }
}
