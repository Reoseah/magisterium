package io.github.reoseah.magisterium.spellbook.element;

import io.github.reoseah.magisterium.spellbook.BookProperties;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.Drawable;

public class VerticalCenterElement extends SimpleBlock {
    private final SimpleBlock element;

    public VerticalCenterElement(SimpleBlock element) {
        this.element = element;
    }

    @Override
    protected int getHeight(int width, TextRenderer textRenderer) {
        // forces new page
        return 10000;
    }

    @Override
    protected Drawable createWidget(int x, int y, BookProperties properties, int maxHeight, TextRenderer textRenderer) {
        int height = this.element.getHeight(properties.pageWidth, textRenderer);
        int newY = y + (maxHeight - height) / 2;
        if (newY > y) {
            newY -= this.element.getTopMargin();
        }
        return this.element.createWidget(x, newY, properties, maxHeight, textRenderer);
    }
}