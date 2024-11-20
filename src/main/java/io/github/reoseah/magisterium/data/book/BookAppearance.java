package io.github.reoseah.magisterium.data.book;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.Identifier;

// TODO: merge with BookProperties, support vanilla-like single page books and current left and right page books
public class BookAppearance {
    public static final MapCodec<BookAppearance> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Identifier.CODEC.fieldOf("texture").forGetter(appearance -> appearance.texture)
    ).apply(instance, BookAppearance::new));

    public final Identifier texture;

    public BookAppearance(Identifier texture) {
        this.texture = texture;
    }
}
