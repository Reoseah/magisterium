package io.github.reoseah.magisterium.spellbook.element;

import io.github.reoseah.magisterium.spellbook.BookLayout;
import io.github.reoseah.magisterium.spellbook.BookProperties;
import net.minecraft.client.font.TextRenderer;

public class PageBreak implements BookElement {
    @Override
    public void populate(BookLayout.Builder builder, BookProperties properties, TextRenderer textRenderer) {
        builder.advancePage();
    }
}
