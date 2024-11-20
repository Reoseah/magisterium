package io.github.reoseah.magisterium.item;

import net.minecraft.item.Item;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.util.Identifier;

public class MagisteriumItemTags {
    // TODO: use stack.has(...) and components instead of a tag?
    public static final TagKey<Item> SPELL_BOOK_COMPONENTS = TagKey.of(RegistryKeys.ITEM, Identifier.of("magisterium:spell_book_components"));
}
