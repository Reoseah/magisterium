package io.github.reoseah.magisterium.data.element;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.Drawable;
import net.minecraft.text.Text;
import net.minecraft.text.TextCodecs;

public class Heading extends SimpleBlock {
    public static final MapCodec<Heading> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group( //
            TextCodecs.CODEC.fieldOf("text").forGetter(heading -> heading.text) //
    ).apply(instance, Heading::new));

    protected final Text text;

    public Heading(Text text) {
        this.text = text;
    }

    @Override
    public MapCodec<? extends PageElement> getCodec() {
        return CODEC;
    }

    @Override
    @Environment(EnvType.CLIENT)
    protected int getHeight(int width, int pageHeight, TextRenderer textRenderer) {
        return textRenderer.getWrappedLinesHeight(this.text, width);
    }

    @Override
    @Environment(EnvType.CLIENT)
    protected int getTopMargin() {
        return super.getTopMargin() + 2;
    }

    @Override
    @Environment(EnvType.CLIENT)
    protected Drawable createWidget(int x, int y, BookProperties properties, int maxHeight, TextRenderer textRenderer) {
        var lines = textRenderer.wrapLines(this.text, properties.pageWidth);
        var offsets = new IntArrayList(lines.size());
        for (var line : lines) {
            offsets.add(x + (properties.pageWidth - textRenderer.getWidth(line)) / 2);
        }
        return (DrawContext ctx, int mouseX, int mouseY, float delta) -> {
            for (int i = 0; i < lines.size(); i++) {
                ctx.drawText(textRenderer, lines.get(i), offsets.getInt(i), y + i * textRenderer.fontHeight, 0x000000, false);
            }
        };
    }
}
