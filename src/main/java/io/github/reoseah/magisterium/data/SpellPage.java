package io.github.reoseah.magisterium.data;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.reoseah.magisterium.data.element.BookElement;

import java.util.List;

public class SpellPage {
    public static final MapCodec<SpellPage> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group( //
            BookElement.CODEC.listOf().fieldOf("elements").forGetter(page -> page.elements) //
    ).apply(instance, SpellPage::new));

    public final List<BookElement> elements;

    public SpellPage(List<BookElement> elements) {
        this.elements = elements;
    }
}
