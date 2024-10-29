package io.github.reoseah.magisterium.recipe;

import io.github.reoseah.magisterium.item.SpellBookItem;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.util.Identifier;
import net.minecraft.util.Unit;
import net.minecraft.world.World;

public class UnstableChargeRecipe extends SpellBookRecipe {
    public static final RecipeSerializer<UnstableChargeRecipe> SERIALIZER = new SpellBookRecipe.SimpleSerializer<>(UnstableChargeRecipe::new);

    protected UnstableChargeRecipe(Identifier utterance, int duration) {
        super(utterance, duration);
    }

    @Override
    public boolean matches(SpellBookRecipeInput input, World world) {
        // TODO: require ingredients, like a Nether Wart and a Bottle o' Enchanting
        return true;
    }

    @Override
    public ItemStack craft(SpellBookRecipeInput input, RegistryWrapper.WrapperLookup lookup) {
        input.getContext().setStackComponent(SpellBookItem.UNSTABLE_CHARGE, Unit.INSTANCE);

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
