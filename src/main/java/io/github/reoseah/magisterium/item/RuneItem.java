package io.github.reoseah.magisterium.item;

import net.minecraft.component.DataComponentTypes;
import net.minecraft.item.Item;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public class RuneItem extends Item {
    public static final Identifier FIRE_ID = Identifier.of("magisterium:fire_rune");
    public static final Identifier WIND_ID = Identifier.of("magisterium:wind_rune");

    public static final RegistryKey<Item> FIRE_KEY = RegistryKey.of(RegistryKeys.ITEM, FIRE_ID);
    public static final RegistryKey<Item> WIND_KEY = RegistryKey.of(RegistryKeys.ITEM, WIND_ID);

    public static final Item FIRE = new RuneItem(new Item.Settings().registryKey(FIRE_KEY).component(DataComponentTypes.ITEM_NAME, Text.translatable("item.magisterium.fire_rune")).maxCount(1));
    public static final Item WIND = new RuneItem(new Item.Settings().registryKey(WIND_KEY).component(DataComponentTypes.ITEM_NAME, Text.translatable("item.magisterium.wind_rune")).maxCount(1));

    public RuneItem(Settings settings) {
        super(settings);
    }
}

