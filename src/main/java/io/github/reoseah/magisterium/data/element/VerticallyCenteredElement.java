package io.github.reoseah.magisterium.data.element;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.Drawable;

public class VerticallyCenteredElement extends SimpleBlock {
    public static final MapCodec<VerticallyCenteredElement> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group( //
            SimpleBlock.CODEC.fieldOf("element").forGetter(element -> element.element) //
    ).apply(instance, VerticallyCenteredElement::new));

    private final SimpleBlock element;

    public VerticallyCenteredElement(SimpleBlock element) {
        this.element = element;
    }

    @Override
    public MapCodec<? extends PageElement> getCodec() {
        return CODEC;
    }

    @Override
    @Environment(EnvType.CLIENT)
    protected int getHeight(int width, TextRenderer textRenderer) {
        // forces new page
        return 10000;
    }

    @Override
    @Environment(EnvType.CLIENT)
    protected Drawable createWidget(int x, int y, BookProperties properties, int maxHeight, TextRenderer textRenderer) {
        int height = this.element.getHeight(properties.pageWidth, textRenderer);
        int newY = y + (maxHeight - height) / 2;
        if (newY > y) {
            newY -= this.element.getTopMargin();
        }
        return this.element.createWidget(x, newY, properties, maxHeight, textRenderer);
    }
}