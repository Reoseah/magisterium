package io.github.reoseah.magisterium.item;

import net.minecraft.item.Item;

public class BookmarkItem extends Item {
    public static final Item INSTANCE = new BookmarkItem(new Item.Settings());

    public BookmarkItem(Settings settings) {
        super(settings);
    }
}
