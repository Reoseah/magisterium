package io.github.reoseah.magisterium.spellbook.element;


import io.github.reoseah.magisterium.spellbook.BookProperties;
import it.unimi.dsi.fastutil.objects.ObjectIntPair;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.Drawable;
import net.minecraft.text.MutableText;
import net.minecraft.text.OrderedText;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.List;

public class Heading extends SimpleBlock {
    protected final String translationKey;

    public Heading(String translationKey) {
        this.translationKey = translationKey;
    }

    @Override
    protected int getHeight(int width, TextRenderer textRenderer) {
        return textRenderer.getWrappedLinesHeight(Text.translatable(this.translationKey), width);
    }

    @Override
    protected int getTopMargin() {
        return super.getTopMargin() + 2;
    }

    @Override
    protected Drawable createWidget(int x, int y, BookProperties properties, int maxHeight, TextRenderer textRenderer) {
        MutableText translated = Text.translatable(this.translationKey);
        List<OrderedText> lines = textRenderer.wrapLines(translated, properties.pageWidth);
        List<ObjectIntPair<OrderedText>> centeredLines = new ArrayList<>(lines.size());
        for (OrderedText text : lines) {
            centeredLines.add(ObjectIntPair.of(text, x + (properties.pageWidth - textRenderer.getWidth(text)) / 2));
        }
        return (DrawContext ctx, int mouseX, int mouseY, float delta) -> {
            for (int i = 0; i < centeredLines.size(); i++) {
                ObjectIntPair<OrderedText> line = centeredLines.get(i);
                ctx.drawText(textRenderer, line.left(), line.rightInt(), y + i * textRenderer.fontHeight, 0x000000, false);
            }
        };
    }
}
