package io.github.reoseah.magisterium.network.s2c;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

public enum FinishSpellPayload implements CustomPayload {
    INSTANCE;

    public static final CustomPayload.Id<FinishSpellPayload> ID = new CustomPayload.Id<>(Identifier.of("magisterium:finish_spell"));
    public static final PacketCodec<PacketByteBuf, FinishSpellPayload> CODEC = PacketCodec.unit(FinishSpellPayload.INSTANCE);

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}
