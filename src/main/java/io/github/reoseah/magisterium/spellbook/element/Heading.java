package io.github.reoseah.magisterium.spellbook.element;


import io.github.reoseah.magisterium.spellbook.BookProperties;
import it.unimi.dsi.fastutil.objects.ObjectIntPair;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.Drawable;
import net.minecraft.text.OrderedText;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.List;

public class Heading extends SimpleBlock {
    protected final Text text;

    public Heading(String translationKey) {
        this(Text.translatable(translationKey));
    }

    public Heading(Text text) {
        this.text = text;
    }

    @Override
    protected int getHeight(int width, TextRenderer textRenderer) {
        return textRenderer.getWrappedLinesHeight(this.text, width);
    }

    @Override
    protected int getTopMargin() {
        return super.getTopMargin() + 2;
    }

    @Override
    protected Drawable createWidget(int x, int y, BookProperties properties, int maxHeight, TextRenderer textRenderer) {
        List<OrderedText> lines = textRenderer.wrapLines(this.text, properties.pageWidth);
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
