package io.github.reoseah.magisterium.data.effect;

import com.mojang.serialization.Codec;
import com.mojang.serialization.Lifecycle;
import com.mojang.serialization.MapCodec;
import io.github.reoseah.magisterium.screen.SpellBookScreenHandler;
import net.minecraft.inventory.Inventory;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.SimpleRegistry;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;

public abstract class SpellEffect {
    public static final RegistryKey<Registry<MapCodec<? extends SpellEffect>>> REGISTRY_KEY = RegistryKey.ofRegistry(Identifier.of("magisterium", "spell_effects"));
    public static final Registry<MapCodec<? extends SpellEffect>> REGISTRY = new SimpleRegistry<>(REGISTRY_KEY, Lifecycle.experimental());
    public static final Codec<SpellEffect> CODEC = REGISTRY.getCodec().dispatch("type", SpellEffect::getCodec, codec -> codec);

    public final int duration;

    public SpellEffect(int duration) {
        this.duration = duration;
    }

    public abstract MapCodec<? extends SpellEffect> getCodec();

    public abstract void finish(ServerPlayerEntity player, Inventory inventory, SpellBookScreenHandler.Context screenContext);
}
