package io.github.reoseah.magisterium.network.c2s;

import io.github.reoseah.magisterium.screen.SpellBookScreenHandler;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

public enum StopSpellPayload implements CustomPayload {
    INSTANCE;

    public static final CustomPayload.Id<StopSpellPayload> ID = new CustomPayload.Id<>(Identifier.of("magisterium:stop_spell"));
    public static final PacketCodec<PacketByteBuf, StopSpellPayload> CODEC = PacketCodec.unit(INSTANCE);

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }

    public static void receive(StopSpellPayload payload, ServerPlayNetworking.Context context) {
        if (context.player().currentScreenHandler instanceof SpellBookScreenHandler handler) {
            handler.stopUtterance();
        }
    }
}
