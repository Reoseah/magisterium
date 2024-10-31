package io.github.reoseah.magisterium.block;

import io.github.reoseah.magisterium.particle.MagisteriumParticles;
import net.minecraft.block.Block;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.particle.SimpleParticleType;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;

public class TestBlock extends Block {
    public static final Block INSTANCE = new TestBlock(Settings.create().nonOpaque().noCollision());

    public TestBlock(Settings settings) {
        super(settings);
    }

    @Override
    protected BlockRenderType getRenderType(BlockState state) {
        return BlockRenderType.INVISIBLE;
    }

    @Override
    public void randomDisplayTick(BlockState state, World world, BlockPos pos, Random random) {
        super.randomDisplayTick(state, world, pos, random);

        var types = new SimpleParticleType[]{ //
                MagisteriumParticles.GLYPH_A, //
                MagisteriumParticles.GLYPH_B, //
                MagisteriumParticles.GLYPH_C //
        };

        for (int i = 0; i < 1; i++) {
            double x = pos.getX() + .5 + random.nextGaussian();
            double y = pos.getY() + .5 + random.nextGaussian();
            double z = pos.getZ() + .5 + random.nextGaussian();
            world.addParticle(types[random.nextInt(types.length)], x, y, z, 0.0D, 0.0D, 0.0D);
        }

        for (int i = 0; i < 4; i++) {
            double x = pos.getX() + .5 + random.nextGaussian();
            double y = pos.getY() + .5 + random.nextGaussian();
            double z = pos.getZ() + .5 + random.nextGaussian();
            world.addParticle(MagisteriumParticles.ENERGY, x, y, z, 0.0D, 0.05D, 0.0D);
        }
    }
}
