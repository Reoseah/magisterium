package io.github.reoseah.magisterium.data.element;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.client.font.TextRenderer;

public class TitlePage implements PageElement {
    public static final MapCodec<TitlePage> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group( //
            SimpleBlock.CODEC.fieldOf("top").orElse(EmptyPageElement.INSTANCE).forGetter(titlePage -> titlePage.top), //
            SimpleBlock.CODEC.fieldOf("center").orElse(EmptyPageElement.INSTANCE).forGetter(titlePage -> titlePage.center), //
            SimpleBlock.CODEC.fieldOf("bottom").orElse(EmptyPageElement.INSTANCE).forGetter(titlePage -> titlePage.bottom) //
    ).apply(instance, TitlePage::new));

    public final SimpleBlock top, center, bottom;

    public TitlePage(SimpleBlock top, SimpleBlock center, SimpleBlock bottom) {
        this.top = top;
        this.center = center;
        this.bottom = bottom;
    }

    @Override
    public MapCodec<? extends PageElement> getCodec() {
        return CODEC;
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
}
