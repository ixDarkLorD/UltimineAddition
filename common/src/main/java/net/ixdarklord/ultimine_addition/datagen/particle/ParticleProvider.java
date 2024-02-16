package net.ixdarklord.ultimine_addition.datagen.particle;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import net.ixdarklord.ultimine_addition.core.UltimineAddition;
import net.ixdarklord.ultimine_addition.core.Registration;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.concurrent.CompletableFuture;

public abstract class ParticleProvider implements DataProvider {
    private final PackOutput.PathProvider pathProvider;

    private final Map<String, List<ResourceLocation>> data = new TreeMap<>();
    protected abstract void addParticles();

    public ParticleProvider(PackOutput output) {
        this.pathProvider = output.createPathProvider(PackOutput.Target.RESOURCE_PACK, "particles");
    }

    public @NotNull CompletableFuture<?> run(@NotNull CachedOutput cache) {
        clear();
        addParticles();

        if (data.isEmpty()) throw new IllegalStateException("The structure data is empty!");
        CompletableFuture<?>[] futures = new CompletableFuture<?>[this.data.size()];

        int i = 0;
        for (var particle : data.entrySet()) {
            JsonObject JSONProperties = new JsonObject();
            JsonArray array = new JsonArray();
            for (var entry : particle.getValue()) {
                array.add(entry.toString());
            }

            JSONProperties.add("textures", array);
            futures[i++] = DataProvider.saveStable(cache, JSONProperties, pathProvider.file(UltimineAddition.getLocation(particle.getKey()), "json"));
        }

        return CompletableFuture.allOf(futures);
    }

    public void add(SimpleParticleType particle, ResourceLocation... texture) {
        var particleName = Objects.requireNonNull(Registration.PARTICLE_TYPES.getRegistrar().getId(particle)).getPath();
        if (data.containsKey(particleName)) {
            throw new IllegalStateException("Duplicate particle " + particleName);
        } else data.put(particleName, Arrays.stream(texture).toList());
    }

    private void clear() {
        data.clear();
    }

    public final @NotNull String getName() {
        return "Particles";
    }
}
