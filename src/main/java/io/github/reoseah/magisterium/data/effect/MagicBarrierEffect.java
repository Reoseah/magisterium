package io.github.reoseah.magisterium.data.effect;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.reoseah.magisterium.block.MagicBarrierBlock;
import io.github.reoseah.magisterium.screen.SpellBookScreenHandler;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.Ingredient;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;
import net.minecraft.util.dynamic.Codecs;
import net.minecraft.util.hit.BlockHitResult;

import java.util.List;

public class MagicBarrierEffect extends SpellEffect {
    public static final MapCodec<MagicBarrierEffect> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group( //
            Codecs.POSITIVE_INT.fieldOf("duration").forGetter(effect -> effect.duration), //
            Codecs.POSITIVE_INT.fieldOf("glyph_search_radius").forGetter(effect -> effect.glyphSearchRadius), //
            Codecs.POSITIVE_INT.fieldOf("max_width").forGetter(effect -> effect.maxWidth), //
            Codecs.POSITIVE_INT.fieldOf("max_height").forGetter(effect -> effect.maxHeight), //
            Ingredient.CODEC.listOf().fieldOf("ingredients").forGetter(effect -> effect.ingredients) //
    ).apply(instance, MagicBarrierEffect::new));

    public final int glyphSearchRadius;
    public final int maxWidth;
    public final int maxHeight;
    public final List<Ingredient> ingredients;

    public MagicBarrierEffect(int duration, int glyphSearchRadius, int maxWidth, int maxHeight, List<Ingredient> ingredients) {
        super(duration);
        this.glyphSearchRadius = glyphSearchRadius;
        this.maxWidth = maxWidth;
        this.maxHeight = maxHeight;
        this.ingredients = ingredients;
    }

    @Override
    public MapCodec<? extends SpellEffect> getCodec() {
        return CODEC;
    }

    @Override
    public void finish(ServerPlayerEntity player, Inventory inventory, SpellBookScreenHandler.Context screenContext) {
        for (int i = 0; i < this.ingredients.size(); i++) {
            var stack = inventory.getStack(i);
            if (!this.ingredients.get(i).test(stack)) {
                player.sendMessage(Text.translatable("magisterium.missing_spell_ingredients"), true);
                player.closeHandledScreen();
                return;
            }
            stack.decrement(1);
        }

        var startPos = SpellEffectUtil.findClosestGlyph(player.getBlockPos(), player.getWorld(), this.glyphSearchRadius);
        var line = SpellEffectUtil.getGlyphLine(startPos, player.getWorld(), this.maxWidth);

        var context = new SpellWorldChangeTracker(player);
        for (var glyph : line) {
            for (int y = 0; y < this.maxHeight; y++) {
                var pos = glyph.up(y);
                var placementCtx = new ItemPlacementContext(player, Hand.MAIN_HAND, ItemStack.EMPTY, BlockHitResult.createMissed(pos.toCenterPos(), player.getHorizontalFacing(), pos));
                var state = MagicBarrierBlock.INSTANCE.getPlacementState(placementCtx);
                context.trySetBlockState(pos, state);
            }
        }
        context.finishWorldChanges(true);
    }
}
