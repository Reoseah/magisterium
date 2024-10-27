package io.github.reoseah.magisterium.block;

import com.mojang.serialization.MapCodec;
import io.github.reoseah.magisterium.world.MagisteriumPlaygrounds;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.BlockWithEntity;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayDeque;
import java.util.HashSet;

public class IllusoryWallBlock extends BlockWithEntity {
    public static final MapCodec<IllusoryWallBlock> CODEC = createCodec(IllusoryWallBlock::new);
    public static final Settings SETTINGS = Settings.create().nonOpaque().noCollision().strength(0.5F);
    public static final Block INSTANCE = new IllusoryWallBlock(SETTINGS);

    protected IllusoryWallBlock(Settings settings) {
        super(settings);
    }

    public static boolean setBlock(World world, BlockPos pos, BlockState illusoryState, PlayerEntity player) {
        if (MagisteriumPlaygrounds.canModifyWorld(world, pos, player)) {

            if (world.setBlockState(pos, INSTANCE.getDefaultState())) {
                var be = world.getBlockEntity(pos);
                ((IllusoryWallBlockEntity) be).setIllusoryState(illusoryState);
                return true;
            }
        }
        return false;
    }

    @Override
    protected MapCodec<? extends BlockWithEntity> getCodec() {
        return CODEC;
    }

    @Override
    public @Nullable BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return new IllusoryWallBlockEntity(pos, state);
    }

    @Override
    public void onBroken(WorldAccess world, BlockPos pos, BlockState state) {
        super.onBroken(world, pos, state);

        final int maxIterations = 100;

        var queue = new ArrayDeque<BlockPos>();
        for (var dir : Direction.values()) {
            queue.add(pos.offset(dir));
        }
        var visited = new HashSet<BlockPos>();

        for (int i = 0; i < maxIterations && !queue.isEmpty(); i++) {
            var currentPos = queue.poll();
            if (visited.contains(currentPos)) {
                continue;
            }
            visited.add(currentPos);

            var currentState = world.getBlockState(currentPos);
            if (currentState.getBlock() == INSTANCE) {
                world.setBlockState(currentPos, Blocks.AIR.getDefaultState(), 3);
                world.syncWorldEvent(null, 2001, currentPos, Block.getRawIdFromState(INSTANCE.getDefaultState()));

                for (var dir : Direction.values()) {
                    queue.add(currentPos.offset(dir));
                }
            }
        }
    }
}
