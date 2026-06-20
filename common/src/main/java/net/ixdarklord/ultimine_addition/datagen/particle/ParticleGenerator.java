package net.ixdarklord.ultimine_addition.datagen.particle;

import net.ixdarklord.ultimine_addition.core.FTBUltimineAddition;
import net.ixdarklord.ultimine_addition.core.Registration;
import net.minecraft.data.PackOutput;

public class ParticleGenerator extends ParticleProvider {
   public ParticleGenerator(PackOutput output) {
      super(output);
   }

   protected void addParticles() {
      this.add(Registration.CELEBRATE_PARTICLE.get(), FTBUltimineAddition.id("celebrate"), FTBUltimineAddition.id("diamond_pickaxe"), FTBUltimineAddition.id("diamond_axe"), FTBUltimineAddition.id("celebrate"), FTBUltimineAddition.id("diamond_shovel"), FTBUltimineAddition.id("diamond_hoe"), FTBUltimineAddition.id("celebrate"));
   }
}
