package io.github.reoseah.magisterium.magisterium.page.element;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import io.github.reoseah.magisterium.magisterium.page.BookLayout;
import io.github.reoseah.magisterium.magisterium.page.BookProperties;
import io.github.reoseah.magisterium.magisterium.page.SlotPropertiesProvider;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.Drawable;

public interface NormalPageElement extends PageElement {
    Codec<NormalPageElement> CODEC = PageElement.CODEC.flatXmap(element -> {
        if (element instanceof NormalPageElement normalElement) {
            return DataResult.success(normalElement);
        }
        return DataResult.error(() -> "Not a simple page element: " + element);
    }, DataResult::success);

    @Override
    @Environment(EnvType.CLIENT)
    default void visit(BookLayout.Builder builder, BookProperties properties, TextRenderer textRenderer) {
        int elementHeight = this.getHeight(properties.pageWidth, properties.pageHeight, textRenderer);

        int elementY = builder.getCurrentY() + (builder.isNewPage() ? 0 : this.getTopMargin());
        if (elementY + elementHeight > builder.getMaxY() && builder.isWrapAllowed() && !builder.isNewPage()) {
            builder.advancePage();
            elementY = builder.getCurrentY();
        }
        var renderer = this.createWidget(builder.getCurrentX(), elementY, properties, builder.getMaxY() - elementY, textRenderer);
        builder.addWidget(renderer);
        builder.setCurrentY(elementY + elementHeight);
    }

    /**
     * @return number of pixels to offset from the previous element
     */
    @Environment(EnvType.CLIENT)
    default int getTopMargin() {
        return 4;
    }

    /**
     * @return how many pixels this element wants to take on a page
     */
    @Environment(EnvType.CLIENT)
    int getHeight(int width, int pageHeight, TextRenderer textRenderer);

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
    Drawable createWidget(int x, int y, BookProperties properties, int maxHeight, TextRenderer textRenderer);
}
