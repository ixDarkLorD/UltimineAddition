package net.ixdarklord.ultimine_addition.datagen.challenge;

import com.google.gson.JsonObject;
import com.mojang.serialization.JsonOps;
import net.ixdarklord.ultimine_addition.common.data.challenge.ChallengeData;
import net.ixdarklord.ultimine_addition.datagen.challenge.builder.ChallengesBuilder;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

import static net.ixdarklord.ultimine_addition.common.item.MiningSkillCardItem.Type.*;

public abstract class ChallengeProvider implements DataProvider {
    private final PackOutput output;
    private final Map<ResourceLocation, ChallengeData> data = new TreeMap<>();

    protected abstract void buildChallenges(Consumer<ChallengesBuilder.Result> consumer);

    public ChallengeProvider(PackOutput output) {
        this.output = output;
    }

    @Override
    public @NotNull CompletableFuture<?> run(@NotNull CachedOutput cache) {
        clear();
        buildChallenges(result -> {
            if (data.containsKey(result.id())) {
                throw new IllegalStateException("Duplicate challenge " + result.id().toString());
            } else data.put(result.id(), result.data());
        });

        if (this.data.isEmpty()) throw new IllegalStateException("The challenges data is empty!");
        CompletableFuture<?>[] futures = new CompletableFuture<?>[this.data.size()];

        int i = 0;
        for (var data : data.entrySet()) {
            AtomicReference<JsonObject> JSONProperties = new AtomicReference<>(new JsonObject());
            ChallengeData.CODEC.encodeStart(JsonOps.INSTANCE, data.getValue()).result().ifPresent(json -> JSONProperties.set(json.getAsJsonObject()));
            futures[i++] = DataProvider.saveStable(cache, JSONProperties.get(), pathProvider(data.getValue()).json(data.getKey()));
        }

        return CompletableFuture.allOf(futures);
    }

    private PackOutput.PathProvider pathProvider(ChallengeData value) {
        String path = "";
        if (value.forCardType().equals(PICKAXE)) {
            path = "/pickaxe";
        } else if (value.forCardType().equals(AXE)) {
            path = "/axe";
        } else if (value.forCardType().equals(SHOVEL)) {
            path = "/shovel";
        } else if (value.forCardType().equals(HOE)) {
            path = "/hoe";
        }
        return output.createPathProvider(PackOutput.Target.DATA_PACK, "challenges" + path);
    }

    private void clear() {
        data.clear();
    }

    @Override
    public final @NotNull String getName() {
        return "Ultimine Addition Challenges";
    }
}
