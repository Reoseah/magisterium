package io.github.reoseah.magisterium.item;

import net.minecraft.item.Item;

public class RuneItem extends Item {
    public static final Item FIRE = new RuneItem(new Item.Settings().maxCount(1));
    public static final Item WIND = new RuneItem(new Item.Settings().maxCount(1));

    public RuneItem(net.minecraft.item.Item.Settings settings) {
        super(settings);
    }
}

