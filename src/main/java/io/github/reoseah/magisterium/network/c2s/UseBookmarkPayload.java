package io.github.reoseah.magisterium.network.c2s;

import io.github.reoseah.magisterium.screen.SpellBookScreenHandler;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

public record UseBookmarkPayload(int page) implements CustomPayload {
    public static final CustomPayload.Id<UseBookmarkPayload> ID = new CustomPayload.Id<>(Identifier.of("magisterium:use_bookmark"));
    public static final PacketCodec<PacketByteBuf, UseBookmarkPayload> CODEC = CustomPayload.codecOf(UseBookmarkPayload::write, UseBookmarkPayload::read);

    public static UseBookmarkPayload read(PacketByteBuf buf) {
        return new UseBookmarkPayload(Math.max(0, buf.readVarInt()));
    }

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }

    private void write(PacketByteBuf buf) {
        buf.writeVarInt(this.page);
    }

    public static void receive(UseBookmarkPayload payload, ServerPlayNetworking.Context context) {
        if (context.player().currentScreenHandler instanceof SpellBookScreenHandler handler) {
            handler.currentPage.set(payload.page());
        }
    }
}
