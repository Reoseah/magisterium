package io.github.reoseah.magisterium.recipe;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.recipe.Recipe;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.recipe.RecipeType;
import net.minecraft.util.Identifier;
import net.minecraft.util.dynamic.Codecs;

import java.util.function.BiFunction;

public abstract class OldSpellBookRecipe implements Recipe<SpellBookRecipeInput> {
    public static final RecipeType<OldSpellBookRecipe> TYPE = new RecipeType<>() {
        @Override
        public String toString() {
            return "magisterium:spell_book";
        }
    };

    // TODO: remove these if spells are server-side?
    //       currently, these have to match spell data on the client
    public final Identifier utterance;
    public final int duration;

    protected OldSpellBookRecipe(Identifier utterance, int duration) {
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

    public static class SimpleSerializer<T extends OldSpellBookRecipe> implements RecipeSerializer<T> {
        private final BiFunction<Identifier, Integer, T> constructor;
        private final MapCodec<T> codec;
        private final PacketCodec<RegistryByteBuf, T> packetCodec;

        public SimpleSerializer(BiFunction<Identifier, Integer, T> constructor) {
            this.constructor = constructor;
            this.codec = RecordCodecBuilder.mapCodec(instance -> instance //
                    .group(Identifier.CODEC.fieldOf("utterance").forGetter(recipe -> recipe.utterance), //
                            Codecs.POSITIVE_INT.fieldOf("duration").forGetter(recipe -> recipe.duration)) //
                    .apply(instance, this.constructor));
            this.packetCodec = new PacketCodec<>() {
                @Override
                public void encode(RegistryByteBuf buf, T value) {
                    buf.writeIdentifier(value.utterance);
                    buf.writeVarInt(value.duration);
                }

                @Override
                public T decode(RegistryByteBuf buf) {
                    Identifier utterance = buf.readIdentifier();
                    int duration = buf.readVarInt();
                    return constructor.apply(utterance, duration);
                }
            };
        }

        @Override
        public MapCodec<T> codec() {
            return this.codec;
        }

        @Override
        public PacketCodec<RegistryByteBuf, T> packetCodec() {
            return this.packetCodec;
        }
    }
}
