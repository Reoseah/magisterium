package io.github.reoseah.magisterium;

import io.github.reoseah.magisterium.block.ArcaneTableBlock;
import io.github.reoseah.magisterium.item.SpellBookItem;
import io.github.reoseah.magisterium.network.SlotLayoutPayload;
import io.github.reoseah.magisterium.network.StartUtterancePayload;
import io.github.reoseah.magisterium.network.StopUtterancePayload;
import io.github.reoseah.magisterium.recipe.SpellBookCraftingRecipe;
import io.github.reoseah.magisterium.recipe.SpellBookRecipe;
import io.github.reoseah.magisterium.screen.SpellBookScreenHandler;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.text.Text;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Magisterium implements ModInitializer {
    public static final String MOD_ID = "magisterium";

    public static final Logger LOGGER = LoggerFactory.getLogger(Magisterium.class);

    @Override
    public void onInitialize() {
        Registry.register(Registries.BLOCK, "magisterium:arcane_table", ArcaneTableBlock.INSTANCE);

        Registry.register(Registries.ITEM, "magisterium:arcane_table", new BlockItem(ArcaneTableBlock.INSTANCE, new Item.Settings()));
        Registry.register(Registries.ITEM, "magisterium:spell_book", SpellBookItem.INSTANCE);

        Registry.register(Registries.DATA_COMPONENT_TYPE, "magisterium:current_page", SpellBookItem.CURRENT_PAGE);

        var group = FabricItemGroup.builder() //
                .icon(SpellBookItem.INSTANCE::getDefaultStack) //
                .displayName(Text.translatable("itemGroup.magisterium")) //
                .entries((displayContext, entries) -> {
                    entries.add(ArcaneTableBlock.INSTANCE.asItem());
                    entries.add(SpellBookItem.INSTANCE);
                }) //
                .build();
        Registry.register(Registries.ITEM_GROUP, "magisterium", group);

        Registry.register(Registries.RECIPE_TYPE, "magisterium:spell_book", SpellBookRecipe.TYPE);

        Registry.register(Registries.RECIPE_SERIALIZER, "magisterium:spell_book_crafting", SpellBookCraftingRecipe.Serializer.INSTANCE);

        Registry.register(Registries.SCREEN_HANDLER, "magisterium:spell_book", SpellBookScreenHandler.TYPE);

        PayloadTypeRegistry.playC2S().register(StartUtterancePayload.ID, StartUtterancePayload.CODEC);
        PayloadTypeRegistry.playC2S().register(StopUtterancePayload.ID, StopUtterancePayload.CODEC);
        PayloadTypeRegistry.playC2S().register(SlotLayoutPayload.ID, SlotLayoutPayload.CODEC);

        ServerPlayNetworking.registerGlobalReceiver(StartUtterancePayload.ID, (payload, context) -> {
            LOGGER.info("StartUtterancePayload: {}", payload);
            if (context.player().currentScreenHandler instanceof SpellBookScreenHandler handler) {
                handler.startUtterance(payload.id(), context.player());
            }
        });
        ServerPlayNetworking.registerGlobalReceiver(StopUtterancePayload.ID, (payload, context) -> {
            LOGGER.info("StopUtterancePayload: {}", payload);
            if (context.player().currentScreenHandler instanceof SpellBookScreenHandler handler) {
                handler.stopUtterance();
            }
        });
        ServerPlayNetworking.registerGlobalReceiver(SlotLayoutPayload.ID, (payload, context) -> {
            if (context.player().currentScreenHandler instanceof SpellBookScreenHandler handler) {
                handler.configureSlots(payload.layout());
            }
        });
    }
}