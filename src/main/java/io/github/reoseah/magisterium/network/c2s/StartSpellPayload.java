package io.github.reoseah.magisterium.network.c2s;

import io.github.reoseah.magisterium.screen.SpellBookScreenHandler;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

public record StartSpellPayload(Identifier id) implements CustomPayload {
    public static final CustomPayload.Id<StartSpellPayload> ID = new CustomPayload.Id<>(Identifier.of("magisterium:start_spell"));
    public static final PacketCodec<PacketByteBuf, StartSpellPayload> CODEC = CustomPayload.codecOf(StartSpellPayload::write, StartSpellPayload::read);

    public static StartSpellPayload read(PacketByteBuf buf) {
        return new StartSpellPayload(Identifier.PACKET_CODEC.decode(buf));
    }

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }

    private void write(PacketByteBuf buf) {
        Identifier.PACKET_CODEC.encode(buf, this.id);
    }

    public static void receive(StartSpellPayload payload, ServerPlayNetworking.Context context) {
        if (context.player().currentScreenHandler instanceof SpellBookScreenHandler handler) {
            handler.startUtterance(payload.id(), context.player());
        }
    }
}
