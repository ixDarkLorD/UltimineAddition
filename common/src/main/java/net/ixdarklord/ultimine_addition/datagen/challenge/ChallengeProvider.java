package net.ixdarklord.ultimine_addition.datagen.challenge;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.mojang.serialization.JsonOps;
import net.ixdarklord.ultimine_addition.common.data.challenge.ChallengesData;
import net.ixdarklord.ultimine_addition.datagen.helper.DataGeneratorHelper;
import net.ixdarklord.ultimine_addition.datagen.challenge.builder.ChallengesBuilder;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.DataProvider;
import net.minecraft.data.HashCache;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

import static net.ixdarklord.ultimine_addition.common.item.MiningSkillCardItem.Type.*;

public abstract class ChallengeProvider implements DataProvider {
    private static final Gson GSON = (new GsonBuilder()).setPrettyPrinting().create();
    private final Map<ResourceLocation, ChallengesData> data = new TreeMap<>();
    private final Path path;

    protected abstract void buildChallenges(Consumer<ChallengesBuilder.Result> consumer);

    public ChallengeProvider(DataGenerator generator) {
        this.path = generator.getOutputFolder();
    }

    @Override
    public void run(@NotNull HashCache cache) throws IOException {
        clear();
        buildChallenges(result -> {
            if (data.containsKey(result.id())) {
                throw new IllegalStateException("Duplicate challenge " + result.id().toString());
            } else data.put(result.id(), result.data());
        });

        if (this.data.isEmpty()) throw new IllegalStateException("The challenges data is empty!");
        generateChallenge(cache);
    }

    private void generateChallenge(HashCache cache) throws IOException {
        for (var data : data.entrySet()) {
            AtomicReference<JsonObject> JSONProperties = new AtomicReference<>(new JsonObject());
            ChallengesData.CODEC.encodeStart(JsonOps.INSTANCE, data.getValue()).result().ifPresent(json -> JSONProperties.set(json.getAsJsonObject()));
            Path path = DataGeneratorHelper.createDataPath(this.path, "challenges" + pathProvider(data.getValue()), data.getKey());
            DataProvider.save(GSON, cache, JSONProperties.get(), path);
        }
    }

    private String pathProvider(ChallengesData value) {
        String path = "";
        if (value.getForCardType().equals(PICKAXE)) {
            path = "/pickaxe";
        } else if (value.getForCardType().equals(AXE)) {
            path = "/axe";
        } else if (value.getForCardType().equals(SHOVEL)) {
            path = "/shovel";
        } else if (value.getForCardType().equals(HOE)) {
            path = "/hoe";
        }
        return path;
    }

    private void clear() {
        data.clear();
    }

    @Override
    public @NotNull String getName() {
        return "Challenges";
    }
}
