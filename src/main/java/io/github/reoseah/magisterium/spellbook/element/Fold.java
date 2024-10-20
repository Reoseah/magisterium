package io.github.reoseah.magisterium.spellbook.element;


import io.github.reoseah.magisterium.spellbook.BookLayout;
import io.github.reoseah.magisterium.spellbook.BookProperties;
import net.minecraft.client.font.TextRenderer;

public class Fold implements BookElement {
    private final BookSimpleElement[] left, right;

    public Fold(BookSimpleElement[] left, BookSimpleElement[] right) {
        this.left = left;
        this.right = right;
    }

    @Override
    public void populate(BookLayout.Builder builder, BookProperties properties, TextRenderer textRenderer) {
        if (builder.getCurrentPage() % 2 != 0) {
            builder.advancePage();
        } else if (!builder.isNewPage()) {
            builder.advancePage();
            builder.advancePage();
        }

        builder.allowWrap(false);
        for (BookSimpleElement element : this.left) {
            element.populate(builder, properties, textRenderer);
        }

        builder.allowWrap(true);
        builder.advancePage();

        builder.allowWrap(false);
        for (BookSimpleElement element : this.right) {
            element.populate(builder, properties, textRenderer);
        }

        builder.allowWrap(true);
        builder.advancePage();
    }
}
