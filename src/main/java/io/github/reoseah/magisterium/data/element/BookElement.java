package io.github.reoseah.magisterium.data.element;

import com.mojang.serialization.Codec;
import com.mojang.serialization.Lifecycle;
import com.mojang.serialization.MapCodec;
import io.github.reoseah.magisterium.spellbook.BookLayout;
import io.github.reoseah.magisterium.spellbook.BookProperties;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.SimpleRegistry;
import net.minecraft.util.Identifier;

public interface BookElement {
    RegistryKey<Registry<MapCodec<? extends BookElement>>> REGISTRY_KEY = RegistryKey.ofRegistry(Identifier.of("magisterium", "book_elements"));
    Registry<MapCodec<? extends BookElement>> REGISTRY = new SimpleRegistry<>(REGISTRY_KEY, Lifecycle.experimental());
    Codec<BookElement> CODEC = REGISTRY.getCodec().dispatch("type", BookElement::getCodec, codec -> codec);

    MapCodec<? extends BookElement> getCodec();

    void visit(BookLayout.Builder builder, BookProperties properties, TextRenderer textRenderer);
}

