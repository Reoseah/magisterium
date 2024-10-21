package io.github.reoseah.magisterium.recipe;

import io.github.reoseah.magisterium.Magisterium;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;

public class AwakenFlameRecipe extends SpellBookRecipe {
    public static final RecipeSerializer<SpellBookRecipe> SERIALIZER = new SpellBookRecipe.SimpleSerializer<>(AwakenFlameRecipe::new);

    protected AwakenFlameRecipe(Identifier utterance, int duration) {
        super(utterance, duration);
    }

    @Override
    public boolean matches(SpellBookRecipeInput input, World world) {
        return true;
    }

    @Override
    public ItemStack craft(SpellBookRecipeInput input, RegistryWrapper.WrapperLookup lookup) {
        Magisterium.LOGGER.info("AwakenFlameRecipe.craft");

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
