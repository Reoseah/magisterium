package io.github.reoseah.magisterium.magisterium.page.element;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.reoseah.magisterium.magisterium.page.BookLayout;
import io.github.reoseah.magisterium.magisterium.page.BookProperties;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.Drawable;

public class TitlePage implements NormalPageElement {
    public static final MapCodec<TitlePage> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group( //
            NormalPageElement.CODEC.fieldOf("top").orElse(EmptyPageElement.INSTANCE).forGetter(titlePage -> titlePage.top), //
            NormalPageElement.CODEC.fieldOf("center").orElse(EmptyPageElement.INSTANCE).forGetter(titlePage -> titlePage.center), //
            NormalPageElement.CODEC.fieldOf("bottom").orElse(EmptyPageElement.INSTANCE).forGetter(titlePage -> titlePage.bottom) //
    ).apply(instance, TitlePage::new));

    public final NormalPageElement top, center, bottom;

    public TitlePage(NormalPageElement top, NormalPageElement center, NormalPageElement bottom) {
        this.top = top;
        this.center = center;
        this.bottom = bottom;
    }

    @Override
    public MapCodec<? extends PageElement> getCodec() {
        return CODEC;
    }

    @Override
    public int getHeight(int width, int pageHeight, TextRenderer textRenderer) {
        return pageHeight;
    }

    @Override
    public void visit(BookLayout.Builder builder, BookProperties properties, TextRenderer textRenderer) {
        if (!builder.isNewPage()) {
            builder.advancePage();
        }

        var topHeight = this.top.getHeight(properties.pageWidth, properties.pageHeight, textRenderer);
        var topDrawable = this.top.createWidget(builder.getCurrentX(), builder.getCurrentY(), properties, properties.pageHeight, textRenderer);

        var bottomHeight = this.bottom.getHeight(properties.pageWidth, properties.pageHeight, textRenderer);
        var bottomDrawable = this.bottom.createWidget(builder.getCurrentX(), builder.getMaxY() - bottomHeight, properties, properties.pageHeight, textRenderer);

        var heightLeft = properties.pageHeight - topHeight - bottomHeight;
        var centerHeight = this.center.getHeight(properties.pageWidth, heightLeft, textRenderer);
        var centerY = builder.getCurrentY() + topHeight + (heightLeft - centerHeight) / 2;
        var centerDrawable = this.center.createWidget(builder.getCurrentX(), centerY, properties, centerHeight, textRenderer);

        builder.addWidget(topDrawable);
        builder.setCurrentY(centerY);
        builder.addWidget(centerDrawable);
        builder.setCurrentY(properties.pageHeight - bottomHeight);
        builder.addWidget(bottomDrawable);
        builder.advancePage();
    }

    @Override
    public Drawable createWidget(int x, int y, BookProperties properties, int maxHeight, TextRenderer textRenderer) {
        var topHeight = this.top.getHeight(properties.pageWidth, properties.pageHeight, textRenderer);
        var topDrawable = this.top.createWidget(x, y, properties, properties.pageHeight, textRenderer);

        var bottomHeight = this.bottom.getHeight(properties.pageWidth, properties.pageHeight, textRenderer);
        var bottomDrawable = this.bottom.createWidget(x, properties.pageY + properties.pageHeight - bottomHeight, properties, properties.pageHeight, textRenderer);

        var heightLeft = properties.pageY + properties.pageHeight - topHeight - bottomHeight;
        var centerHeight = this.center.getHeight(properties.pageWidth, properties.pageHeight, textRenderer);
        var centerY = y + topHeight + (heightLeft - centerHeight) / 2;
        var centerDrawable = this.center.createWidget(x, centerY, properties, centerHeight, textRenderer);

        return (context, mouseX, mouseY, delta) -> {
            topDrawable.render(context, mouseX, mouseY, delta);
            centerDrawable.render(context, mouseX, mouseY, delta);
            bottomDrawable.render(context, mouseX, mouseY, delta);
        };
    }
}
