package io.github.reoseah.magisterium.block;

import com.mojang.serialization.MapCodec;
import io.github.reoseah.magisterium.MagisteriumSounds;
import io.github.reoseah.magisterium.block.entity.MagicBarrierBlockEntity;
import io.github.reoseah.magisterium.data.effect.SpellWorldChangeTracker;
import io.github.reoseah.magisterium.particle.MagisteriumParticles;
import net.minecraft.block.*;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityTicker;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.sound.SoundCategory;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.random.Random;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.WorldView;
import net.minecraft.world.tick.ScheduledTickView;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayDeque;
import java.util.HashSet;

public class MagicBarrierBlock extends BlockWithEntity implements CustomDispelBehavior {
    public static final BooleanProperty DOWN = ConnectingBlock.DOWN;
    public static final BooleanProperty UP = ConnectingBlock.UP;
    public static final BooleanProperty NORTH = ConnectingBlock.NORTH;
    public static final BooleanProperty EAST = ConnectingBlock.EAST;
    public static final BooleanProperty SOUTH = ConnectingBlock.SOUTH;
    public static final BooleanProperty WEST = ConnectingBlock.WEST;

    public static final MapCodec<MagicBarrierBlock> CODEC = createCodec(MagicBarrierBlock::new);

    public static final Identifier ID = Identifier.of("magisterium", "magic_barrier");
    public static final RegistryKey<Block> KEY = RegistryKey.of(RegistryKeys.BLOCK, ID);

    public static final Block INSTANCE = new MagicBarrierBlock(AbstractBlock.Settings.create() //
            .registryKey(KEY) //
            .nonOpaque() //
            .strength(-1, 3600000) //
            .dropsNothing() //
            .luminance(state -> 7)
            .allowsSpawning(Blocks::never) //
            .solidBlock(Blocks::never) //
            .suffocates(Blocks::never) //
            .blockVision(Blocks::never));

    public MagicBarrierBlock(Settings settings) {
        super(settings);
        this.setDefaultState(this.stateManager.getDefaultState() //
                .with(DOWN, false) //
                .with(UP, false) //
                .with(NORTH, false) //
                .with(EAST, false) //
                .with(SOUTH, false) //
                .with(WEST, false));
    }

    @Override
    protected MapCodec<? extends BlockWithEntity> getCodec() {
        return CODEC;
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        super.appendProperties(builder);
        builder.add(DOWN, UP, NORTH, EAST, SOUTH, WEST);
    }

    @Override
    public @Nullable BlockState getPlacementState(ItemPlacementContext ctx) {
        var world = ctx.getWorld();
        var pos = ctx.getBlockPos();
        return super.getDefaultState() //
                .with(DOWN, !world.getBlockState(pos.down()).isOf(this) && world.getBlockState(pos.down()).isSideSolidFullSquare(world, pos.down(), Direction.UP)) //
                .with(UP, !world.getBlockState(pos.up()).isOf(this) && world.getBlockState(pos.up()).isSideSolidFullSquare(world, pos.up(), Direction.DOWN)) //
                .with(NORTH, !world.getBlockState(pos.north()).isOf(this) && world.getBlockState(pos.north()).isSideSolidFullSquare(world, pos.north(), Direction.SOUTH)) //
                .with(EAST, !world.getBlockState(pos.east()).isOf(this) && world.getBlockState(pos.east()).isSideSolidFullSquare(world, pos.east(), Direction.WEST)) //
                .with(SOUTH, !world.getBlockState(pos.south()).isOf(this) && world.getBlockState(pos.south()).isSideSolidFullSquare(world, pos.south(), Direction.NORTH)) //
                .with(WEST, !world.getBlockState(pos.west()).isOf(this) && world.getBlockState(pos.west()).isSideSolidFullSquare(world, pos.west(), Direction.EAST));
    }

    @Override
    protected BlockState getStateForNeighborUpdate(BlockState state, WorldView world, ScheduledTickView tickView, BlockPos pos, Direction direction, BlockPos neighborPos, BlockState neighborState, Random random) {
        return state.with(ConnectingBlock.FACING_PROPERTIES.get(direction), !neighborState.isOf(this) && neighborState.isSideSolidFullSquare(world, neighborPos, direction.getOpposite()));
    }

    @Override
    protected float getAmbientOcclusionLightLevel(BlockState state, BlockView world, BlockPos pos) {
        return 1;
    }

    @Override
    protected boolean isTransparent(BlockState state) {
        return true;
    }

