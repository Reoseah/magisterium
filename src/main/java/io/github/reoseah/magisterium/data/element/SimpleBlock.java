package io.github.reoseah.magisterium.data.element;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import io.github.reoseah.magisterium.spellbook.BookLayout;
import io.github.reoseah.magisterium.spellbook.BookProperties;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.Drawable;

public abstract class SimpleBlock implements BookElement {
    public static final Codec<SimpleBlock> CODEC = BookElement.CODEC.flatXmap(element -> {
        if (element instanceof SimpleBlock simpleBlock) {
            return DataResult.success(simpleBlock);
        }
        return DataResult.error(() -> "Not a SimpleBlock: " + element);
    }, DataResult::success);

    @Override
    @Environment(EnvType.CLIENT)
    public void visit(BookLayout.Builder builder, BookProperties properties, TextRenderer textRenderer) {
        int elementHeight = this.getHeight(properties.pageWidth, textRenderer);

        int elementY = builder.getCurrentY() + (builder.isNewPage() ? 0 : this.getTopMargin());
        if (elementY + elementHeight > builder.getMaxY() && builder.isWrapAllowed() && !builder.isNewPage()) {
            builder.advancePage();
            elementY = builder.getCurrentY();
        }
        int elementX = builder.getCurrentX();
        Drawable renderer = this.createWidget(elementX, elementY, properties, builder.getMaxY() - elementY, textRenderer);
        builder.addWidget(renderer);
        builder.setCurrentY(elementY + elementHeight);
    }

    /**
     * @return number of pixels to offset from the previous element
     */
    @Environment(EnvType.CLIENT)
    protected int getTopMargin() {
        return 4;
    }

    /**
     * @return how many pixels this element wants to take on a page
     */
    @Environment(EnvType.CLIENT)
    protected abstract int getHeight(int width, TextRenderer textRenderer);

    /**
     * Return a renderer for this element.
     * <p>
     * Optionally, implement {@link net.minecraft.client.gui.Element} to also handle mouse events,
     * {@link SlotPropertiesProvider} to configure screen handler slots.
     *
     * @see net.minecraft.client.gui.Element
     * @see SlotPropertiesProvider
     */
    @Environment(EnvType.CLIENT)
    protected abstract Drawable createWidget(int x, int y, BookProperties properties, int maxHeight, TextRenderer textRenderer);
}
