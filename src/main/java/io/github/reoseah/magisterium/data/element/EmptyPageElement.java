package io.github.reoseah.magisterium.data.element;

import com.mojang.serialization.MapCodec;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.Drawable;

public class EmptyPageElement implements NormalPageElement {
    public static final EmptyPageElement INSTANCE = new EmptyPageElement();
    public static final MapCodec<EmptyPageElement> CODEC = MapCodec.unit(INSTANCE);

    @Override
    public int getHeight(int width, int pageHeight, TextRenderer textRenderer) {
        return 0;
    }

    @Override
    public Drawable createWidget(int x, int y, BookProperties properties, int maxHeight, TextRenderer textRenderer) {
        return (context, mouseX, mouseY, delta) -> {
            // Do nothing
        };
    }

    @Override
    public MapCodec<? extends PageElement> getCodec() {
        return CODEC;
    }
}
