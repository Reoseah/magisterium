package io.github.reoseah.magisterium.block;

import net.minecraft.block.Block;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.util.Identifier;

public class MagisteriumBlockTags {
    public static final TagKey<Block> AWAKEN_THE_FIRE_TARGETS = TagKey.of(RegistryKeys.BLOCK, Identifier.of("magisterium:awaken_the_fire_targets"));
    public static final TagKey<Block> DISPEL_MAGIC_SUSCEPTIBLE = TagKey.of(RegistryKeys.BLOCK, Identifier.of("magisterium:dispel_magic_susceptible"));
}
