package io.github.reoseah.magisterium.block;

import com.mojang.serialization.MapCodec;
import io.github.reoseah.magisterium.MagisteriumSounds;
import io.github.reoseah.magisterium.block.entity.MagicBarrierBlockEntity;
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

public class MagicBarrierBlock extends BlockWithEntity {
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
            .noCollision() //
            .strength(-1, 3600000) //
            .dropsNothing() //
            .luminance(state -> 7));

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
                .with(DOWN, world.getBlockState(pos.down()).isSideSolidFullSquare(world, pos.down(), Direction.UP)) //
                .with(UP, world.getBlockState(pos.up()).isSideSolidFullSquare(world, pos.up(), Direction.DOWN)) //
                .with(NORTH, world.getBlockState(pos.north()).isSideSolidFullSquare(world, pos.north(), Direction.SOUTH)) //
                .with(EAST, world.getBlockState(pos.east()).isSideSolidFullSquare(world, pos.east(), Direction.WEST)) //
                .with(SOUTH, world.getBlockState(pos.south()).isSideSolidFullSquare(world, pos.south(), Direction.NORTH)) //
                .with(WEST, world.getBlockState(pos.west()).isSideSolidFullSquare(world, pos.west(), Direction.EAST));
    }

    @Override
    protected BlockState getStateForNeighborUpdate(BlockState state, WorldView world, ScheduledTickView tickView, BlockPos pos, Direction direction, BlockPos neighborPos, BlockState neighborState, Random random) {
        return state.with(ConnectingBlock.FACING_PROPERTIES.get(direction), neighborState.isSideSolidFullSquare(world, neighborPos, direction.getOpposite()));
    }

    @Override
    public void randomDisplayTick(BlockState state, World world, BlockPos pos, Random random) {
        if (random.nextInt(50) == 0) {
            world.playSoundAtBlockCenter(pos, MagisteriumSounds.MAGIC_HUM, SoundCategory.BLOCKS, 0.1F, .5F + random.nextFloat() * .5F, true);
        }

        for (int i = 0; i < 1; i++) {
            double x = pos.getX() + random.nextFloat();
            double y = pos.getY() + random.nextFloat();
            double z = pos.getZ() + random.nextFloat();

            world.addParticle(MagisteriumParticles.BARRIER_ENERGY, x, y, z, 0, 0, 0);
        }

        if (state.get(DOWN) && random.nextInt(2) == 0) {
            double y = pos.getY() + .5F * random.nextFloat() * random.nextFloat();
            double x = pos.getX() + random.nextFloat();
            double z = pos.getZ() + random.nextFloat();

            world.addParticle(MagisteriumParticles.BARRIER_SPARK, x, y, z, 0, 0, 0);
        }
        if (state.get(UP) && random.nextInt(2) == 0) {
            double y = pos.getY() + 1 - .5F * random.nextFloat() * random.nextFloat();
            double x = pos.getX() + random.nextFloat();
            double z = pos.getZ() + random.nextFloat();

            world.addParticle(MagisteriumParticles.BARRIER_SPARK, x, y, z, 0, 0, 0);
        }
        if (state.get(NORTH) && random.nextInt(2) == 0) {
            double z = pos.getZ() + .5F * random.nextFloat() * random.nextFloat();
            double x = pos.getX() + random.nextFloat();
            double y = pos.getY() + random.nextFloat();

            world.addParticle(MagisteriumParticles.BARRIER_SPARK, x, y, z, 0, 0, 0);
        }
        if (state.get(EAST) && random.nextInt(2) == 0) {
            double x = pos.getX() + 1 - .5F * random.nextFloat() * random.nextFloat();
            double z = pos.getZ() + random.nextFloat();
            double y = pos.getY() + random.nextFloat();

            world.addParticle(MagisteriumParticles.BARRIER_SPARK, x, y, z, 0, 0, 0);
        }
        if (state.get(SOUTH) && random.nextInt(2) == 0) {
            double z = pos.getZ() + 1 - .5F * random.nextFloat() * random.nextFloat();
            double x = pos.getX() + random.nextFloat();
            double y = pos.getY() + random.nextFloat();

            world.addParticle(MagisteriumParticles.BARRIER_SPARK, x, y, z, 0, 0, 0);
        }
        if (state.get(WEST) && random.nextInt(2) == 0) {
            double x = pos.getX() + .5F * random.nextFloat() * random.nextFloat();
            double z = pos.getZ() + random.nextFloat();
            double y = pos.getY() + random.nextFloat();

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
    protected VoxelShape getCollisionShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        if (world.getBlockEntity(pos) instanceof MagicBarrierBlockEntity be
                && context instanceof EntityShapeContext entityCtx
                && entityCtx.getEntity() != null
                && entityCtx.getEntity().getUuid().equals(be.getCaster())) {
            return VoxelShapes.empty();
        }
        return VoxelShapes.fullCube();
    }
}
