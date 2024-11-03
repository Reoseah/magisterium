package io.github.reoseah.magisterium.data.element;

import com.mojang.serialization.MapCodec;
import io.github.reoseah.magisterium.spellbook.BookLayout;
import io.github.reoseah.magisterium.spellbook.BookProperties;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.font.TextRenderer;

public class PageBreak implements BookElement {
    public static final PageBreak INSTANCE = new PageBreak();
    public static final MapCodec<PageBreak> CODEC = MapCodec.unit(INSTANCE);

    @Override
    public MapCodec<? extends BookElement> getCodec() {
        return CODEC;
    }

    @Override
    @Environment(EnvType.CLIENT)
    public void visit(BookLayout.Builder builder, BookProperties properties, TextRenderer textRenderer) {
        builder.advancePage();
    }
}
