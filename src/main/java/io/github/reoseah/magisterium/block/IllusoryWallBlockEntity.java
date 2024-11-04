package io.github.reoseah.magisterium.block;

import com.google.common.collect.ImmutableSet;
import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityType;
import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtHelper;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.Nullable;

public class IllusoryWallBlockEntity extends BlockEntity {
    public static final BlockEntityType<IllusoryWallBlockEntity> TYPE = FabricBlockEntityTypeBuilder.create(IllusoryWallBlockEntity::new, IllusoryWallBlock.INSTANCE).build();

    protected BlockState illusoryState = Blocks.STONE.getDefaultState();

    public IllusoryWallBlockEntity(BlockPos pos, BlockState state) {
        super(TYPE, pos, state);
    }

    public BlockState getIllusoryState() {
        return this.illusoryState;
    }

    public void setIllusoryState(BlockState state) {
        this.illusoryState = state;
        if (this.world instanceof ServerWorld world) {
            world.updateListeners(this.pos, this.getCachedState(), this.getCachedState(), 3);
        }
    }

    @Override
    protected void readNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup) {
        super.readNbt(nbt, registryLookup);
        this.illusoryState = NbtHelper.toBlockState(registryLookup.getOrThrow(RegistryKeys.BLOCK), nbt.getCompound("IllusoryState"));
    }

    @Override
    protected void writeNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup) {
        super.writeNbt(nbt, registryLookup);
        nbt.put("IllusoryState", NbtHelper.fromBlockState(this.illusoryState));
    }

    @Override
    public NbtCompound toInitialChunkDataNbt(RegistryWrapper.WrapperLookup registryLookup) {
        var nbt = super.toInitialChunkDataNbt(registryLookup);
        nbt.put("IllusoryState", NbtHelper.fromBlockState(this.illusoryState));
        return nbt;
    }

    @Override
    public @Nullable Packet<ClientPlayPacketListener> toUpdatePacket() {
        return BlockEntityUpdateS2CPacket.create(this);
    }
}
