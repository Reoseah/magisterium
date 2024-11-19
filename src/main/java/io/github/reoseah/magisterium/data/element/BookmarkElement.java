package io.github.reoseah.magisterium.data.element;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.text.Text;
import net.minecraft.text.TextCodecs;

public class BookmarkElement implements PageElement, Bookmark {
    public static final MapCodec<BookmarkElement> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group( //
            TextCodecs.STRINGIFIED_CODEC.fieldOf("text").forGetter(bookmark -> bookmark.text) //
    ).apply(instance, BookmarkElement::new));

    public final Text text;

    public BookmarkElement(String translationKey) {
        this(Text.translatable(translationKey));
    }

    public BookmarkElement(Text text) {
        this.text = text;
    }

    @Override
    public MapCodec<? extends PageElement> getCodec() {
        return CODEC;
    }

    @Override
    @Environment(EnvType.CLIENT)
    public void visit(BookLayout.Builder builder, BookProperties properties, TextRenderer textRenderer) {
        builder.startNewFold();

        int x = 256 / 2 - properties.bookmarkWidth;
        int y = properties.getBookmarkY(builder.getCurrentBookmark());

        builder.addWidget((context, mouseX, mouseY, delta) -> {
            context.drawTexture(RenderLayer::getGuiTextured, properties.texture, x, y, properties.bookmarkU, properties.bookmarkV, properties.bookmarkWidth, properties.bookmarkHeight, 256, 256);
        });
        builder.markBookmark(this);
        builder.advancePage();
    }

    @Override
    public Text getName() {
        return this.text;
    }
}