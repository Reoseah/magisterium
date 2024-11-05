package io.github.reoseah.magisterium.data;

import net.fabricmc.fabric.api.resource.IdentifiableResourceReloadListener;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.resource.JsonDataLoader;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;
import net.minecraft.util.profiler.Profiler;

import java.util.Map;

public class SpellPageLoader extends JsonDataLoader<SpellPage> implements IdentifiableResourceReloadListener {
    public static final Identifier ID = Identifier.of("magisterium", "spell_pages");

    private static SpellPageLoader instance;

    public Map<Identifier, SpellPage> pages;

    public SpellPageLoader(RegistryWrapper.WrapperLookup registries) {
        super(registries, SpellPage.CODEC.codec(), "magisterium/pages");
        instance = this;
    }

    public static SpellPageLoader getInstance() {
        return instance;
    }

    @Override
    public Identifier getFabricId() {
        return ID;
    }

    @Override
    protected void apply(Map<Identifier, SpellPage> prepared, ResourceManager manager, Profiler profiler) {
        this.pages = prepared;
    }
}
