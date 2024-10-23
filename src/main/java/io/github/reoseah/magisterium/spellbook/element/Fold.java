package io.github.reoseah.magisterium.spellbook.element;


import io.github.reoseah.magisterium.spellbook.BookLayout;
import io.github.reoseah.magisterium.spellbook.BookProperties;
import net.minecraft.client.font.TextRenderer;

public class Fold implements BookElement {
    private final SimpleBlock[] left, right;

    public Fold(SimpleBlock[] left, SimpleBlock[] right) {
        this.left = left;
        this.right = right;
    }

    @Override
    public void visit(BookLayout.Builder builder, BookProperties properties, TextRenderer textRenderer) {
        builder.startNewFold();
        builder.allowWrap(false);

        for (var element : this.left) {
            element.visit(builder, properties, textRenderer);
        }

        builder.allowWrap(true);
        builder.advancePage();
        builder.allowWrap(false);

        for (var element : this.right) {
            element.visit(builder, properties, textRenderer);
        }

        builder.allowWrap(true);
        builder.advancePage();
    }
}
