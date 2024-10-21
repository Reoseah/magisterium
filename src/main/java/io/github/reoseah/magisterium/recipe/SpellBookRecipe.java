package io.github.reoseah.magisterium.recipe;

import io.github.reoseah.magisterium.screen.SpellBookRecipeInput;
import net.minecraft.recipe.Recipe;
import net.minecraft.recipe.RecipeType;
import net.minecraft.util.Identifier;

public abstract class SpellBookRecipe implements Recipe<SpellBookRecipeInput> {
    public static final RecipeType<SpellBookRecipe> TYPE = new RecipeType<>() {
        @Override
        public String toString() {
            return "magisterium:spell_book";
        }
    };

    // TODO: remove these if spells are server-side?
    //       currently, these have to match spell data on the client
    public final Identifier utterance;
    public final int duration;

    protected SpellBookRecipe(Identifier utterance, int duration) {
        this.utterance = utterance;
        this.duration = duration;
    }

    @Override
    public RecipeType<?> getType() {
        return TYPE;
    }

    @Override
    public boolean fits(int width, int height) {
        return true;
    }
}
