package io.github.reoseah.magisterium.data.element;

import com.mojang.serialization.MapCodec;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.Drawable;

public class EmptyPageElement extends SimpleBlock {
    public static final EmptyPageElement INSTANCE = new EmptyPageElement();
    public static final MapCodec<EmptyPageElement> CODEC = MapCodec.unit(INSTANCE);

    @Override
    protected int getHeight(int width, int pageHeight, TextRenderer textRenderer) {
        return 0;
    }

    @Override
    protected Drawable createWidget(int x, int y, BookProperties properties, int maxHeight, TextRenderer textRenderer) {
        return (context, mouseX, mouseY, delta) -> {

        };
    }

    @Override
    public MapCodec<? extends PageElement> getCodec() {
        return CODEC;
    }
}
