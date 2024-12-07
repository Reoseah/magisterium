package io.github.reoseah.magisterium.data;

import io.github.reoseah.magisterium.magisterium.effect.SpellEffect;
import net.fabricmc.fabric.api.resource.IdentifiableResourceReloadListener;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.resource.JsonDataLoader;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;
import net.minecraft.util.profiler.Profiler;

import java.util.Map;

public class SpellEffectLoader extends JsonDataLoader<SpellEffect> implements IdentifiableResourceReloadListener {
    public static final Identifier ID = Identifier.of("magisterium", "spell_effects");

    private static SpellEffectLoader instance;

    public Map<Identifier, SpellEffect> effects;

    public SpellEffectLoader(RegistryWrapper.WrapperLookup registries) {
        super(registries, SpellEffect.CODEC, "magisterium/effects");
        instance = this;
    }

    public static SpellEffectLoader getInstance() {
        return instance;
    }

    @Override
    public Identifier getFabricId() {
        return ID;
    }

    @Override
    protected void apply(Map<Identifier, SpellEffect> prepared, ResourceManager manager, Profiler profiler) {
        this.effects = prepared;
    }
}