    @Override
    protected VoxelShape getCameraCollisionShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        return VoxelShapes.empty();
    }

    @Override
    protected VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        return VoxelShapes.fullCube();
    }

    @Override
    protected VoxelShape getCollisionShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        return VoxelShapes.fullCube();
    }

    @Override
    public void randomDisplayTick(BlockState state, World world, BlockPos pos, Random random) {
        if (random.nextInt(50) == 0) {
            world.playSoundAtBlockCenter(pos, MagisteriumSounds.MAGIC_HUM, SoundCategory.BLOCKS, 0.1F, .5F + random.nextFloat() * .5F, true);
        }

        for (int i = 0; i < 1; i++) {
            var x = pos.getX() + random.nextFloat();
            var y = pos.getY() + random.nextFloat();
            var z = pos.getZ() + random.nextFloat();

            world.addParticle(MagisteriumParticles.BARRIER_ENERGY, x, y, z, 0, 0, 0);
        }

        if (state.get(DOWN) && random.nextInt(2) == 0) {
            var y = pos.getY() + .25F * random.nextFloat() * random.nextFloat();
            var x = pos.getX() + random.nextFloat();
            var z = pos.getZ() + random.nextFloat();

            world.addParticle(MagisteriumParticles.BARRIER_SPARK, x, y, z, 0, 0, 0);
        }
        if (state.get(UP) && random.nextInt(2) == 0) {
            var y = pos.getY() + 1 - .25F * random.nextFloat() * random.nextFloat();
            var x = pos.getX() + random.nextFloat();
            var z = pos.getZ() + random.nextFloat();

            world.addParticle(MagisteriumParticles.BARRIER_SPARK, x, y, z, 0, 0, 0);
        }
        if (state.get(NORTH) && random.nextInt(2) == 0) {
            var z = pos.getZ() + .25F * random.nextFloat() * random.nextFloat();
            var x = pos.getX() + random.nextFloat();
            var y = pos.getY() + random.nextFloat();

            world.addParticle(MagisteriumParticles.BARRIER_SPARK, x, y, z, 0, 0, 0);
        }
        if (state.get(EAST) && random.nextInt(2) == 0) {
            var x = pos.getX() + 1 - .25F * random.nextFloat() * random.nextFloat();
            var z = pos.getZ() + random.nextFloat();
            var y = pos.getY() + random.nextFloat();

            world.addParticle(MagisteriumParticles.BARRIER_SPARK, x, y, z, 0, 0, 0);
        }
        if (state.get(SOUTH) && random.nextInt(2) == 0) {
            var z = pos.getZ() + 1 - .25F * random.nextFloat() * random.nextFloat();
            var x = pos.getX() + random.nextFloat();
            var y = pos.getY() + random.nextFloat();

            world.addParticle(MagisteriumParticles.BARRIER_SPARK, x, y, z, 0, 0, 0);
        }
        if (state.get(WEST) && random.nextInt(2) == 0) {
            var x = pos.getX() + .25F * random.nextFloat() * random.nextFloat();
            var z = pos.getZ() + random.nextFloat();
            var y = pos.getY() + random.nextFloat();

            world.addParticle(MagisteriumParticles.BARRIER_SPARK, x, y, z, 0, 0, 0);
        }
    }

    @Override
    public @Nullable <T extends BlockEntity> BlockEntityTicker<T> getTicker(World world, BlockState state, BlockEntityType<T> type) {
        return world.isClient ? BlockWithEntity.validateTicker(type, MagicBarrierBlockEntity.TYPE, MagicBarrierBlockEntity::tickClient) : null;
    }

    @Override
    public @Nullable BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return new MagicBarrierBlockEntity(pos, state);
    }

    @Override
    public void dispel(SpellWorldChangeTracker context, BlockPos start) {
        var queue = new ArrayDeque<BlockPos>();
        queue.add(start);

        var visited = new HashSet<BlockPos>();
        var i = 0;
        while (!queue.isEmpty() && i++ < 1000) {
            var pos = queue.poll();
            if (!visited.add(pos)) {
                continue;
            }
            visited.add(pos.toImmutable());

            if (context.world.getBlockState(pos).isOf(this)) {
                context.trySetBlockState(pos, Blocks.AIR.getDefaultState());

                for (var next : BlockPos.iterate(pos.add(-1, -1, -1), pos.add(1, 1, 1))) {
                    queue.add(next.toImmutable());
                }
            }
        }
    }
}
