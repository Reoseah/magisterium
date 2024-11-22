package io.github.reoseah.magisterium.data.element;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.font.TextRenderer;

import java.util.Collections;
import java.util.List;

public class Fold implements PageElement {
    public static final MapCodec<Fold> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group( //
            NormalPageElement.CODEC.listOf().fieldOf("left").orElse(Collections.emptyList()).forGetter(fold -> fold.left), //
            NormalPageElement.CODEC.listOf().fieldOf("right").orElse(Collections.emptyList()).forGetter(fold -> fold.right) //
    ).apply(instance, Fold::new));

    private final List<NormalPageElement> left, right;

    public Fold(List<NormalPageElement> left, List<NormalPageElement> right) {
        this.left = left;
        this.right = right;
    }

    @Override
    public MapCodec<? extends PageElement> getCodec() {
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
