package io.github.reoseah.magisterium.recipe;

import io.github.reoseah.magisterium.block.GlyphBlock;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class GlyphicIgnitionRecipe extends SpellBookRecipe {
    public static final RecipeSerializer<GlyphicIgnitionRecipe> SERIALIZER = new SpellBookRecipe.SimpleSerializer<>(GlyphicIgnitionRecipe::new);

    public static final int RADIUS = 16;

    protected GlyphicIgnitionRecipe(Identifier utterance, int duration) {
        super(utterance, duration);
    }

    @Override
    public boolean matches(SpellBookRecipeInput input, World world) {
        return true;
    }

    @Override
    public ItemStack craft(SpellBookRecipeInput input, RegistryWrapper.WrapperLookup lookup) {
        boolean hasTargets = false;
        boolean hasLit = false;
        boolean hasFailed = false;

        World world = input.player.getWorld();
        BlockPos center = input.player.getBlockPos();
        for (BlockPos pos : BlockPos.iterate(center.add(-RADIUS, -RADIUS, -RADIUS), center.add(RADIUS, RADIUS, RADIUS))) {
            BlockState state = world.getBlockState(pos);
            if (state.isOf(GlyphBlock.INSTANCE)) {
                hasTargets = true;
                if (world.canPlayerModifyAt(input.player, pos)) {
                    world.setBlockState(pos, Blocks.FIRE.getDefaultState());
                    hasLit = true;
                } else {
                    hasFailed = true;
                }
            }
        }

        if (!hasTargets) {
            input.player.sendMessage(Text.translatable("magisterium.gui.no_glyphs_found"), true);
        } else if (hasFailed && hasLit) {
            input.player.sendMessage(Text.translatable("magisterium.gui.partial_success"), true);
        } else if (hasFailed) {
            input.player.sendMessage(Text.translatable("magisterium.gui.no_success"), true);
        }

        return ItemStack.EMPTY;
    }

    @Override
    public ItemStack getResult(RegistryWrapper.WrapperLookup registriesLookup) {
        return ItemStack.EMPTY;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return SERIALIZER;
    }
}
