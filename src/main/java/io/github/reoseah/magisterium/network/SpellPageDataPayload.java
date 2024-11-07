package io.github.reoseah.magisterium.network;

import io.github.reoseah.magisterium.data.SpellPage;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

import java.util.HashMap;
import java.util.Map;

public record SpellPageDataPayload(Map<Identifier, SpellPage> pages) implements CustomPayload {
    public static final CustomPayload.Id<SpellPageDataPayload> ID = new CustomPayload.Id<>(Identifier.of("magisterium:spell_page_data"));
    public static final PacketCodec<RegistryByteBuf, SpellPageDataPayload> CODEC = CustomPayload.codecOf(SpellPageDataPayload::write, SpellPageDataPayload::read);

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

    public static SpellPageDataPayload read(RegistryByteBuf buf) {
        int size = buf.readVarInt();
        var pages = new HashMap<Identifier, SpellPage>(size);
        for (int i = 0; i < size; i++) {
            Identifier id = buf.readIdentifier();
            SpellPage page = SpellPage.PACKET_CODEC.decode(buf);
            pages.put(id, page);
        }
        return new SpellPageDataPayload(pages);
    }

}
