package io.github.reoseah.magisterium.magisterium.page.element;

import com.mojang.serialization.MapCodec;
import io.github.reoseah.magisterium.magisterium.page.BookLayout;
import io.github.reoseah.magisterium.magisterium.page.BookProperties;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.font.TextRenderer;

public class PageBreak implements PageElement {
    public static final PageBreak INSTANCE = new PageBreak();
    public static final MapCodec<PageBreak> CODEC = MapCodec.unit(INSTANCE);

    @Override
    public MapCodec<? extends PageElement> getCodec() {
        return CODEC;
    }

    @Override
    @Environment(EnvType.CLIENT)
    public void visit(BookLayout.Builder builder, BookProperties properties, TextRenderer textRenderer) {
        builder.advancePage();
    }
}
