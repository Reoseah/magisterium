package io.github.reoseah.magisterium.data.element;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.reoseah.magisterium.spellbook.BookLayout;
import io.github.reoseah.magisterium.spellbook.BookProperties;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.font.TextRenderer;

import java.util.List;

public class Fold implements BookElement {
    public static final MapCodec<Fold> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group( //
            SimpleBlock.CODEC.listOf().fieldOf("left").forGetter(fold -> fold.left), //
            SimpleBlock.CODEC.listOf().fieldOf("right").forGetter(fold -> fold.right) //
    ).apply(instance, Fold::new));

    private final List<SimpleBlock> left, right;

    public Fold(List<SimpleBlock> left, List<SimpleBlock> right) {
        this.left = left;
        this.right = right;
    }

    @Override
    public MapCodec<? extends BookElement> getCodec() {
        return CODEC;
    }

    @Override
    @Environment(EnvType.CLIENT)
    public void visit(BookLayout.Builder builder, BookProperties properties, TextRenderer textRenderer) {
        builder.startNewFold();
        builder.allowWrap(false);

        for (var element : this.left) {
            element.visit(builder, properties, textRenderer);
        }

        builder.allowWrap(true);
        builder.advancePage();
        builder.allowWrap(false);

        for (var element : this.right) {
            element.visit(builder, properties, textRenderer);
        }

        builder.allowWrap(true);
        builder.advancePage();
    }
}
