package io.github.reoseah.magisterium.block.entity;

import io.github.reoseah.magisterium.block.MagicBarrierBlock;
import io.github.reoseah.magisterium.particle.MagisteriumParticles;
import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.UUID;

public class MagicBarrierBlockEntity extends BlockEntity {
    public static final BlockEntityType<?> TYPE = FabricBlockEntityTypeBuilder.create(MagicBarrierBlockEntity::new, MagicBarrierBlock.INSTANCE).build();

    protected UUID caster;

    public MagicBarrierBlockEntity(BlockPos pos, BlockState state) {
        super(TYPE, pos, state);
    }

    @Override
    protected void readNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registries) {
        super.readNbt(nbt, registries);
        if (nbt.contains("Caster")) {
            this.caster = nbt.getUuid("Caster");
        }
    }

    @Override
    protected void writeNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registries) {
        super.writeNbt(nbt, registries);
        if (this.caster != null) {
            nbt.putUuid("Caster", this.caster);
        }
    }

    public UUID getCaster() {
        return this.caster;
    }

    public static void tickClient(World world, BlockPos pos, BlockState state, BlockEntity entity) {
        var seed = Math.abs(hash(pos)) % 32;
        if (world.getTime() % 32 == seed) {
            double x = pos.getX() + .5;
            double y = pos.getY() + .5;
            double z = pos.getZ() + .5;
            var particle = MagisteriumParticles.GLYPHS[world.random.nextInt(MagisteriumParticles.GLYPHS.length)];
            world.addParticle(particle, x, y, z, 0, 0, 0);
        }
    }

    public static int mixBits(int z) {
        z = (z ^ (z >>> 16)) * 0xd36d884b;
        z = (z ^ (z >>> 16)) * 0xd36d884b;
        return z ^ (z >>> 16);
    }

    public static int hash(BlockPos pos) {
        int x = pos.getX();
        int y = pos.getY();
        int z = pos.getZ();
        return mixBits(x) + mixBits(y) * 31 + mixBits(z) * 127;
    }
}
