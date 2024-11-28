package io.github.reoseah.magisterium.network.s2c;

import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;

import java.util.ArrayList;
import java.util.List;

public record SpellParticlePayload(List<BlockPos> positions) implements CustomPayload {
    public static final CustomPayload.Id<SpellParticlePayload> ID = new CustomPayload.Id<>(Identifier.of("magisterium:spell_particle"));
    public static final PacketCodec<RegistryByteBuf, SpellParticlePayload> CODEC = CustomPayload.codecOf(SpellParticlePayload::write, SpellParticlePayload::read);

    public void write(RegistryByteBuf buf) {
        buf.writeVarInt(this.positions.size());
        for (BlockPos pos : this.positions) {
            buf.writeBlockPos(pos);
        }
    }

    public static SpellParticlePayload read(RegistryByteBuf buf) {
        int size = buf.readVarInt();
        var positions = new ArrayList<BlockPos>(size);
        for (int i = 0; i < size; i++) {
            positions.add(buf.readBlockPos());
        }
        return new SpellParticlePayload(positions);
    }

    @Override
    public CustomPayload.Id<? extends CustomPayload> getId() {
        return ID;
    }
}
