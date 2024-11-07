package io.github.reoseah.magisterium.block;

import com.mojang.serialization.MapCodec;
import io.github.reoseah.magisterium.screen.ArcaneTableScreenHandler;
import net.minecraft.block.*;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.enums.NoteBlockInstrument;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.screen.NamedScreenHandlerFactory;
import net.minecraft.screen.ScreenHandlerContext;
import net.minecraft.screen.SimpleNamedScreenHandlerFactory;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.EnumProperty;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Identifier;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public class ArcaneTableBlock extends BlockWithEntity {
    public static final MapCodec<ArcaneTableBlock> CODEC = createCodec(ArcaneTableBlock::new);
    public static final EnumProperty<Direction> FACING = HorizontalFacingBlock.FACING;
    public static final VoxelShape SHAPE = VoxelShapes.union( //
            Block.createCuboidShape(2, 0, 2, 6, 8, 6), //
            Block.createCuboidShape(2, 0, 10, 6, 8, 14), //
            Block.createCuboidShape(10, 0, 2, 14, 8, 6), //
            Block.createCuboidShape(10, 0, 10, 14, 8, 14), //
            Block.createCuboidShape(0, 8, 0, 16, 16, 16) //
    );

    public static final Identifier ID = Identifier.of("magisterium", "arcane_table");
    public static final RegistryKey<Block> KEY = RegistryKey.of(RegistryKeys.BLOCK, ID);
    public static final Block INSTANCE = new ArcaneTableBlock(Settings.create() //
            .registryKey(KEY) //
            .strength(2.5F) //
            .sounds(BlockSoundGroup.WOOD) //
            .instrument(NoteBlockInstrument.BASS) //
            .burnable() //
            .mapColor(MapColor.BLUE));

    public static final RegistryKey<Item> ITEM_KEY = RegistryKey.of(RegistryKeys.ITEM, ID);
    public static final Item ITEM = new BlockItem(INSTANCE, new Item.Settings()//
            .registryKey(ITEM_KEY) //
            // .component(DataComponentTypes.ITEM_NAME, Text.translatable("block.magisterium.arcane_table")) //
            .useBlockPrefixedTranslationKey());


    protected ArcaneTableBlock(Settings settings) {
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

    @Override
    public BlockState getPlacementState(ItemPlacementContext ctx) {
        return this.getDefaultState().with(FACING, ctx.getHorizontalPlayerFacing().getOpposite());
    }

    @Override
    protected ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, BlockHitResult hit) {
        if (!world.isClient) {
            player.openHandledScreen(state.createScreenHandlerFactory(world, pos));
            return ActionResult.CONSUME;
        }
        return ActionResult.SUCCESS;
    }

    @Override
    protected NamedScreenHandlerFactory createScreenHandlerFactory(BlockState state, World world, BlockPos pos) {
        return new SimpleNamedScreenHandlerFactory((syncId, inventory, player) -> ArcaneTableScreenHandler.createServerSide(syncId, inventory, ScreenHandlerContext.create(world, pos)), Text.translatable("container.magisterium.arcane_table"));
    }
}
