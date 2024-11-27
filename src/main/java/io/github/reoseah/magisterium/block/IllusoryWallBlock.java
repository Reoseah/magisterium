package io.github.reoseah.magisterium.block;

import com.mojang.serialization.MapCodec;
import io.github.reoseah.magisterium.block.entity.IllusoryWallBlockEntity;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.BlockWithEntity;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.WorldAccess;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayDeque;
import java.util.HashSet;
import java.util.Optional;

public class IllusoryWallBlock extends BlockWithEntity {
    public static final MapCodec<IllusoryWallBlock> CODEC = createCodec(IllusoryWallBlock::new);
    public static final RegistryKey<Block> KEY = RegistryKey.of(RegistryKeys.BLOCK, Identifier.of("magisterium:illusory_wall"));
    public static final Settings SETTINGS = Settings.create() //
            .nonOpaque() //
            .noCollision() //
            .strength(0.5F) //
            .lootTable(Optional.empty()) //
            .registryKey(KEY);

    public static final Block INSTANCE = new IllusoryWallBlock(SETTINGS);

    protected IllusoryWallBlock(Settings settings) {
        super(settings);
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

        final int maxIterations = 200;

        var queue = new ArrayDeque<BlockPos>();
        for (var nextPos : BlockPos.iterate(pos.add(-1, -1, -1), pos.add(1, 1, 1))) {
            queue.add(nextPos.toImmutable());
        }
        var visited = new HashSet<BlockPos>();

        for (int i = 0; i < maxIterations && !queue.isEmpty(); i++) {
            var currentPos = queue.poll();
            if (visited.contains(currentPos)) {
                continue;
            }
            visited.add(currentPos);

            if (world.getBlockState(currentPos).getBlock() == INSTANCE) {
                // TODO perhaps check that illusion block is the same as the one being broken
                //      in case different illusions are touching each other
                world.setBlockState(currentPos, Blocks.AIR.getDefaultState(), 3);
                world.syncWorldEvent(null, 2001, currentPos, Block.getRawIdFromState(INSTANCE.getDefaultState()));

                for (var nextPos : BlockPos.iterate(currentPos.add(-1, -1, -1), currentPos.add(1, 1, 1))) {
                    if (!visited.contains(nextPos)) {
                        queue.add(nextPos.toImmutable());
                    }
                }
            }
        }
    }
}
