package io.github.reoseah.magisterium.particle;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.particle.*;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.particle.SimpleParticleType;

@Environment(EnvType.CLIENT)
public class GlyphParticle extends SpriteBillboardParticle {
    private final SpriteProvider spriteProvider;

    public GlyphParticle(ClientWorld clientWorld, double x, double y, double z, double velocityX, double velocityY, double velocityZ, SpriteProvider spriteProvider) {
        super(clientWorld, x, y, z, velocityX, velocityY, velocityZ);
        this.spriteProvider = spriteProvider;
        this.maxAge = 32;
        this.setSpriteForAge(spriteProvider);

        this.scale = .5F;
        this.velocityX = this.velocityX * 0.05 + velocityX;
        this.velocityY = this.velocityY * 0.05 + velocityY;
        this.velocityZ = this.velocityZ * 0.05 + velocityZ;
    }

    @Override
    public ParticleTextureSheet getType() {
        return ParticleTextureSheet.PARTICLE_SHEET_OPAQUE;
    }

    @Override
    protected int getBrightness(float tint) {
        return 0xFF;
    }

    @Override
    public void tick() {
        this.prevPosX = this.x;
        this.prevPosY = this.y;
        this.prevPosZ = this.z;
        if (this.age++ >= this.maxAge) {
            this.markDead();
        } else {
            this.velocityY = this.velocityY - this.gravityStrength;
            double delta = this.age / (double) this.maxAge;
            this.move(delta * this.velocityX, delta * this.velocityY, delta * this.velocityZ);
            this.setSpriteForAge(this.spriteProvider);
        }
    }

    @Environment(EnvType.CLIENT)
    public static class Factory implements ParticleFactory<SimpleParticleType> {
        private final SpriteProvider spriteProvider;
        private final int maxAge;

        public Factory(SpriteProvider spriteProvider) {
            this(spriteProvider, 32);
        }

        public Factory(SpriteProvider spriteProvider, int maxAge) {
            this.spriteProvider = spriteProvider;
            this.maxAge = maxAge;
        }

        public Particle createParticle(SimpleParticleType simpleParticleType, ClientWorld clientWorld, double x, double y, double z, double velocityX, double velocityY, double velocityZ) {
            GlyphParticle particle = new GlyphParticle(clientWorld, x, y, z, velocityX, velocityY, velocityZ, this.spriteProvider);
            particle.maxAge = this.maxAge;
            particle.setSpriteForAge(this.spriteProvider);
            return particle;
        }
    }
}
