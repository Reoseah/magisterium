package io.github.reoseah.magisterium.data.effect;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.reoseah.magisterium.block.IllusoryWallBlock;
import io.github.reoseah.magisterium.block.IllusoryWallBlockEntity;
import io.github.reoseah.magisterium.screen.SpellBookScreenHandler;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.Blocks;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.BlockItem;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.dynamic.Codecs;
import net.minecraft.util.math.BlockPos;

public class IllusoryWallEffect extends SpellEffect {
    public static final MapCodec<IllusoryWallEffect> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group( //
            Codecs.POSITIVE_INT.fieldOf("duration").forGetter(effect -> effect.duration), //
            Codecs.POSITIVE_INT.fieldOf("glyph_search_radius").forGetter(effect -> effect.glyphSearchRadius), //
            Codecs.POSITIVE_INT.fieldOf("max_width").forGetter(effect -> effect.maxWidth), //
            Codecs.POSITIVE_INT.fieldOf("max_height").forGetter(effect -> effect.maxHeight) //
    ).apply(instance, IllusoryWallEffect::new));

    public static final MutableText INVALID_ILLUSION_BLOCK = Text.translatable("magisterium.invalid_illusion_block");

    public final int glyphSearchRadius;
    public final int maxWidth;
    public final int maxHeight;

    public IllusoryWallEffect(int duration, int glyphSearchRadius, int maxWidth, int maxHeight) {
        super(duration);
        this.glyphSearchRadius = glyphSearchRadius;
        this.maxWidth = maxWidth;
        this.maxHeight = maxHeight;
    }

    @Override
    public MapCodec<? extends SpellEffect> getCodec() {
        return CODEC;
    }

    @Override
    public void finish(ServerPlayerEntity player, Inventory inventory, SpellBookScreenHandler.Context screenContext) {
        var world = player.getWorld();
        var playerPos = player.getBlockPos();

        var illusionState = Blocks.AIR.getDefaultState();
        var stack = inventory.getStack(0);
        if (stack.getItem() instanceof BlockItem blockItem) {
            // TODO maybe try calling getPlacementState for each illusory wall position?
            illusionState = blockItem.getBlock().getDefaultState();
        }

        if (illusionState.isAir() || illusionState.getRenderType() != BlockRenderType.MODEL) {
            player.sendMessage(INVALID_ILLUSION_BLOCK, true);
            player.closeHandledScreen();
            return;
        }

        BlockPos startPos = SpellEffectUtil.findClosestGlyph(playerPos, world, this.glyphSearchRadius);

        if (startPos == null) {
            player.sendMessage(Text.translatable("magisterium.no_glyphs_found"), true);
            player.closeHandledScreen();
            return;
        }

        var glyphs = SpellEffectUtil.getGlyphLine(startPos, world, this.maxWidth);
        var tracker = new SpellWorldChangeTracker(player);
        for (var glyph : glyphs) {
            for (int dy = 0; dy < this.maxHeight; dy++) {
                var pos = glyph.up(dy);
                if (dy == 0 || world.getBlockState(pos).isAir()) {
                    if (tracker.trySetBlockState(pos, IllusoryWallBlock.INSTANCE.getDefaultState())) {
                        if (player.getWorld().getBlockEntity(pos) instanceof IllusoryWallBlockEntity be) {
                            be.setIllusoryState(illusionState);
                        }
                    }
                }
            }
        }

        tracker.finishWorldChanges(true);
    }
}
