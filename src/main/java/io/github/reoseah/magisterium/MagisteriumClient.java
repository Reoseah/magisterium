package io.github.reoseah.magisterium;

import io.github.reoseah.magisterium.block.ArcaneTableBlock;
import io.github.reoseah.magisterium.block.GlyphBlock;
import io.github.reoseah.magisterium.block.IllusoryWallBlockEntity;
import io.github.reoseah.magisterium.block.IllusoryWallBlockEntityRenderer;
import io.github.reoseah.magisterium.item.SpellBookItem;
import io.github.reoseah.magisterium.screen.ArcaneTableScreen;
import io.github.reoseah.magisterium.screen.ArcaneTableScreenHandler;
import io.github.reoseah.magisterium.screen.SpellBookScreen;
import io.github.reoseah.magisterium.screen.SpellBookScreenHandler;
import io.github.reoseah.magisterium.spellbook.SpellDataLoader;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap;
import net.fabricmc.fabric.api.client.model.loading.v1.ModelLoadingPlugin;
import net.fabricmc.fabric.impl.resource.loader.ResourceManagerHelperImpl;
import net.minecraft.client.gui.screen.ingame.HandledScreens;
import net.minecraft.client.item.ModelPredicateProviderRegistry;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactories;
import net.minecraft.client.util.ModelIdentifier;
import net.minecraft.resource.ResourceType;
import net.minecraft.util.Identifier;

public class MagisteriumClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        // perhaps it could be loaded on the server and sent to client,
        // like the recipes do?
        // this will help with making items for each spell
        ResourceManagerHelperImpl.get(ResourceType.CLIENT_RESOURCES).registerReloadListener(new SpellDataLoader());

        BlockRenderLayerMap.INSTANCE.putBlocks(RenderLayer.getCutout(), ArcaneTableBlock.INSTANCE, GlyphBlock.INSTANCE);

        ModelLoadingPlugin.register(ctx -> ctx.addModels(Identifier.of("magisterium", "item/spell_book_in_hand")));

        ModelPredicateProviderRegistry.register(SpellBookItem.INSTANCE, Identifier.of("magisterium:is_in_hand"), //
                (stack, world, entity, seed) -> entity != null && entity.getActiveItem().equals(stack) ? 1.0F : 0.0F);

        HandledScreens.register(SpellBookScreenHandler.TYPE, SpellBookScreen::new);
        HandledScreens.register(ArcaneTableScreenHandler.TYPE, ArcaneTableScreen::new);

        BlockEntityRendererFactories.register(IllusoryWallBlockEntity.TYPE, IllusoryWallBlockEntityRenderer::new);
    }
}