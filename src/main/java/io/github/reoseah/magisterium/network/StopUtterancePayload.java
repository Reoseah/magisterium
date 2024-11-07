package io.github.reoseah.magisterium.network;

import io.github.reoseah.magisterium.screen.SpellBookScreenHandler;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

public record StopUtterancePayload() implements CustomPayload {
    public static final CustomPayload.Id<StopUtterancePayload> ID = new CustomPayload.Id<>(Identifier.of("magisterium:stop_reading"));
    public static final PacketCodec<PacketByteBuf, StopUtterancePayload> CODEC = PacketCodec.unit(new StopUtterancePayload());

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }

    public static void receive(StopUtterancePayload payload, ServerPlayNetworking.Context context) {
        if (context.player().currentScreenHandler instanceof SpellBookScreenHandler handler) {
            handler.stopUtterance();
        }
    }
}
