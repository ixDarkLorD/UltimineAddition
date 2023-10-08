package net.ixdarklord.ultimine_addition.client.particle;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.*;
import net.minecraft.core.particles.SimpleParticleType;
import org.jetbrains.annotations.NotNull;

public class CelebrateParticle extends RisingParticle {
    private final SpriteSet sprites;
    protected CelebrateParticle(ClientLevel level, double xCord, double yCord, double zCord, double xd, double yd, double zd, SpriteSet spriteSet, SpriteSet sprites) {
        super(level, xCord, yCord, zCord, xd, yd, zd);
        this.sprites = sprites;
        this.quadSize *= 0.85F;
        this.lifetime = (int)(16.0 / ((double)this.random.nextFloat() * 0.8 + 0.2)) + 2;

        this.setSpriteFromAge(spriteSet);
        this.rCol = 1f;
        this.gCol = 1f;
        this.bCol = 1f;
    }

    @Override
    public void move(double pX, double pY, double pZ) {
        this.setBoundingBox(this.getBoundingBox().move(pX, pY, pZ));
        this.setLocationFromBoundingbox();
    }

    @Override
    public void tick() {
        super.tick();
        fadeOut();
        this.setSpriteFromAge(this.sprites);
    }
    private void fadeOut() {
        this.alpha = (-(1/(float)lifetime) * age + 1);
    }

    @Override
    public @NotNull ParticleRenderType getRenderType() {
        return ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT;
    }


    public static class Provider implements ParticleProvider<SimpleParticleType> {
        private final SpriteSet sprites;
        public Provider(SpriteSet spriteSet) {
            this.sprites = spriteSet;
        }

        public Particle createParticle(@NotNull SimpleParticleType type, @NotNull ClientLevel level, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed) {
            return new CelebrateParticle(level, x, y, z, xSpeed, ySpeed, zSpeed, this.sprites, sprites);
        }
    }
}
