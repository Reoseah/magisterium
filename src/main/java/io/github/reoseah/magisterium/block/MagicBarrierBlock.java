package io.github.reoseah.magisterium.block;

import com.mojang.serialization.MapCodec;
import io.github.reoseah.magisterium.MagisteriumSounds;
import io.github.reoseah.magisterium.particle.MagisteriumParticles;
import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.minecraft.block.*;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityTicker;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.sound.SoundCategory;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;
import net.minecraft.world.WorldView;
import net.minecraft.world.tick.ScheduledTickView;
import org.jetbrains.annotations.Nullable;

public class MagicBarrierBlock extends BlockWithEntity {
    public static final BooleanProperty DOWN = ConnectingBlock.DOWN;
    public static final BooleanProperty UP = ConnectingBlock.UP;
    public static final MapCodec<MagicBarrierBlock> CODEC = createCodec(MagicBarrierBlock::new);
    public static final Identifier ID = Identifier.of("magisterium", "test_block");
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
        this.setDefaultState(this.stateManager.getDefaultState().with(DOWN, false).with(UP, false));
    }

    @Override
    protected MapCodec<? extends BlockWithEntity> getCodec() {
        return CODEC;
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        super.appendProperties(builder);
        builder.add(DOWN, UP);
    }

    @Override
    protected BlockRenderType getRenderType(BlockState state) {
        return BlockRenderType.INVISIBLE;
    }

    @Override
    protected BlockState getStateForNeighborUpdate(BlockState state, WorldView world, ScheduledTickView tickView, BlockPos pos, Direction direction, BlockPos neighborPos, BlockState neighborState, Random random) {
        return state.with(DOWN, world.getBlockState(pos.down()).isSideSolidFullSquare(world, pos.down(), Direction.UP))
                .with(UP, world.getBlockState(pos.up()).isSideSolidFullSquare(world, pos.up(), Direction.DOWN));
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

            var particle = MagisteriumParticles.BARRIER_ENERGY;
            world.addParticle(particle, x, y, z, 0, 0, 0);
        }

        if (state.get(DOWN)) {
            if (random.nextInt(2) == 0) {
                double y = pos.getY() + .5F * random.nextFloat() * random.nextFloat();
                double x = pos.getX() + random.nextFloat();
                double z = pos.getZ() + random.nextFloat();

                var particle = MagisteriumParticles.BARRIER_SPARK;
                world.addParticle(particle, x, y, z, 0, 0, 0);
            }
        }
        if (state.get(UP)) {
            if (random.nextInt(2) == 0) {
                double y = pos.getY() + 1 - .5F * random.nextFloat() * random.nextFloat();
                double x = pos.getX() + random.nextFloat();
                double z = pos.getZ() + random.nextFloat();

                var particle = MagisteriumParticles.BARRIER_SPARK;
                world.addParticle(particle, x, y, z, 0, 0, 0);
            }
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

    public static class MagicBarrierBlockEntity extends BlockEntity {
        public static final BlockEntityType<?> TYPE = FabricBlockEntityTypeBuilder.create(MagicBarrierBlockEntity::new, MagicBarrierBlock.INSTANCE).build();

        public MagicBarrierBlockEntity(BlockPos pos, BlockState state) {
            super(TYPE, pos, state);
        }

        public static void tickClient(World world, BlockPos pos, BlockState state, BlockEntity entity) {
            var seed = Math.abs(hash(pos)) % 32;
            if (world.getTime() % 32 == seed) {
                double x = pos.getX() + .5;
                double y = pos.getY() + .5;
                double z = pos.getZ() + .5;
                var particle = MagisteriumParticles.GLYPHS[world.random.nextInt(MagisteriumParticles.GLYPHS.length)];
                world.addParticle(particle, x, y, z, 0, 0, 0);
            }
        }

        public static int mixBits(int z) {
            z = (z ^ (z >>> 16)) * 0xd36d884b;
            z = (z ^ (z >>> 16)) * 0xd36d884b;
            return z ^ (z >>> 16);
        }

        public static int hash(BlockPos pos) {
            int x = pos.getX();
            int y = pos.getY();
            int z = pos.getZ();
            return mixBits(x) + mixBits(y) * 31 + mixBits(z) * 127;
        }
    }
}
