package io.github.reoseah.magisterium.block.entity;

import io.github.reoseah.magisterium.block.ArcaneResonatorBlock;
import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.util.math.BlockPos;

public class ArcaneResonatorBlockEntity extends BlockEntity {
    public static final BlockEntityType<ArcaneResonatorBlockEntity> TYPE = FabricBlockEntityTypeBuilder.create(ArcaneResonatorBlockEntity::new, ArcaneResonatorBlock.INSTANCE).build();

    public ArcaneResonatorBlockEntity(BlockPos pos, BlockState state) {
        super(TYPE, pos, state);
    }
}
