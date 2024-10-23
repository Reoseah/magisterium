package io.github.reoseah.magisterium.spellbook.element;

import io.github.reoseah.magisterium.spellbook.BookLayout;
import io.github.reoseah.magisterium.spellbook.BookProperties;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.text.Text;

public class BookmarkElement implements BookElement, Bookmark {
    public final Text text;

    public BookmarkElement(String translationKey) {
        this(Text.translatable(translationKey));
    }

    public BookmarkElement(Text text) {
        this.text = text;
    }

    @Override
    public void visit(BookLayout.Builder builder, BookProperties properties, TextRenderer textRenderer) {
        builder.startNewFold();

        int x = 256 / 2 - properties.bookmarkWidth;
        int y = properties.getBookmarkY(builder.getCurrentBookmark());

        builder.addWidget((context, mouseX, mouseY, delta) -> {
            context.drawTexture(properties.texture, x, y, properties.bookmarkU, properties.bookmarkV, properties.bookmarkWidth, properties.bookmarkHeight);
        });
        builder.markBookmark(this);
        builder.advancePage();
    }

    @Override
    public Text getName() {
        return this.text;
    }
}