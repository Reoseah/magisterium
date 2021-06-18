package com.github.reoseah.magisterium;

import net.fabricmc.api.ModInitializer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.util.Identifier;
import net.minecraft.util.Rarity;
import net.minecraft.util.registry.Registry;

public class Magisterium implements ModInitializer {
	public static final String MODID = "magisterium";

	public static final Item MAGISTERIUM = new MagisteriumItem(
			new Item.Settings().group(ItemGroup.TOOLS).rarity(Rarity.UNCOMMON).maxCount(1));

	@Override
	public void onInitialize() {
		Registry.register(Registry.ITEM, createId("magisterium"), MAGISTERIUM);
	}

	public static Identifier createId(String name) {
		return new Identifier(MODID, name);
	}
}
