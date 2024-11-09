package io.github.reoseah.magisterium.item;

import net.minecraft.component.DataComponentTypes;
import net.minecraft.item.Item;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public class BookmarkItem extends Item {
    public static final Identifier ID = Identifier.of("magisterium:bookmark");
    public static final RegistryKey<Item> KEY = RegistryKey.of(RegistryKeys.ITEM, ID);
    public static final Item INSTANCE = new BookmarkItem(new Item.Settings() //
            .registryKey(KEY) //
            .component(DataComponentTypes.ITEM_NAME, Text.translatable("item.magisterium.bookmark")));

    public BookmarkItem(Settings settings) {
        super(settings);
    }
}
