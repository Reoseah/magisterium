package io.github.reoseah.magisterium.network.s2c;

import io.github.reoseah.magisterium.magisterium.book.Book;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

import java.util.HashMap;
import java.util.Map;

public record SyncronizeBookDataPayload(Map<Identifier, Book> books) implements CustomPayload {
    public static final CustomPayload.Id<SyncronizeBookDataPayload> ID = new CustomPayload.Id<>(Identifier.of("magisterium:book_data"));
    public static final PacketCodec<RegistryByteBuf, SyncronizeBookDataPayload> CODEC = CustomPayload.codecOf(SyncronizeBookDataPayload::write, SyncronizeBookDataPayload::read);

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }

    public void write(RegistryByteBuf buf) {
        buf.writeVarInt(this.books.size());
        for (Map.Entry<Identifier, Book> entry : this.books.entrySet()) {
            buf.writeIdentifier(entry.getKey());
            Book.PACKET_CODEC.encode(buf, entry.getValue());
        }
    }

    public static SyncronizeBookDataPayload read(RegistryByteBuf buf) {
        int size = buf.readVarInt();
        var books = new HashMap<Identifier, Book>(size);
        for (int i = 0; i < size; i++) {
            var id = buf.readIdentifier();
            var page = Book.PACKET_CODEC.decode(buf);
            books.put(id, page);
        }
        return new SyncronizeBookDataPayload(books);
    }
}
