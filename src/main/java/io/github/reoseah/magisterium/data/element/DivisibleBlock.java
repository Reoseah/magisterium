package io.github.reoseah.magisterium.data.element;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.Drawable;

public abstract class DivisibleBlock extends SimpleBlock {
    @Override
    @Environment(EnvType.CLIENT)
    public void visit(BookLayout.Builder builder, BookProperties properties, TextRenderer textRenderer) {
        int elementY = builder.getCurrentY() + (builder.isNewPage() ? 0 : this.getTopMargin());
        int elementHeight = this.getHeight(properties.pageWidth, properties.pageHeight, textRenderer);

        boolean canDivide = elementY + elementHeight > builder.getMaxY()
                && builder.isWrapAllowed()
                && this.canDivide(elementHeight, builder.getMaxY() - elementY, textRenderer);
        if (!canDivide) {
            super.visit(builder, properties, textRenderer);
            return;
        }

        int elementX = builder.getCurrentX();
        int nextX = builder.getNextX();

        WidgetPair pair = this.createWidgetPair(elementX, elementY, properties.pageWidth, builder.getMaxY() - elementY, nextX, builder.getMinY(), properties.pageHeight, textRenderer);

        builder.addWidget(pair.current());
        builder.advancePage();
        builder.addWidget(pair.next());

        builder.setCurrentY(builder.getMinY() + pair.nextHeight());
    }

    @Environment(EnvType.CLIENT)
    protected abstract boolean canDivide(int height, int maxHeight, TextRenderer textRenderer);

    @Environment(EnvType.CLIENT)
    protected abstract WidgetPair createWidgetPair(int x, int y, int width, int maxHeight, int nextX, int nextY, int nextMaxHeight, TextRenderer textRenderer);

    @Environment(EnvType.CLIENT)
    protected record WidgetPair(Drawable current, Drawable next, int nextHeight) {
    }
}
