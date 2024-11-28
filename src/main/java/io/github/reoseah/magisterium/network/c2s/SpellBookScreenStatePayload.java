package io.github.reoseah.magisterium.network.c2s;

import io.github.reoseah.magisterium.data.element.SlotProperties;
import io.github.reoseah.magisterium.screen.SpellBookScreenHandler;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

public record SpellBookScreenStatePayload(SlotProperties[] slots) implements CustomPayload {
    public static final CustomPayload.Id<SpellBookScreenStatePayload> ID = new CustomPayload.Id<>(Identifier.of("magisterium:spell_book_screen_state"));
    public static final PacketCodec<RegistryByteBuf, SpellBookScreenStatePayload> CODEC = CustomPayload.codecOf(SpellBookScreenStatePayload::write, SpellBookScreenStatePayload::read);

    public static SpellBookScreenStatePayload read(RegistryByteBuf buf) {
        var layout = new SlotProperties[buf.readVarInt()];
        for (int i = 0; i < layout.length; i++) {
            layout[i] = SlotProperties.read(buf);
        }
        return new SpellBookScreenStatePayload(layout);
    }

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }

    public void write(RegistryByteBuf buf) {
        buf.writeVarInt(this.slots.length);
        for (SlotProperties configuration : this.slots) {
            configuration.write(buf);
        }
    }

    public static void receive(SpellBookScreenStatePayload payload, ServerPlayNetworking.Context context) {
        if (context.player().currentScreenHandler instanceof SpellBookScreenHandler handler) {
            handler.applySlotProperties(payload.slots());
        }
    }
}