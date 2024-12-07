package io.github.reoseah.magisterium.block;

import io.github.reoseah.magisterium.util.WorldUtil;
import net.minecraft.block.*;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Identifier;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.random.Random;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.WorldView;
import net.minecraft.world.tick.ScheduledTickView;

public class EnchantedCandlestickBlock extends Block {
    public static final VoxelShape SHAPE = Block.createCuboidShape(6, 0, 6, 10, 16, 10);
    public static final BooleanProperty LIT = Properties.LIT;

    private static final TagKey<Block> AFFECTED_BLOCKS = TagKey.of(RegistryKeys.BLOCK, Identifier.of("magisterium:enchanted_candlestick_affectable"));

    public static final Block INSTANCE = new EnchantedCandlestickBlock(Settings.create() //
            .registryKey(RegistryKey.of(RegistryKeys.BLOCK, Identifier.of("magisterium:candlestick"))) //
            .breakInstantly() //
            .sounds(BlockSoundGroup.LANTERN) //
            .nonOpaque() //
            .luminance(state -> state.get(LIT) ? 13 : 0) //
            .strength(0));
    public static final Item ITEM = new BlockItem(INSTANCE, new Item.Settings() //
            .registryKey(RegistryKey.of(RegistryKeys.ITEM, Identifier.of("magisterium:candlestick"))) //
            .useBlockPrefixedTranslationKey());

    public EnchantedCandlestickBlock(Settings settings) {
        super(settings);
        this.setDefaultState(this.getDefaultState().with(LIT, false));
    }

    @Override
    public void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(LIT);
    }

    @Override
    protected boolean canPlaceAt(BlockState state, WorldView world, BlockPos pos) {
        var below = pos.down();
        return world.getBlockState(below).isSideSolid(world, below, Direction.UP, SideShapeType.CENTER);
    }

    @Override
    protected VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        return SHAPE;
    }

    protected BlockState getStateForNeighborUpdate(BlockState state, WorldView world, ScheduledTickView tickView, BlockPos pos, Direction direction, BlockPos neighborPos, BlockState neighborState, Random random) {
        return direction == Direction.DOWN && !state.canPlaceAt(world, pos) ? Blocks.AIR.getDefaultState() : super.getStateForNeighborUpdate(state, world, tickView, pos, direction, neighborPos, neighborState, random);
    }

    @Override
    protected ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, BlockHitResult hit) {
        boolean lit = state.get(LIT);

        for (var ipos : BlockPos.iterate(pos.add(-16, -16, -16), pos.add(16, 16, 16))) {
            if (WorldUtil.canModifyWorld(world, pos, player)) {
                var istate = world.getBlockState(ipos);
                if (istate.isIn(AFFECTED_BLOCKS) && istate.getProperties().contains(Properties.LIT)) {
                    world.setBlockState(ipos, istate.with(Properties.LIT, !lit));
                }
            }
        }

        return ActionResult.SUCCESS;
    }

    @Override
    public void randomDisplayTick(BlockState state, World world, BlockPos pos, Random random) {
        if (state.get(LIT)) {
            float f = random.nextFloat();
            if (f < 0.3F) {
                world.addParticle(ParticleTypes.SMOKE, pos.getX() + .5, pos.getY() + 1.0625, pos.getZ() + .5, 0.0F, 0.0F, 0.0F);
                if (f < 0.17F) {
                    world.playSound(pos.getX() + 0.5F, pos.getY() + 1F, pos.getZ() + 0.5F, SoundEvents.BLOCK_CANDLE_AMBIENT, SoundCategory.BLOCKS, 1.0F + random.nextFloat(), random.nextFloat() * 0.7F + 0.3F, false);
                }
            }

            world.addParticle(ParticleTypes.SMALL_FLAME, pos.getX() + .5, pos.getY() + 1.0625, pos.getZ() + .5, 0.0F, 0.0F, 0.0F);
        }
    }

}
