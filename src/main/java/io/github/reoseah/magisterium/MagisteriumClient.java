package io.github.reoseah.magisterium;

import io.github.reoseah.magisterium.block.*;
import io.github.reoseah.magisterium.data.BookLoader;
import io.github.reoseah.magisterium.data.SpellPage;
import io.github.reoseah.magisterium.network.SpellParticlePayload;
import io.github.reoseah.magisterium.network.SyncronizeBookDataPayload;
import io.github.reoseah.magisterium.network.SyncronizePageDataPayload;
import io.github.reoseah.magisterium.particle.EnergyParticle;
import io.github.reoseah.magisterium.particle.GlyphParticle;
import io.github.reoseah.magisterium.particle.MagisteriumParticles;
import io.github.reoseah.magisterium.screen.ArcaneTableScreen;
import io.github.reoseah.magisterium.screen.ArcaneTableScreenHandler;
import io.github.reoseah.magisterium.screen.SpellBookScreen;
import io.github.reoseah.magisterium.screen.SpellBookScreenHandler;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap;
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
        BlockRenderLayerMap.INSTANCE.putBlocks(RenderLayer.getCutout(), ArcaneTableBlock.INSTANCE, GlyphBlock.INSTANCE, MagicBarrierBlock.INSTANCE);

        HandledScreens.register(SpellBookScreenHandler.TYPE, SpellBookScreen::new);
        HandledScreens.register(ArcaneTableScreenHandler.TYPE, ArcaneTableScreen::new);

        BlockEntityRendererFactories.register(IllusoryWallBlockEntity.TYPE, IllusoryWallBlockEntityRenderer::new);

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

        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> {
            BookLoader.disconnectClientSide();
            pages = null;
        });
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