package io.github.reoseah.magisterium;

import io.github.reoseah.magisterium.block.ArcaneTableBlock;
import io.github.reoseah.magisterium.screen.ArcaneTableScreen;
import io.github.reoseah.magisterium.screen.ArcaneTableScreenHandler;
import io.github.reoseah.magisterium.screen.SpellBookScreen;
import io.github.reoseah.magisterium.screen.SpellBookScreenHandler;
import io.github.reoseah.magisterium.spellbook.SpellDataLoader;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap;
import net.fabricmc.fabric.impl.resource.loader.ResourceManagerHelperImpl;
import net.minecraft.client.gui.screen.ingame.HandledScreens;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.resource.ResourceType;

public class MagisteriumClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        // perhaps it could be loaded on the server and sent to client,
        // like the recipes do?
        // this will help with making items for each spell
        ResourceManagerHelperImpl.get(ResourceType.CLIENT_RESOURCES).registerReloadListener(new SpellDataLoader());

        BlockRenderLayerMap.INSTANCE.putBlocks(RenderLayer.getCutout(), ArcaneTableBlock.INSTANCE);

        HandledScreens.register(SpellBookScreenHandler.TYPE, SpellBookScreen::new);
        HandledScreens.register(ArcaneTableScreenHandler.TYPE, ArcaneTableScreen::new);
    }
}