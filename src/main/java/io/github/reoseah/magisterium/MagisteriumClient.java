package io.github.reoseah.magisterium;

import io.github.reoseah.magisterium.block.ArcaneTableBlock;
import io.github.reoseah.magisterium.block.EnchantedCandlestickBlock;
import io.github.reoseah.magisterium.block.GlyphBlock;
import io.github.reoseah.magisterium.block.MagicBarrierBlock;
import io.github.reoseah.magisterium.block.entity.ArcaneDetectorBlockEntity;
import io.github.reoseah.magisterium.block.entity.IllusoryWallBlockEntity;
import io.github.reoseah.magisterium.client.render.ArcaneResonatorRenderer;
import io.github.reoseah.magisterium.client.render.IllusoryWallBlockEntityRenderer;
import io.github.reoseah.magisterium.data.BookLoader;
import io.github.reoseah.magisterium.data.SpellPage;
import io.github.reoseah.magisterium.network.s2c.FinishSpellPayload;
import io.github.reoseah.magisterium.network.s2c.SpellParticlePayload;
import io.github.reoseah.magisterium.network.s2c.SyncronizeBookDataPayload;
import io.github.reoseah.magisterium.network.s2c.SyncronizePageDataPayload;
import io.github.reoseah.magisterium.particle.EnergyParticle;
import io.github.reoseah.magisterium.particle.GlyphParticle;
import io.github.reoseah.magisterium.particle.MagisteriumParticles;
import io.github.reoseah.magisterium.screen.ArcaneTableScreen;
import io.github.reoseah.magisterium.screen.ArcaneTableScreenHandler;
import io.github.reoseah.magisterium.screen.SpellBookScreen;
import io.github.reoseah.magisterium.screen.SpellBookScreenHandler;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap;
import net.fabricmc.fabric.api.client.model.loading.v1.ModelLoadingPlugin;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.particle.v1.ParticleFactoryRegistry;
import net.minecraft.client.gui.screen.ingame.HandledScreens;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactories;
import net.minecraft.util.Identifier;

import java.util.Map;

public class MagisteriumClient implements ClientModInitializer {
    public static Map<Identifier, SpellPage> pages;

    @Override
    public void onInitializeClient() {
        BlockRenderLayerMap.INSTANCE.putBlocks(RenderLayer.getCutout(), ArcaneTableBlock.INSTANCE, GlyphBlock.INSTANCE, EnchantedCandlestickBlock.INSTANCE);

        HandledScreens.register(SpellBookScreenHandler.TYPE, SpellBookScreen::new);
        HandledScreens.register(ArcaneTableScreenHandler.TYPE, ArcaneTableScreen::new);

        BlockEntityRendererFactories.register(IllusoryWallBlockEntity.TYPE, IllusoryWallBlockEntityRenderer::new);
        BlockEntityRendererFactories.register(ArcaneDetectorBlockEntity.TYPE, ArcaneResonatorRenderer::new);

        ModelLoadingPlugin.register(pluginContext -> {
            pluginContext.addModels(Identifier.of("magisterium:block/arcane_detector_crystal"));
            pluginContext.addModels(Identifier.of("magisterium:block/arcane_detector_crystal_on"));
        });

        ParticleFactoryRegistry.getInstance().register(MagisteriumParticles.ENERGY, EnergyParticle.Factory::new);
        ParticleFactoryRegistry.getInstance().register(MagisteriumParticles.GLYPH_A, GlyphParticle.Factory::new);
        ParticleFactoryRegistry.getInstance().register(MagisteriumParticles.GLYPH_B, GlyphParticle.Factory::new);
        ParticleFactoryRegistry.getInstance().register(MagisteriumParticles.GLYPH_C, GlyphParticle.Factory::new);
        ParticleFactoryRegistry.getInstance().register(MagisteriumParticles.GLYPH_D, GlyphParticle.Factory::new);
        ParticleFactoryRegistry.getInstance().register(MagisteriumParticles.GLYPH_E, GlyphParticle.Factory::new);
        ParticleFactoryRegistry.getInstance().register(MagisteriumParticles.GLYPH_F, GlyphParticle.Factory::new);
        ParticleFactoryRegistry.getInstance().register(MagisteriumParticles.GLYPH_G, GlyphParticle.Factory::new);
        ParticleFactoryRegistry.getInstance().register(MagisteriumParticles.BARRIER_SPARK, GlyphParticle.Factory::new);
        ParticleFactoryRegistry.getInstance().register(MagisteriumParticles.BARRIER_ENERGY, GlyphParticle.Factory::new);

        ClientPlayNetworking.registerGlobalReceiver(SyncronizeBookDataPayload.ID, (payload, context) -> {
            var books = payload.books();
            BookLoader.setClientSide(books);
        });
        ClientPlayNetworking.registerGlobalReceiver(SyncronizePageDataPayload.ID, (payload, context) -> pages = payload.pages());
        ClientPlayNetworking.registerGlobalReceiver(SpellParticlePayload.ID, MagisteriumClient::spawnSpellParticles);
        ClientPlayNetworking.registerGlobalReceiver(FinishSpellPayload.ID, MagisteriumClient::finishSpell);

        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> {
            BookLoader.disconnectClientSide();
            pages = null;
        });
    }

    private static void finishSpell(FinishSpellPayload payload, ClientPlayNetworking.Context context) {
        if (context.client().currentScreen instanceof SpellBookScreen screen) {
            screen.finishSpell();
        }
    }

    private static void spawnSpellParticles(SpellParticlePayload payload, ClientPlayNetworking.Context context) {
        var world = context.client().world;
        var random = world.random;
        for (var pos : payload.positions()) {
            for (int i = 0; i < 2; i++) {
                var x = pos.getX() + random.nextFloat();
                var y = pos.getY() + random.nextFloat();
                var z = pos.getZ() + random.nextFloat();
                var particle = MagisteriumParticles.GLYPHS[random.nextInt(MagisteriumParticles.GLYPHS.length)];

                world.addParticle(particle, x, y, z, 0, 0, 0);
            }

            for (int i = 0; i < 6; i++) {
                var x = pos.getX() + random.nextFloat();
                var y = pos.getY() + random.nextFloat();
                var z = pos.getZ() + random.nextFloat();
                var dx = .01 * (random.nextFloat() - .5);
                var dy = .01 * (random.nextFloat() - .5);
                var dz = .01 * (random.nextFloat() - .5);

                world.addParticle(MagisteriumParticles.ENERGY, x, y, z, dx, dy, dz);
            }
        }
    }
}