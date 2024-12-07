package io.github.reoseah.magisterium.magisterium.page.element;

import com.mojang.serialization.Codec;
import com.mojang.serialization.Lifecycle;
import com.mojang.serialization.MapCodec;
import io.github.reoseah.magisterium.magisterium.page.BookLayout;
import io.github.reoseah.magisterium.magisterium.page.BookProperties;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.SimpleRegistry;
import net.minecraft.util.Identifier;

public interface PageElement {
    RegistryKey<Registry<MapCodec<? extends PageElement>>> REGISTRY_KEY = RegistryKey.ofRegistry(Identifier.of("magisterium", "page_element"));
    Registry<MapCodec<? extends PageElement>> REGISTRY = new SimpleRegistry<>(REGISTRY_KEY, Lifecycle.experimental());
    Codec<PageElement> CODEC = REGISTRY.getCodec().dispatch("type", PageElement::getCodec, codec -> codec);

    MapCodec<? extends PageElement> getCodec();

    @Environment(EnvType.CLIENT)
    void visit(BookLayout.Builder builder, BookProperties properties, TextRenderer textRenderer);
}

