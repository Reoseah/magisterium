package io.github.reoseah.magisterium;

import io.github.reoseah.magisterium.block.ArcaneTableBlock;
import io.github.reoseah.magisterium.item.SpellBookItem;
import io.github.reoseah.magisterium.screen.SpellBookScreenHandler;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.text.Text;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Magisterium implements ModInitializer {
    public static final String MOD_ID = "magisterium";

    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitialize() {
        Registry.register(Registries.BLOCK, "magisterium:arcane_table", ArcaneTableBlock.INSTANCE);

        Registry.register(Registries.ITEM, "magisterium:arcane_table", new BlockItem(ArcaneTableBlock.INSTANCE, new Item.Settings()));
        Registry.register(Registries.ITEM, "magisterium:spell_book", SpellBookItem.INSTANCE);

        var group = FabricItemGroup.builder() //
                .icon(SpellBookItem.INSTANCE::getDefaultStack) //
                .displayName(Text.translatable("itemGroup.magisterium")) //
                .entries((displayContext, entries) -> {
                    entries.add(ArcaneTableBlock.INSTANCE.asItem());
                    entries.add(SpellBookItem.INSTANCE);
                }) //
                .build();
        Registry.register(Registries.ITEM_GROUP, "magisterium", group);

        Registry.register(Registries.SCREEN_HANDLER, "magisterium:spell_book", SpellBookScreenHandler.TYPE);
    }
}