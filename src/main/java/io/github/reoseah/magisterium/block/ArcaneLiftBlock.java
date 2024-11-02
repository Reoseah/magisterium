package io.github.reoseah.magisterium.block;

import io.github.reoseah.magisterium.MagisteriumSounds;
import io.github.reoseah.magisterium.particle.MagisteriumParticles;
import net.minecraft.block.*;
import net.minecraft.block.piston.PistonBehavior;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.IntProperty;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.random.Random;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;

public class ArcaneLiftBlock extends Block implements Dispelable {
    //    public static final BooleanProperty BASE = BooleanProperty.of("base");
    public static final IntProperty HEIGHT = IntProperty.of("height", 0, 15);

    public static final Block INSTANCE = new ArcaneLiftBlock(Settings.create() //
            .resistance(6000000.0F) //
            .replaceable() //
            .breakInstantly() //
            .noBlockBreakParticles() //
            .nonOpaque() //
            .noCollision() //
            .pistonBehavior(PistonBehavior.DESTROY)//
            .luminance(state -> state.get(HEIGHT) == 0 ? 13 : 8) //
    );

    public ArcaneLiftBlock(Settings settings) {
        super(settings);
        this.setDefaultState(this.getStateManager().getDefaultState().with(HEIGHT, 0));
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        super.appendProperties(builder);
        builder.add(HEIGHT);
    }

    @Override
    protected BlockRenderType getRenderType(BlockState state) {
        return BlockRenderType.INVISIBLE;
    }

    @Override
    protected VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        return VoxelShapes.empty();
    }

    @Override
    public void randomDisplayTick(BlockState state, World world, BlockPos pos, Random random) {
        if (random.nextInt(10) == 0) {
            world.playSoundAtBlockCenter(pos, MagisteriumSounds.ARCANE_LIFT_LOOP, SoundCategory.BLOCKS, 0.15F, 1.0F, true);
        }

        if (random.nextInt(4) == 0) {
            double x = pos.getX() - .5 + random.nextFloat() * 2;
            double y = pos.getY() - .5 + random.nextFloat() * 2;
            double z = pos.getZ() - .5 + random.nextFloat() * 2;
            var particle = MagisteriumParticles.GLYPHS[random.nextInt(MagisteriumParticles.GLYPHS.length)];
            world.addParticle(particle, x, y, z, 0, 0, 0);
        }
        boolean isBase = state.get(HEIGHT) == 0;
        for (int i = 0; i < (isBase ? 3 : 6); i++) {
            double x = pos.getX() - .5 + random.nextFloat() + random.nextFloat();
            double y = pos.getY() - .5 + random.nextFloat() + random.nextFloat();
            double z = pos.getZ() - .5 + random.nextFloat() + random.nextFloat();
            world.addParticle(MagisteriumParticles.ENERGY, x, y, z, //
                    0.01 * random.nextFloat(), 0.05, 0.01 * random.nextFloat());
        }
        if (isBase) {
            for (int i = 0; i < 6; i++) {
                double y = pos.getY() + random.nextFloat() * .5;
                double x, z;
                if (random.nextBoolean()) {
                    x = pos.getX() + (random.nextBoolean() ? -.5 : 1.5);
                    z = pos.getZ() - .5 + 2 * random.nextFloat();
                } else {
                    z = pos.getZ() + (random.nextBoolean() ? -.5 : 1.5);
                    x = pos.getX() - .5 + 2 * random.nextFloat();
                }
                double centerX = pos.getX() + .5;
                double centerZ = pos.getZ() + .5;
                double velocityX = (centerX - x) * 0.03;
                double velocityZ = (centerZ - z) * 0.03;
                world.addParticle(MagisteriumParticles.ENERGY, //
                        x - .25 + random.nextFloat() * .5, y, z - .25 + random.nextFloat() * .5, //
                        velocityX, 0.05, velocityZ);
            }
        }
    }

    @Override
    protected void onEntityCollision(BlockState state, World world, BlockPos pos, Entity entity) {
        var velocity = entity.getVelocity();
        if (entity.isDescending()) {
            double velocityY = Math.max(velocity.y, -0.15);
            entity.setVelocity(velocity.x, velocityY, velocity.z);
        } else {
            double velocityY = MathHelper.clamp(velocity.y + 0.03, 0.25, 0.75);
            entity.setVelocity(velocity.x, velocityY, velocity.z);
        }
        entity.limitFallDistance();
    }

    @Override
    protected BlockState getStateForNeighborUpdate(BlockState state, Direction direction, BlockState neighborState, WorldAccess world, BlockPos pos, BlockPos neighborPos) {
        world.scheduleBlockTick(pos, this, 5);
        return state;
    }

    @Override
    protected void onBlockAdded(BlockState state, World world, BlockPos pos, BlockState oldState, boolean notify) {
        super.onBlockAdded(state, world, pos, oldState, notify);
        world.scheduleBlockTick(pos, this, 5);
    }

    @Override
    protected void scheduledTick(BlockState state, ServerWorld world, BlockPos pos, Random random) {
        if (state.get(HEIGHT) == 0) {
            for (int dx = -1; dx <= 1; dx++) {
                for (int dz = -1; dz <= 1; dz++) {
                    if (dx == 0 && dz == 0) {
                        continue;
                    }
                    var neighborPos = pos.add(dx, 0, dz);
                    var neighborState = world.getBlockState(neighborPos);
                    if (neighborState.getBlock() != GlyphBlock.INSTANCE) {
                        world.removeBlock(pos, false);
                        return;
                    }
                }
            }
        } else {
            var below = pos.down();
            var stateBelow = world.getBlockState(below);
            if (stateBelow.getBlock() != this) {
                world.removeBlock(pos, false);
                return;
            }
        }

        if (state.get(HEIGHT) < 15) {
            var above = pos.up();
            if (world.isAir(above)) {
                world.setBlockState(above, state.with(HEIGHT, state.get(HEIGHT) + 1));
            }
        }
    }

    @Override
    public void dispel(World world, BlockPos pos, PlayerEntity player) {
        var state = world.getBlockState(pos);
        var height = state.get(HEIGHT);
        for (var ipos : BlockPos.iterate(pos, pos.add(0, -height, 0))) {
            if (world.getBlockState(ipos).getBlock() != this) {
                break;
            }
            world.setBlockState(ipos, Blocks.AIR.getDefaultState(), 3);
            world.syncWorldEvent(null, 2001, ipos, Block.getRawIdFromState(state));
        }
        for (var ipos : BlockPos.iterate(pos, pos.add(0, 15 - height, 0))) {
            if (world.getBlockState(ipos).getBlock() != GlyphBlock.INSTANCE) {
                break;
            }
            world.setBlockState(ipos, Blocks.AIR.getDefaultState(), 3);
            world.syncWorldEvent(null, 2001, ipos, Block.getRawIdFromState(state));
        }
    }
}
