package io.github.reoseah.magisterium.network;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

// this could use normal button feature of screen handler,
// but first two buttons ids are taken by page navigation
// and I find it awkward to have to "encode" page into a distinct button id
public record UseBookmarkPayload(int page) implements CustomPayload {
    public static final CustomPayload.Id<UseBookmarkPayload> ID = new CustomPayload.Id<>(Identifier.of("hematurgy:hemonomicon/use_bookmark"));
    public static final PacketCodec<PacketByteBuf, UseBookmarkPayload> CODEC = CustomPayload.codecOf(UseBookmarkPayload::write, UseBookmarkPayload::new);

    public UseBookmarkPayload(PacketByteBuf buf) {
        this(buf.readVarInt());
    }

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }

    private void write(PacketByteBuf buf) {
        buf.writeVarInt(this.page);
    }
}
