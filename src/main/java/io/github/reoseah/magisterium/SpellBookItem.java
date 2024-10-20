package io.github.reoseah.magisterium;

import net.minecraft.item.Item;

public class SpellBookItem extends Item {
    public static final Item INSTANCE = new SpellBookItem(new Item.Settings().maxCount(1));

    public SpellBookItem(Settings settings) {
        super(settings);
    }
}
