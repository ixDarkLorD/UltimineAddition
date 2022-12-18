package net.ixdarklord.ultimine_addition.particle;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.*;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.util.RandomSource;

public class CelebrateParticle extends TextureSheetParticle {
    private static final RandomSource RANDOM = RandomSource.create();
    private final SpriteSet sprites;
    protected CelebrateParticle(ClientLevel level, double xCoord, double yCoord, double zCoord, double xd, double yd, double zd, SpriteSet spriteSet, SpriteSet sprites) {
        super(level, xCoord, yCoord, zCoord, xd, yd, zd);

        this.sprites = sprites;
        this.friction = 0.35F;
        this.x = xd;
        this.y = yd;
        this.z = zd;
        this.xd = xd + (Math.random() * 2.0 - 1.0) * 0.05000000074505806;
        this.yd = yd + (Math.random() * 2.0 - 1.0) * 0.05000000074505806;
        this.zd = zd + (Math.random() * 2.0 - 1.0) * 0.05000000074505806;
        this.quadSize *= 0.85F;
        this.lifetime = (int)(16.0 / ((double)this.random.nextFloat() * 0.8 + 0.2)) + 2;
        this.setSpriteFromAge(spriteSet);

        this.rCol = 1f;
        this.gCol = 1f;
        this.bCol = 1f;
    }

    @Override
    public void tick() {
        super.tick();
        this.setSpriteFromAge(this.sprites);
        fadeOut();
        this.xd *= 0.949999988079071;
        this.yd *= 0.8999999761581421;
        this.zd *= 0.949999988079071;
    }
    private void fadeOut() {
        this.alpha = (-(1/(float)lifetime) * age + 1);
    }

    @Override
    public ParticleRenderType getRenderType() {
        return ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT;
    }

    public static class Provider implements ParticleProvider<SimpleParticleType> {
        private final SpriteSet sprites;
        public Provider(SpriteSet spriteSet) {
            this.sprites = spriteSet;
        }

        public Particle createParticle(SimpleParticleType type, ClientLevel level, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed) {
            return new CelebrateParticle(level, x, y, z, xSpeed, ySpeed, zSpeed, this.sprites, sprites);
        }
    }
}
