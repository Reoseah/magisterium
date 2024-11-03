package io.github.reoseah.magisterium.data.effect;

import com.mojang.serialization.Codec;
import com.mojang.serialization.Lifecycle;
import com.mojang.serialization.MapCodec;
import io.github.reoseah.magisterium.recipe.SpellBookRecipeInput;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.registry.SimpleRegistry;
import net.minecraft.util.Identifier;

public abstract class SpellEffect {
    public static final RegistryKey<Registry<MapCodec<? extends SpellEffect>>> REGISTRY_KEY = RegistryKey.ofRegistry(Identifier.of("magisterium", "spell_effects"));
    public static final Registry<MapCodec<? extends SpellEffect>> REGISTRY = new SimpleRegistry<>(REGISTRY_KEY, Lifecycle.experimental());
    public static final Codec<SpellEffect> CODEC = REGISTRY.getCodec().dispatch("type", SpellEffect::getCodec, codec -> codec);

    public final Identifier utterance;
    public final int duration;

    public SpellEffect(Identifier utterance, int duration) {
        this.utterance = utterance;
        this.duration = duration;
    }

    public abstract MapCodec<? extends SpellEffect> getCodec();

    public abstract void finish(SpellBookRecipeInput input, RegistryWrapper.WrapperLookup lookup);
}
