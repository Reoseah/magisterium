package io.github.reoseah.magisterium.recipe;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.reoseah.magisterium.screen.SpellBookRecipeInput;
import net.minecraft.item.ItemStack;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.recipe.Ingredient;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.util.Identifier;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.dynamic.Codecs;
import net.minecraft.world.World;

import java.util.List;

public class SpellBookCraftingRecipe extends SpellBookRecipe {
    public final List<Ingredient> ingredients;
    public final ItemStack result;

    protected SpellBookCraftingRecipe(Identifier utterance, int duration, List<Ingredient> ingredients, ItemStack result) {
        super(utterance, duration);
        this.ingredients = ingredients;
        this.result = result;
    }

    @Override
    public boolean matches(SpellBookRecipeInput input, World world) {
        return false;
    }

    @Override
    public ItemStack craft(SpellBookRecipeInput input, RegistryWrapper.WrapperLookup lookup) {
        return this.result.copy();
    }

    @Override
    public ItemStack getResult(RegistryWrapper.WrapperLookup registriesLookup) {
        return this.result.copy();
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return Serializer.INSTANCE;
    }

    public static class Serializer implements RecipeSerializer<SpellBookCraftingRecipe> {
        public static final MapCodec<SpellBookCraftingRecipe> CODEC = RecordCodecBuilder //
                .mapCodec(instance -> instance.group( //
                                Identifier.CODEC.fieldOf("utterance").forGetter(recipe -> recipe.utterance), //
                                Codecs.POSITIVE_INT.fieldOf("duration").forGetter(recipe -> recipe.duration), //
                                Codec.list(Ingredient.DISALLOW_EMPTY_CODEC).fieldOf("ingredients").forGetter(recipe -> recipe.ingredients), //
                                ItemStack.VALIDATED_CODEC.fieldOf("result").forGetter(recipe -> recipe.result)
                        ) //
                        .apply(instance, SpellBookCraftingRecipe::new));

        public static final PacketCodec<RegistryByteBuf, SpellBookCraftingRecipe> PACKET_CODEC = new PacketCodec<>() {
            @Override
            public void encode(RegistryByteBuf buf, SpellBookCraftingRecipe value) {
                buf.writeIdentifier(value.utterance);
                buf.writeVarInt(value.duration);
                buf.writeVarInt(value.ingredients.size());
                for (int i = 0; i < value.ingredients.size(); i++) {
                    Ingredient.PACKET_CODEC.encode(buf, value.ingredients.get(i));
                }
                ItemStack.PACKET_CODEC.encode(buf, value.result);
            }

            @Override
            public SpellBookCraftingRecipe decode(RegistryByteBuf buf) {
                Identifier utterance = buf.readIdentifier();
                int duration = buf.readVarInt();
                int size = buf.readVarInt();
                DefaultedList<Ingredient> ingredients = DefaultedList.ofSize(size, Ingredient.EMPTY);
                for (int i = 0; i < size; i++) {
                    ingredients.set(i, Ingredient.PACKET_CODEC.decode(buf));
                }
                ItemStack result = ItemStack.PACKET_CODEC.decode(buf);
                return new SpellBookCraftingRecipe(utterance, duration, ingredients, result);
            }
        };

        public static final Serializer INSTANCE = new Serializer();

        @Override
        public MapCodec<SpellBookCraftingRecipe> codec() {
            return CODEC;
        }

        @Override
        public PacketCodec<RegistryByteBuf, SpellBookCraftingRecipe> packetCodec() {
            return PACKET_CODEC;
        }
    }
}
