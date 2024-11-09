package io.github.reoseah.magisterium.block;

import io.github.reoseah.magisterium.data.effect.SpellWorldChangeTracker;
import net.minecraft.util.math.BlockPos;

public interface CustomDispelBehavior {
    void dispel(SpellWorldChangeTracker context, BlockPos start);
}
