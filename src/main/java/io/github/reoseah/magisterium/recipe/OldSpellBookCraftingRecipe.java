package io.github.reoseah.magisterium.recipe;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
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

public class OldSpellBookCraftingRecipe extends OldSpellBookRecipe {
    public final List<Ingredient> ingredients;
    public final ItemStack result;

    protected OldSpellBookCraftingRecipe(Identifier utterance, int duration, List<Ingredient> ingredients, ItemStack result) {
        super(utterance, duration);
        this.ingredients = ingredients;
        this.result = result;
    }

    @Override
    public boolean matches(SpellRecipeInput input, World world) {
        for (int i = 0; i < this.ingredients.size(); i++) {
            if (!this.ingredients.get(i).test(input.getStackInSlot(i))) {
                return false;
            }
        }
        return true;
    }

    @Override
    public ItemStack craft(SpellRecipeInput input, RegistryWrapper.WrapperLookup lookup) {
        ItemStack result = this.getResult(lookup);

        int count = result.getMaxCount();
        for (int i = 0; i < this.ingredients.size(); i++) {
            ItemStack stack = input.getStackInSlot(i);
            if (!stack.isEmpty()) {
                count = Math.min(count, input.getStackInSlot(i).getCount());
            }
        }
        for (int i = 0; i < this.ingredients.size(); i++) {
            input.removeStack(i, count);
        }
        result = result.copyWithCount(count);

        return result;
    }

    @Override
    public ItemStack getResult(RegistryWrapper.WrapperLookup registriesLookup) {
        return this.result.copy();
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return Serializer.INSTANCE;
    }

    @Override
    public DefaultedList<Ingredient> getIngredients() {
        return DefaultedList.copyOf(Ingredient.EMPTY, this.ingredients.toArray(new Ingredient[0]));
    }

    public static class Serializer implements RecipeSerializer<OldSpellBookCraftingRecipe> {
        public static final MapCodec<OldSpellBookCraftingRecipe> CODEC = RecordCodecBuilder //
                .mapCodec(instance -> instance.group( //
                                Identifier.CODEC.fieldOf("utterance").forGetter(recipe -> recipe.utterance), //
                                Codecs.POSITIVE_INT.fieldOf("duration").forGetter(recipe -> recipe.duration), //
                                Codec.list(Ingredient.DISALLOW_EMPTY_CODEC).fieldOf("ingredients").forGetter(recipe -> recipe.ingredients), //
                                ItemStack.VALIDATED_CODEC.fieldOf("result").forGetter(recipe -> recipe.result)) //
                        .apply(instance, OldSpellBookCraftingRecipe::new));

        public static final PacketCodec<RegistryByteBuf, OldSpellBookCraftingRecipe> PACKET_CODEC = new PacketCodec<>() {
            @Override
            public void encode(RegistryByteBuf buf, OldSpellBookCraftingRecipe value) {
                buf.writeIdentifier(value.utterance);
                buf.writeVarInt(value.duration);
                buf.writeVarInt(value.ingredients.size());
                for (int i = 0; i < value.ingredients.size(); i++) {
                    Ingredient.PACKET_CODEC.encode(buf, value.ingredients.get(i));
                }
                ItemStack.PACKET_CODEC.encode(buf, value.result);
            }

            @Override
            public OldSpellBookCraftingRecipe decode(RegistryByteBuf buf) {
                Identifier utterance = buf.readIdentifier();
                int duration = buf.readVarInt();
                int size = buf.readVarInt();
                DefaultedList<Ingredient> ingredients = DefaultedList.ofSize(size, Ingredient.EMPTY);
                for (int i = 0; i < size; i++) {
                    ingredients.set(i, Ingredient.PACKET_CODEC.decode(buf));
                }
                ItemStack result = ItemStack.PACKET_CODEC.decode(buf);
                return new OldSpellBookCraftingRecipe(utterance, duration, ingredients, result);
            }
        };

        public static final Serializer INSTANCE = new Serializer();

        @Override
        public MapCodec<OldSpellBookCraftingRecipe> codec() {
            return CODEC;
        }

        @Override
        public PacketCodec<RegistryByteBuf, OldSpellBookCraftingRecipe> packetCodec() {
            return PACKET_CODEC;
        }
    }
}
