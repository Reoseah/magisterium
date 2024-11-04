package io.github.reoseah.magisterium.recipe;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.reoseah.magisterium.data.element.BookElement;
import net.minecraft.item.ItemStack;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.recipe.IngredientPlacement;
import net.minecraft.recipe.Recipe;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.recipe.RecipeType;
import net.minecraft.recipe.book.RecipeBookCategory;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.world.World;
import io.github.reoseah.magisterium.data.effect.SpellEffect;

import java.util.List;

public class SpellRecipe implements Recipe<SpellRecipeInput> {
    public static final RecipeType<SpellRecipe> TYPE = new RecipeType<>() {
    };

    public static final MapCodec<SpellRecipe> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group( //
            BookElement.CODEC.listOf().fieldOf("elements").forGetter(SpellRecipe::getElements), //
            SpellEffect.CODEC.listOf().fieldOf("effects").forGetter(SpellRecipe::getEffects) //
    ).apply(instance, SpellRecipe::new));

    public static final PacketCodec<RegistryByteBuf, SpellRecipe> PACKET_CODEC = PacketCodecs.registryCodec(CODEC.codec());

    public static final RecipeSerializer<SpellRecipe> SERIALIZER = new RecipeSerializer<>() {
        @Override
        public MapCodec<SpellRecipe> codec() {
            return CODEC;
        }

        @Override
        public PacketCodec<RegistryByteBuf, SpellRecipe> packetCodec() {
            return PACKET_CODEC;
        }
    };

    public final List<BookElement> elements;
    public final List<SpellEffect> effects;

    public SpellRecipe(List<BookElement> elements, List<SpellEffect> effects) {
        this.elements = elements;
        this.effects = effects;
    }

    public List<BookElement> getElements() {
        return elements;
    }

    public List<SpellEffect> getEffects() {
        return effects;
    }

    @Override
    public boolean matches(SpellRecipeInput input, World world) {
        // TODO implement
        return true;
    }

    @Override
    public ItemStack craft(SpellRecipeInput input, RegistryWrapper.WrapperLookup lookup) {
        return ItemStack.EMPTY;
    }

    @Override
    public RecipeSerializer<? extends Recipe<SpellRecipeInput>> getSerializer() {
        return SERIALIZER;
    }

    @Override
    public RecipeType<? extends Recipe<SpellRecipeInput>> getType() {
        return TYPE;
    }

    @Override
    public IngredientPlacement getIngredientPlacement() {
        return IngredientPlacement.NONE;
    }

    @Override
    public RecipeBookCategory getRecipeBookCategory() {
        return null;
    }
}
