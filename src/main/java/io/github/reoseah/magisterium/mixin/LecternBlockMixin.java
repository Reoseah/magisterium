package io.github.reoseah.magisterium.mixin;

import io.github.reoseah.magisterium.block.MagisteriumProperties;
import io.github.reoseah.magisterium.item.SpellBookItem;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.LecternBlock;
import net.minecraft.block.entity.LecternBlockEntity;
import net.minecraft.entity.Entity;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LecternBlock.class)
public class LecternBlockMixin extends Block {
    @Unique
    private static final BooleanProperty HOLDS_SPELL_BOOK = MagisteriumProperties.HOLDS_SPELL_BOOK;

    public LecternBlockMixin(Settings settings) {
        super(settings);
    }

    @Inject(at = @At("RETURN"), method = "<init>")
    protected void init(AbstractBlock.Settings settings, CallbackInfo ci) {
        this.setDefaultState(this.getDefaultState().with(HOLDS_SPELL_BOOK, false));
    }

    @Inject(at = @At("HEAD"), method = "appendProperties")
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder, CallbackInfo ci) {
        builder.add(HOLDS_SPELL_BOOK);
    }

    @Inject(at = @At("RETURN"), method = "setHasBook")
    private static void setHasBook(Entity user, World world, BlockPos pos, BlockState state, boolean hasBook, CallbackInfo ci) {
        if (world.getBlockEntity(pos) instanceof LecternBlockEntity be) {
            world.setBlockState(pos, world.getBlockState(pos) //
                    .with(HOLDS_SPELL_BOOK, be.getBook().isOf(SpellBookItem.INSTANCE)));
        }
    }
}