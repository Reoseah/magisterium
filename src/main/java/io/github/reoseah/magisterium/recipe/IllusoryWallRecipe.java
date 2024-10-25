package io.github.reoseah.magisterium.recipe;

import io.github.reoseah.magisterium.block.GlyphBlock;
import io.github.reoseah.magisterium.block.IllusoryWallBlock;
import io.github.reoseah.magisterium.block.IllusoryWallBlockEntity;
import net.minecraft.block.Blocks;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.apache.commons.lang3.stream.Streams;

import java.util.ArrayDeque;
import java.util.HashSet;

public class IllusoryWallRecipe extends SpellBookRecipe {
    public static final RecipeSerializer<IllusoryWallRecipe> SERIALIZER = new SpellBookRecipe.SimpleSerializer<>(IllusoryWallRecipe::new);

    private static final int GLYPH_SEARCH_RADIUS = 5;
    private static final int MAX_GLYPHS_CONVERTED = 8;
    private static final int MAX_HEIGHT = 3;

    protected IllusoryWallRecipe(Identifier utterance, int duration) {
        super(utterance, duration);
    }

    @Override
    public boolean matches(SpellBookRecipeInput input, World world) {
        return true;
    }

    @Override
    public ItemStack getResult(RegistryWrapper.WrapperLookup registriesLookup) {
        return ItemStack.EMPTY;
    }

    @Override
    public ItemStack craft(SpellBookRecipeInput input, RegistryWrapper.WrapperLookup lookup) {
        var world = input.player.getWorld();
        var playerPos = input.player.getBlockPos();

        var illusionState = Blocks.AIR.getDefaultState();
        var stack = input.getStackInSlot(0);
        if (stack.getItem() instanceof BlockItem blockItem) {
            // TODO maybe try calling getPlacementState for each illusory wall position?
            illusionState = blockItem.getBlock().getDefaultState();
        }

        if (illusionState.isAir()) {
            input.player.sendMessage(Text.translatable("magisterium.gui.invalid_illusion_block"), true);
            return ItemStack.EMPTY;
        }

        var startPos = Streams.of(BlockPos.iterateOutwards(playerPos, GLYPH_SEARCH_RADIUS, GLYPH_SEARCH_RADIUS, GLYPH_SEARCH_RADIUS))
                .filter(pos -> world.getBlockState(pos).isOf(GlyphBlock.INSTANCE))
                .findFirst()
                .orElse(null);

        if (startPos == null) {
            input.player.sendMessage(Text.translatable("magisterium.gui.no_glyphs_found"), true);
            return ItemStack.EMPTY;
        }

        var queue = new ArrayDeque<BlockPos>();
        queue.add(startPos);
        var visited = new HashSet<BlockPos>();

        var glyphsConverted = 0;
        while (!queue.isEmpty() && glyphsConverted < MAX_GLYPHS_CONVERTED) {
            var pos = queue.poll();
            if (visited.contains(pos)) {
                continue;
            }
            visited.add(pos);

            var state = world.getBlockState(pos);
            if (state.isOf(GlyphBlock.INSTANCE)) {
                // TODO: record positions changed, send to client to spawn particles
                IllusoryWallBlock.setBlock(world, pos, illusionState);
                for (int dy = 1; dy < MAX_HEIGHT; dy++) {
                    var wallPos = pos.up(dy);
                    if (world.getBlockState(wallPos).isAir()) {
                        IllusoryWallBlock.setBlock(world, wallPos, illusionState);
                    }
                }
                glyphsConverted++;

                for (var offset : BlockPos.iterate(-1, -1, -1, 1, 1, 1)) {
                    var nextPos = pos.add(offset);
                    if (visited.contains(nextPos)) {
                        continue;
                    }
                    queue.add(nextPos);
                }
            }
        }

        return ItemStack.EMPTY;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return SERIALIZER;
    }
}
