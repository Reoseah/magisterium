package io.github.reoseah.magisterium.network;


import io.github.reoseah.magisterium.spellbook.element.SlotConfiguration;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

public record SlotLayoutPayload(SlotConfiguration[] layout) implements CustomPayload {
    public static final CustomPayload.Id<SlotLayoutPayload> ID = new CustomPayload.Id<>(Identifier.of("magisterium:sync_slot_layout"));
    public static final PacketCodec<RegistryByteBuf, SlotLayoutPayload> CODEC = CustomPayload.codecOf(SlotLayoutPayload::write, SlotLayoutPayload::read);

    public void write(RegistryByteBuf buf) {
        buf.writeVarInt(this.layout.length);
        for (SlotConfiguration configuration : this.layout) {
            configuration.write(buf);
        }
    }

    public static SlotLayoutPayload read(RegistryByteBuf buf) {
        var layout = new SlotConfiguration[buf.readVarInt()];
        for (int i = 0; i < layout.length; i++) {
            layout[i] = SlotConfiguration.read(buf);
        }
        return new SlotLayoutPayload(layout);
    }

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}