package io.github.reoseah.magisterium.block;

import com.mojang.serialization.MapCodec;
import io.github.reoseah.magisterium.block.entity.ArcaneResonatorBlockEntity;
import io.github.reoseah.magisterium.screen.SpellBookScreenHandler;
import net.minecraft.block.*;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.ai.pathing.NavigationType;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.particle.DustParticleEffect;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.random.Random;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.WorldView;
import net.minecraft.world.block.WireOrientation;
import org.jetbrains.annotations.Nullable;

public class ArcaneResonatorBlock extends BlockWithEntity {
    public static final VoxelShape SHAPE = Block.createCuboidShape(0, 0, 0, 16, 2, 16);
    public static final BooleanProperty POWERED = Properties.POWERED;
    public static final MapCodec<ArcaneResonatorBlock> CODEC = createCodec(ArcaneResonatorBlock::new);

    public static final ArcaneResonatorBlock INSTANCE = new ArcaneResonatorBlock(Settings.create() //
            .registryKey(RegistryKey.of(RegistryKeys.BLOCK, Identifier.of("magisterium", "arcane_resonator"))) //
            .breakInstantly() //
            .nonOpaque() //
            .luminance(state -> state.get(POWERED) ? 13 : 0) //
            .strength(0));
    public static final Item ITEM = new BlockItem(INSTANCE, new Item.Settings() //
            .registryKey(RegistryKey.of(RegistryKeys.ITEM, Identifier.of("magisterium", "arcane_resonator"))) //
            .useBlockPrefixedTranslationKey());

    public ArcaneResonatorBlock(Settings settings) {
        super(settings);
        this.setDefaultState(this.getDefaultState().with(POWERED, false));
    }

    @Override
    protected MapCodec<? extends BlockWithEntity> getCodec() {
        return CODEC;
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(POWERED);
    }

    @Override
    protected boolean canPlaceAt(BlockState state, WorldView world, BlockPos pos) {
        var below = pos.down();
        return world.getBlockState(below).isSideSolid(world, below, Direction.UP, SideShapeType.RIGID);
    }

    @Override
    protected void scheduledTick(BlockState state, ServerWorld world, BlockPos pos, Random random) {
        if (state.get(POWERED)) {
            world.setBlockState(pos, state.with(POWERED, false));
            updateNeighbors(world, pos, state);
        }
    }

    @Override
    protected void neighborUpdate(BlockState state, World world, BlockPos pos, Block sourceBlock, @Nullable WireOrientation wireOrientation, boolean notify) {
        if (!state.canPlaceAt(world, pos)) {
            var entity = state.hasBlockEntity() ? world.getBlockEntity(pos) : null;
            dropStacks(state, world, pos, entity);

            world.removeBlock(pos, false);

            for (var direction : Direction.values()) {
                world.updateNeighborsAlways(pos.offset(direction), this);
            }
        }
    }

    @Override
    protected VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        return SHAPE;
    }

    @Override
    protected boolean emitsRedstonePower(BlockState state) {
        return true;
    }

    @Override
    protected BlockRenderType getRenderType(BlockState state) {
        return BlockRenderType.MODEL;
    }

    @Override
    public void randomDisplayTick(BlockState state, World world, BlockPos pos, Random random) {
        if (state.get(POWERED)) {
            for (int i = 0; i < 2; i++) {
                double x = pos.getX() + 0.5 + (random.nextDouble() - 0.5);
                double y = pos.getY() + 0.4 + (random.nextDouble() - 0.5);
                double z = pos.getZ() + 0.5 + (random.nextDouble() - 0.5);

                world.addParticle(DustParticleEffect.DEFAULT, x, y, z, 0, 0, 0);
            }
        }
    }

    @Override
    public @Nullable BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return new ArcaneResonatorBlockEntity(pos, state);
    }

    private static void updateNeighbors(World world, BlockPos pos, BlockState state) {
        world.updateNeighborsAlways(pos, state.getBlock());
    }

    @Override
    protected int getWeakRedstonePower(BlockState state, BlockView world, BlockPos pos, Direction direction) {
        return state.get(POWERED) ? 15 : 0;
    }

    @Override
    public int getStrongRedstonePower(BlockState state, BlockView world, BlockPos pos, Direction direction) {
        return direction == Direction.UP ? state.getWeakRedstonePower(world, pos, direction) : 0;
    }

    @Override
    protected boolean canPathfindThrough(BlockState state, NavigationType type) {
        return false;
    }

    @Override
    protected boolean hasSidedTransparency(BlockState state) {
        return true;
    }

    public void onSpellStart(ServerPlayerEntity player, SpellBookScreenHandler.Context context) {
        var world = player.getWorld();
        var pos = player.getBlockPos();
        for (var ipos : BlockPos.iterate(pos.add(-16, -16, -16), pos.add(16, 16, 16))) {
            if (world.getBlockEntity(ipos) instanceof ArcaneResonatorBlockEntity be) {
                be.users.add(context);
                world.setBlockState(ipos, this.getDefaultState().with(POWERED, true));
            }
        }
    }

    public void onSpellFinish(ServerPlayerEntity player, SpellBookScreenHandler.Context context) {
        var world = player.getWorld();
        var pos = player.getBlockPos();
        for (var ipos : BlockPos.iterate(pos.add(-16, -16, -16), pos.add(16, 16, 16))) {
            if (world.getBlockEntity(ipos) instanceof ArcaneResonatorBlockEntity be) {
                be.users.remove(context);
                if (be.users.isEmpty()) {
                    world.setBlockState(ipos, this.getDefaultState().with(POWERED, false));
                }
            }
        }
    }
}