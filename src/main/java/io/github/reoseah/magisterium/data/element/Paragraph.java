package io.github.reoseah.magisterium.data.element;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.Drawable;
import net.minecraft.text.Text;
import net.minecraft.text.TextCodecs;

public class Paragraph extends DivisibleBlock {
    public static final MapCodec<Paragraph> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group( //
            TextCodecs.CODEC.fieldOf("text").forGetter(paragraph -> paragraph.text) //
    ).apply(instance, Paragraph::new));

    protected final Text text;

    public Paragraph(Text text) {
        this.text = text;
    }

    @Override
    public MapCodec<? extends BookElement> getCodec() {
        return CODEC;
    }

    @Override
    @Environment(EnvType.CLIENT)
    protected int getHeight(int width, TextRenderer textRenderer) {
        return textRenderer.getWrappedLinesHeight(this.text, width);
    }

    @Override
    @Environment(EnvType.CLIENT)
    protected Drawable createWidget(int x, int y, BookProperties properties, int maxHeight, TextRenderer textRenderer) {
        var lines = textRenderer.wrapLines(this.text, properties.pageWidth);
        return (matrices, mouseX, mouseY, delta) -> {
            for (int i = 0; i < lines.size(); i++) {
                matrices.drawText(textRenderer, lines.get(i), x, y + i * textRenderer.fontHeight, 0x000000, false);
            }
        };
    }

    @Override
    @Environment(EnvType.CLIENT)
    protected boolean canDivide(int height, int maxHeight, TextRenderer textRenderer) {
        return maxHeight >= textRenderer.fontHeight;
    }

    @Override
    @Environment(EnvType.CLIENT)
    protected WidgetPair createWidgetPair(int x, int y, int width, int maxHeight, int nextX, int nextY, int nextHeight, TextRenderer textRenderer) {
        var lines = textRenderer.wrapLines(this.text, width);
        int lineCountOnCurrentPage = maxHeight / textRenderer.fontHeight;

        var linesOnCurrentPage = lines.subList(0, lineCountOnCurrentPage);
        var linesOnNextPage = lines.subList(lineCountOnCurrentPage, lines.size());

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
