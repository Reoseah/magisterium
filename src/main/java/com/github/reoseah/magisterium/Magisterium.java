package com.github.reoseah.magisterium;

import java.util.function.Consumer;

import com.github.reoseah.magisterium.pages.MagisteriumPage;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.client.model.ExtraModelProvider;
import net.fabricmc.fabric.api.client.model.ModelLoadingRegistry;
import net.fabricmc.fabric.api.client.screenhandler.v1.ScreenRegistry;
import net.fabricmc.fabric.api.event.client.ClientSpriteRegistryCallback;
import net.fabricmc.fabric.impl.screenhandler.ExtendedScreenHandlerType;
import net.minecraft.client.texture.SpriteAtlasTexture;
import net.minecraft.client.texture.TextureStitcher;
import net.minecraft.client.util.ModelIdentifier;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.resource.ResourceManager;
import net.minecraft.screen.PlayerScreenHandler;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.util.Identifier;
import net.minecraft.util.Rarity;
import net.minecraft.util.registry.Registry;

public class Magisterium implements ModInitializer {
	public static final String MODID = "magisterium";

	public static final Item MAGISTERIUM = new MagisteriumItem(
			new Item.Settings().group(ItemGroup.TOOLS).rarity(Rarity.UNCOMMON).maxCount(1));

	public static final ScreenHandlerType<MagisteriumHandler> MAGISTERIUM_SCREEN = new ExtendedScreenHandlerType<>(
			MagisteriumHandler.Client::new);

	@Override
	public void onInitialize() {
		Registry.register(Registry.ITEM, createId("magisterium"), MAGISTERIUM);
		Registry.register(Registry.SCREEN_HANDLER, createId("magisterium"), MAGISTERIUM_SCREEN);
	}

	public static Identifier createId(String name) {
		return new Identifier(MODID, name);
	}

	@Environment(EnvType.CLIENT)
	public static class Client implements ClientModInitializer {
		@Override
		public void onInitializeClient() {
			ModelLoadingRegistry.INSTANCE.registerModelProvider(new ExtraModelProvider() {
				@Override
				public void provideExtraModels(ResourceManager manager, Consumer<Identifier> out) {
					out.accept(new ModelIdentifier("magisterium:magisterium_in_hand#inventory"));
					out.accept(new ModelIdentifier("magisterium:magisterium#inventory"));
				}
			});
			ClientSpriteRegistryCallback.event(PlayerScreenHandler.BLOCK_ATLAS_TEXTURE)
					.register((SpriteAtlasTexture atlas, ClientSpriteRegistryCallback.Registry registry) -> {
						registry.register(MagisteriumPage.SLOT_DUST.getSecond());
						registry.register(MagisteriumPage.SLOT_SLIMEBALL.getSecond());
						registry.register(MagisteriumPage.SLOT_FERMENTED_SPIDER_EYE.getSecond());
						registry.register(MagisteriumPage.SLOT_QUESTION_MARK.getSecond());
					});

			ScreenRegistry.register(MAGISTERIUM_SCREEN, MagisteriumScreen::new);
		}
	}
}
