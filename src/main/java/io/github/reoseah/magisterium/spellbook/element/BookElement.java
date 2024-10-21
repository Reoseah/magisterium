package io.github.reoseah.magisterium.spellbook.element;

import io.github.reoseah.magisterium.spellbook.BookLayout;
import io.github.reoseah.magisterium.spellbook.BookProperties;
import net.minecraft.client.font.TextRenderer;

public interface BookElement {
    // TODO: rename to visit or accept
    void populate(BookLayout.Builder builder, BookProperties properties, TextRenderer textRenderer);
}
