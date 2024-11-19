package io.github.reoseah.magisterium.item;

import net.minecraft.item.ToolMaterial;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.util.Identifier;

public class MagisteriumMaterials {
    public static final ToolMaterial BLAZE = new ToolMaterial(BlockTags.INCORRECT_FOR_STONE_TOOL, 100, 6F, 4F, 1, TagKey.of(RegistryKeys.ITEM, Identifier.of("magisterium:repairs_blaze_sword")));
}
