package io.github.reoseah.magisterium.network;

import io.github.reoseah.magisterium.data.SpellPage;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

import java.util.HashMap;
import java.util.Map;

public record SyncronizePageDataPayload(Map<Identifier, SpellPage> pages) implements CustomPayload {
    public static final CustomPayload.Id<SyncronizePageDataPayload> ID = new CustomPayload.Id<>(Identifier.of("magisterium:spell_page_data"));
    public static final PacketCodec<RegistryByteBuf, SyncronizePageDataPayload> CODEC = CustomPayload.codecOf(SyncronizePageDataPayload::write, SyncronizePageDataPayload::read);

    @Override
    public CustomPayload.Id<? extends CustomPayload> getId() {
        return ID;
    }

    public void write(RegistryByteBuf buf) {
        buf.writeVarInt(this.pages.size());
        for (Map.Entry<Identifier, SpellPage> entry : this.pages.entrySet()) {
            buf.writeIdentifier(entry.getKey());
            SpellPage.PACKET_CODEC.encode(buf, entry.getValue());
        }
    }

    public static SyncronizePageDataPayload read(RegistryByteBuf buf) {
        int size = buf.readVarInt();
        var pages = new HashMap<Identifier, SpellPage>(size);
        for (int i = 0; i < size; i++) {
            var id = buf.readIdentifier();
            var page = SpellPage.PACKET_CODEC.decode(buf);
            pages.put(id, page);
        }
        return new SyncronizePageDataPayload(pages);
    }
}
