package net.ixdarklord.ultimine_addition.datagen.particle;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import net.ixdarklord.ultimine_addition.core.FTBUltimineAddition;
import net.ixdarklord.ultimine_addition.core.Registration;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.data.PackOutput.Target;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.concurrent.CompletableFuture;

public abstract class ParticleProvider implements DataProvider {
   private final PackOutput.PathProvider pathProvider;
   private final Map<String, List<ResourceLocation>> data = new TreeMap<>();

   protected abstract void addParticles();

   public ParticleProvider(PackOutput output) {
      this.pathProvider = output.createPathProvider(Target.RESOURCE_PACK, "particles");
   }

   public @NotNull CompletableFuture<?> run(@NotNull CachedOutput cache) {
      this.clear();
      this.addParticles();
      if (this.data.isEmpty()) {
         throw new IllegalStateException("The structure data is empty!");
      } else {
         CompletableFuture<?>[] futures = new CompletableFuture[this.data.size()];
         int i = 0;

         for (Map.Entry<String, List<ResourceLocation>> particle : this.data.entrySet()) {
            JsonObject JSONProperties = new JsonObject();
            JsonArray array = new JsonArray();

            for (ResourceLocation entry : particle.getValue()) {
               array.add(entry.toString());
            }

            JSONProperties.add("textures", array);
            futures[i++] = DataProvider.saveStable(cache, JSONProperties, this.pathProvider.file(FTBUltimineAddition.id(particle.getKey()), "json"));
         }

         return CompletableFuture.allOf(futures);
      }
   }

   public void add(SimpleParticleType particle, ResourceLocation... texture) {
      String particleName = Objects.requireNonNull(Registration.PARTICLE_TYPES.getRegistrar().getId(particle)).getPath();
      if (this.data.containsKey(particleName)) {
         throw new IllegalStateException("Duplicate particle " + particleName);
      } else {
         this.data.put(particleName, Arrays.stream(texture).toList());
      }
   }

   private void clear() {
      this.data.clear();
   }

   public final @NotNull String getName() {
      return "Particles";
   }
}
