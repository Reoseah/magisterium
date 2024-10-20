package io.github.reoseah.magisterium.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.block.*;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.DirectionProperty;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import org.jetbrains.annotations.Nullable;

public class ArcaneTableBlock extends BlockWithEntity {
    public static final MapCodec<ArcaneTableBlock> CODEC = createCodec(ArcaneTableBlock::new);
    public static final DirectionProperty FACING = HorizontalFacingBlock.FACING;
    public static final VoxelShape SHAPE = VoxelShapes.union( //
            Block.createCuboidShape(2, 0, 2, 6, 8, 6), //
            Block.createCuboidShape(2, 0, 10, 6, 8, 14), //
            Block.createCuboidShape(10, 0, 2, 14, 8, 6), //
            Block.createCuboidShape(10, 0, 10, 14, 8, 14), //
            Block.createCuboidShape(0, 8, 0, 16, 16, 16) //
    );

    public static final Block INSTANCE = new ArcaneTableBlock(Settings.copy(Blocks.CRAFTING_TABLE).mapColor(MapColor.BLUE));

    public ArcaneTableBlock(Settings settings) {
        super(settings);
    }

    @Override
    protected MapCodec<? extends BlockWithEntity> getCodec() {
        return CODEC;
    }

    @Override
    public @Nullable BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return null;
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        super.appendProperties(builder);
        builder.add(FACING);
    }

    @Override
    protected BlockRenderType getRenderType(BlockState state) {
        return BlockRenderType.MODEL;
    }

    @Override
    protected VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        return SHAPE;
    }

    public BlockState getPlacementState(ItemPlacementContext ctx) {
        return this.getDefaultState().with(FACING, ctx.getHorizontalPlayerFacing().getOpposite());
    }
}
