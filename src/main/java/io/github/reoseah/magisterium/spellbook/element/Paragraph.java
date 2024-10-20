package io.github.reoseah.magisterium.spellbook.element;

import io.github.reoseah.magisterium.spellbook.BookProperties;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.Drawable;
import net.minecraft.text.MutableText;
import net.minecraft.text.OrderedText;
import net.minecraft.text.Text;

import java.util.List;

public class Paragraph extends BookSplittableElement {
    protected final String translationKey;

    public Paragraph(String translationKey) {
        this.translationKey = translationKey;
    }

    @Override
    protected int getHeight(int width, TextRenderer textRenderer) {
        return textRenderer.getWrappedLinesHeight(Text.translatable(this.translationKey), width);
    }

    @Override
    protected Drawable createWidget(int x, int y, BookProperties properties, int maxHeight, TextRenderer textRenderer) {
        MutableText translated = Text.translatable(this.translationKey);

        List<OrderedText> lines = textRenderer.wrapLines(translated, properties.pageWidth);
        return (matrices, mouseX, mouseY, delta) -> {
            for (int i = 0; i < lines.size(); i++) {
                matrices.drawText(textRenderer, lines.get(i), x, y + i * textRenderer.fontHeight, 0x000000, false);
            }
        };
    }

    @Override
    protected boolean canSplit(int height, int maxHeight, TextRenderer textRenderer) {
        return maxHeight >= textRenderer.fontHeight;
    }

    @Override
    protected WidgetPair createWidgetPair(int x, int y, int width, int maxHeight, int nextX, int nextY, int nextHeight, TextRenderer textRenderer) {
        MutableText translated = Text.translatable(this.translationKey);

        List<OrderedText> lines = textRenderer.wrapLines(translated, width);
        int lineCountOnCurrentPage = maxHeight / textRenderer.fontHeight;

        List<OrderedText> linesOnCurrentPage = lines.subList(0, lineCountOnCurrentPage);
        List<OrderedText> linesOnNextPage = lines.subList(lineCountOnCurrentPage, lines.size());

        return new WidgetPair((ctx, mouseX, mouseY, delta) -> {
            for (int i = 0; i < linesOnCurrentPage.size(); i++) {
                ctx.drawText(textRenderer, linesOnCurrentPage.get(i), x, y + i * textRenderer.fontHeight, 0x000000, false);
            }
        }, (ctx, mouseX, mouseY, delta) -> {
            for (int i = 0; i < linesOnNextPage.size(); i++) {
                ctx.drawText(textRenderer, linesOnNextPage.get(i), nextX, nextY + i * textRenderer.fontHeight, 0x000000, false);
            }
        }, linesOnNextPage.size() * textRenderer.fontHeight);
    }
}
