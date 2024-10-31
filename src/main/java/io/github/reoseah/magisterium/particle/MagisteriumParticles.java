package io.github.reoseah.magisterium.particle;

import net.minecraft.particle.SimpleParticleType;

public class MagisteriumParticles {
    public static final SimpleParticleType ENERGY = new MagisteriumParticleType(true);
    public static final SimpleParticleType GLYPH_A = new MagisteriumParticleType(true);
    public static final SimpleParticleType GLYPH_B = new MagisteriumParticleType(true);
    public static final SimpleParticleType GLYPH_C = new MagisteriumParticleType(true);

    // makes constructor public
    public static class MagisteriumParticleType extends SimpleParticleType {
        public MagisteriumParticleType(boolean alwaysShow) {
            super(alwaysShow);
        }
    }
}
