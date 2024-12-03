package io.github.reoseah.magisterium.block.entity;

import io.github.reoseah.magisterium.block.ArcaneDetectorBlock;
import io.github.reoseah.magisterium.screen.SpellBookScreenHandler;
import io.github.reoseah.magisterium.world.state.ActiveSpellTracker;
import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.util.math.BlockPos;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class ArcaneDetectorBlockEntity extends BlockEntity {
    public static final BlockEntityType<ArcaneDetectorBlockEntity> TYPE = FabricBlockEntityTypeBuilder.create(ArcaneDetectorBlockEntity::new, ArcaneDetectorBlock.INSTANCE).build();

    public final Set<UUID> users = new HashSet<>();

    public ArcaneDetectorBlockEntity(BlockPos pos, BlockState state) {
        super(TYPE, pos, state);
    }
}
