package io.github.reoseah.magisterium.spellbook.element;

import io.github.reoseah.magisterium.spellbook.BookLayout;
import io.github.reoseah.magisterium.spellbook.BookProperties;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.Drawable;

// TODO: rename to DividableBlock or something
public abstract class BookSplittableElement extends BookSimpleElement {
    @Override
    public void populate(BookLayout.Builder builder, BookProperties properties, TextRenderer textRenderer) {
        int elementY = builder.getCurrentY() + (builder.isNewPage() ? 0 : this.getVerticalGap());
        int elementHeight = this.getHeight(properties.pageWidth, textRenderer);

        if (elementY + elementHeight > builder.getMaxY()
                && builder.isWrapAllowed()
                && this.canSplit(elementHeight, builder.getMaxY() - elementY, textRenderer)) {
            int elementX = builder.getCurrentX();
            int nextX = builder.getNextX();

            WidgetPair result = this.createWidgetPair(elementX, elementY, properties.pageWidth, builder.getMaxY() - elementY, nextX, builder.getMinY(), properties.pageHeight, textRenderer);

            builder.addWidget(result.current());
            builder.advancePage();
            builder.addWidget(result.next());

            builder.setCurrentY(builder.getMinY() + result.nextHeight());
        } else {
            super.populate(builder, properties, textRenderer);
        }
    }

    protected abstract boolean canSplit(int height, int maxHeight, TextRenderer textRenderer);

    protected abstract WidgetPair createWidgetPair(int x, int y, int width, int maxHeight, int nextX, int nextY, int nextHeight, TextRenderer textRenderer);

    protected record WidgetPair(Drawable current, Drawable next, int nextHeight) {
    }
}
