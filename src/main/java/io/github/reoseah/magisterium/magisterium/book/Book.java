package io.github.reoseah.magisterium.magisterium.book;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;

public class Book {
    public static final MapCodec<Book> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group( //
            Codec.BOOL.fieldOf("support_insertion").forGetter(data -> data.supportInsertion), //
            BookAppearance.CODEC.fieldOf("appearance").forGetter(data -> data.appearance) //
    ).apply(instance, Book::new));
    public static final PacketCodec<RegistryByteBuf, Book> PACKET_CODEC = PacketCodecs.registryCodec(CODEC.codec());

    public final boolean supportInsertion;
    public final BookAppearance appearance;

    public Book(boolean supportInsertion, BookAppearance appearance) {
        this.supportInsertion = supportInsertion;
        this.appearance = appearance;
    }
}
