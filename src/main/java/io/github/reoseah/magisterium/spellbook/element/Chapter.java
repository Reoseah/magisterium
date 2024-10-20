package io.github.reoseah.magisterium.spellbook.element;

import io.github.reoseah.magisterium.spellbook.BookLayout;
import io.github.reoseah.magisterium.spellbook.BookProperties;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.text.Text;

// TODO: rename to BookmarkElement
public class Chapter implements BookElement, Bookmark {
    public final String translationKey;

    public Chapter(String translationKey) {
        this.translationKey = translationKey;
    }

    @Override
    public void populate(BookLayout.Builder builder, BookProperties properties, TextRenderer textRenderer) {
        if (builder.getCurrentPage() % 2 != 0) {
            builder.advancePage();
        } else if (!builder.isNewPage()) {
            builder.advancePage();
            builder.advancePage();
        }

        int x = 256 / 2 - properties.bookmarkFullWidth;
        int y = properties.getBookmarkY(builder.getCurrentBookmark());

        builder.addWidget((context, mouseX, mouseY, delta) -> {
            context.drawTexture(properties.texture, x, y, properties.bookmarkFullU, properties.bookmarkFullV, properties.bookmarkFullWidth, properties.bookmarkHeight);
        });
        builder.markBookmark(this);
        builder.advancePage();
    }

    @Override
    public Text getName() {
        return Text.of(this.translationKey);
    }
}