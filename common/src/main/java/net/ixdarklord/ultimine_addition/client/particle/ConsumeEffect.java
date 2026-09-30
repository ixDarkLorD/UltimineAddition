package net.ixdarklord.ultimine_addition.client.particle;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.TerrainParticle;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

// A block consumed by a challenge: fragments of it and glowing motes float up out of the spot, then get vacuumed
// into the player completing the challenge. Client only; the particles steer themselves, no particle type needed.
public final class ConsumeEffect {
    private static final int FRAGMENTS = 26;
    private static final int MOTES = 14;

    private final int targetId;
    private boolean arrived;

    private ConsumeEffect(int targetId) {
        this.targetId = targetId;
    }

    public static void play(BlockPos pos, BlockState state, int targetId) {
        Minecraft minecraft = Minecraft.getInstance();
        ClientLevel level = minecraft.level;
        if (level == null || state.isAir()) return;
        ConsumeEffect effect = new ConsumeEffect(targetId);
        RandomSource random = level.getRandom();

        for (int i = 0; i < FRAGMENTS + MOTES; i++) {
            boolean mote = i >= FRAGMENTS;
            double x = pos.getX() + 0.15 + random.nextDouble() * 0.7;
            double y = pos.getY() + 0.15 + random.nextDouble() * 0.7;
            double z = pos.getZ() + 0.15 + random.nextDouble() * 0.7;
            minecraft.particleEngine.add(new Mote(level, x, y, z, state, pos, effect, mote, random));
        }

        var sound = state.getSoundType();
        level.playLocalSound(pos, sound.getBreakSound(), SoundSource.BLOCKS, (sound.getVolume() + 1.0F) / 2.0F, sound.getPitch() * 0.8F, false);
        level.playLocalSound(pos, SoundEvents.BREEZE_INHALE, SoundSource.PLAYERS, 0.45F, 1.5F + random.nextFloat() * 0.2F, false);
        level.playLocalSound(pos, SoundEvents.TRIAL_SPAWNER_SPAWN_ITEM_BEGIN, SoundSource.PLAYERS, 0.35F, 1.6F, false);
    }

    // The first particle reaching the player chimes once, at the player.
    private void onArrive(ClientLevel level, Entity target) {
        if (this.arrived) return;
        this.arrived = true;
        level.playLocalSound(target.getX(), target.getY(), target.getZ(), SoundEvents.ITEM_PICKUP, SoundSource.PLAYERS, 0.3F, 1.7F, false);
        level.playLocalSound(target.getX(), target.getY(), target.getZ(), SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.PLAYERS, 0.6F, 1.3F + level.getRandom().nextFloat() * 0.3F, false);
    }

    private static final class Mote extends TerrainParticle {
        private final ConsumeEffect effect;
        private final boolean glowing;
        private final Vec3 center;
        private final int floatTicks;
        private final float baseSize;
        private final double swirl;

        private Mote(ClientLevel level, double x, double y, double z, BlockState state, BlockPos pos, ConsumeEffect effect, boolean glowing, RandomSource random) {
            super(level, x, y, z, 0, 0, 0, state, pos);
            this.effect = effect;
            this.glowing = glowing;
            this.center = pos.getCenter();
            this.hasPhysics = false;
            this.gravity = 0.0F;
            this.floatTicks = 8 + random.nextInt(10);
            this.lifetime = this.floatTicks + 60;
            this.swirl = (random.nextBoolean() ? 1 : -1) * (0.04 + random.nextDouble() * 0.05);
            // Buoyant start: a little up and outward from the block's centre.
            this.xd = (x - this.center.x) * 0.06;
            this.yd = 0.02 + random.nextDouble() * 0.04;
            this.zd = (z - this.center.z) * 0.06;
            if (glowing) {
                this.quadSize *= 0.55F;
                this.setColor(0.85F + random.nextFloat() * 0.15F, 0.6F + random.nextFloat() * 0.2F, 1.0F);
            } else {
                this.quadSize *= 0.9F + random.nextFloat() * 0.5F;
            }
            this.baseSize = this.quadSize;
        }

        @Override
        public void tick() {
            this.xo = this.x;
            this.yo = this.y;
            this.zo = this.z;
            if (this.age++ >= this.lifetime) {
                this.remove();
                return;
            }

            Entity target = this.level.getEntity(this.effect.targetId);
            if (this.age < this.floatTicks || target == null) {
                // Float: slow down, rise gently and circle the spot.
                double ox = this.x - this.center.x, oz = this.z - this.center.z;
                this.xd = this.xd * 0.86 - oz * this.swirl;
                this.zd = this.zd * 0.86 + ox * this.swirl;
                this.yd = this.yd * 0.86 + 0.003;
                if (target == null && this.age > this.floatTicks) this.alpha = Math.max(0.0F, this.alpha - 0.05F);
            } else {
                // Vacuum: pulled harder every tick towards the player's chest.
                Vec3 aim = target.position().add(0, target.getBbHeight() * 0.55, 0);
                double dx = aim.x - this.x, dy = aim.y - this.y, dz = aim.z - this.z;
                double distance = Math.sqrt(dx * dx + dy * dy + dz * dz);
                if (distance < 0.4) {
                    this.effect.onArrive(this.level, target);
                    this.remove();
                    return;
                }
                double pull = Math.min(1.2, 0.06 + 0.03 * (this.age - this.floatTicks));
                double speed = Math.min(distance, pull);
                this.xd = Mth.lerp(0.35, this.xd, dx / distance * speed);
                this.yd = Mth.lerp(0.35, this.yd, dy / distance * speed);
                this.zd = Mth.lerp(0.35, this.zd, dz / distance * speed);
                // Shrink into the player over the last couple of blocks.
                this.quadSize = this.baseSize * (float) Mth.clamp(distance / 2.0, 0.3, 1.0);
            }
            this.move(this.xd, this.yd, this.zd);
        }

        @Override
        public int getLightColor(float partialTick) {
            return this.glowing ? 0xF000F0 : super.getLightColor(partialTick);
        }
    }
}
