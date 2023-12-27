package net.ixdarklord.ultimine_addition.common.data.challenge;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.mojang.serialization.JsonOps;
import dev.architectury.platform.Platform;
import net.ixdarklord.ultimine_addition.common.config.ConfigHandler;
import net.ixdarklord.ultimine_addition.common.item.MiningSkillCardItem;
import net.ixdarklord.ultimine_addition.core.Constants;
import net.ixdarklord.ultimine_addition.core.ServicePlatform;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.tags.TagKey;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DiggerItem;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import static net.ixdarklord.ultimine_addition.common.item.MiningSkillCardItem.Type.*;

public class ChallengesManager extends SimpleJsonResourceReloadListener {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
    public static ChallengesManager INSTANCE = new ChallengesManager();
    public static final Logger LOGGER = LoggerFactory.getLogger(Constants.MOD_NAME + "/ChallengesManager");
    private Map<ResourceLocation, ChallengesData> challenges = new TreeMap<>();

    public ChallengesManager() {
        super(GSON, "challenges");
    }

    @Override
    protected void apply(@NotNull Map<ResourceLocation, JsonElement> object, @NotNull ResourceManager resourceManager, @NotNull ProfilerFiller profiler) {
        challenges.clear();
        object.forEach((location, json) -> {
            AtomicReference<ChallengesData> challenge = new AtomicReference<>();
            challenge.set(ChallengesData.CODEC.parse(JsonOps.INSTANCE, json).getOrThrow(false, err -> {
                LOGGER.error("There is an issue with ({}) challenge JSON file.", location.toString());
                LOGGER.error(err);
            }));
            if (challenge.get().getChallengeType() == ChallengesData.Type.INTERACT_WITH_BLOCK || challenge.get().getChallengeType() == ChallengesData.Type.INTERACT_WITH_BLOCK_CONSUME) {
                LOGGER.warn("This challenge type ({}) you choose in ({}) isn't stable! You may have to change the type until a new update comes to fix it.", challenge.get().getChallengeType().getTypeName(), location.toString());
            }
            challenges.put(location, challenge.get());
        });
    }

    public Map<ResourceLocation, ChallengesData> getRandomChallenges(int quantity, MiningSkillCardItem.Type type, MiningSkillCardItem.Tier tier) {
        if (quantity > challenges.values().stream().filter(data -> (data.getForCardType().equals(type) && data.getForCardTier().isEligible(tier))).toList().size()) {
            String error = String.format("There aren't enough %s %s challenges for tier %s to add it to Mining Skill Card.", quantity, type.getId().toLowerCase(), tier.name().toLowerCase());
            throw new RuntimeException(error);
        }

        Map<ResourceLocation, ChallengesData> randomValues = new HashMap<>();
        Random random = new Random();
        while (randomValues.size() < quantity) {
            int randomIndex = random.nextInt(challenges.size());
            ResourceLocation[] keys = challenges.keySet().toArray(new ResourceLocation[0]);
            ResourceLocation randomKey = keys[randomIndex];
            if (challenges.get(randomKey).getForCardType().equals(type) && challenges.get(randomKey).getForCardTier().isEligible(tier)) {
                randomValues.put(randomKey, challenges.get(randomKey));
            }
        }
        if (ConfigHandler.COMMON.CHALLENGE_MANAGER_LOGGER.get() || Platform.isDevelopmentEnvironment()) {
            LOGGER.info("Added Challenges:");
            randomValues.forEach((location, data) -> LOGGER.info("id: {}", location));
        }
        return randomValues;
    }

    public Map<ResourceLocation, ChallengesData> getAllChallenges() {
        return this.challenges;
    }

    public void validateAllChallenges() {
        List<ResourceLocation> markedToRemove = new ArrayList<>();
        challenges.forEach((location, challengesData) -> {
            AtomicBoolean isValid = new AtomicBoolean();
            var blocks = utilizeTargetedBlocks(challengesData);
            if (!blocks.isEmpty()) isValid.set(true);
            if (!isValid.get()) {
                markedToRemove.add(location);
                LOGGER.error("There is no valid targeted blocks in ({}) challenge JSON file.", location.toString());
            }
        });
        if (markedToRemove.isEmpty()) {
            LOGGER.info("Loaded {} challenges", challenges.size());
        } else {
            int oldSize = challenges.size();
            markedToRemove.forEach(challenges::remove);
            LOGGER.info("Loaded {} from {} challenges", challenges.size(), oldSize);
        }
    }

    public List<Block> utilizeTargetedBlocks(ChallengesData data) {
        List<Block> list = new ArrayList<>();
        if (data.getTargetedBlocks() == null) return new ArrayList<>();
        for (String value : data.getTargetedBlocks()) {
            if (value.startsWith("#")) {
                List<Block> blocks = new ArrayList<>();
                Registry.BLOCK.getTag(TagKey.create(Registry.BLOCK_REGISTRY, new ResourceLocation(value.replaceAll("#", "")))).ifPresent(holders ->
                        blocks.addAll(holders.stream().map(Holder::value).toList()));
                if (!blocks.isEmpty()) list.addAll(blocks);
            } else {
                Block block = Registry.BLOCK.get(new ResourceLocation(value));
                if (block != Blocks.AIR) list.add(block);
            }
        }
        return list;
    }

    public boolean isCorrectTool(Player player, ChallengesData challengesData) {
        if (challengesData.getForCardType().isCustomType()) {
            return challengesData.getForCardType().utilizeRequiredTools().contains(player.getInventory().getSelected().getItem());
        } else if (challengesData.getForCardType().equals(PICKAXE)) {
            return player.getInventory().getSelected().is(ServicePlatform.Tags.getPickaxes());
        } else if (challengesData.getForCardType().equals(AXE)) {
            return player.getInventory().getSelected().is(ServicePlatform.Tags.getAxes());
        } else if (challengesData.getForCardType().equals(SHOVEL)) {
            return player.getInventory().getSelected().is(ServicePlatform.Tags.getShovels());
        } else if (challengesData.getForCardType().equals(HOE)) {
            return player.getInventory().getSelected().is(ServicePlatform.Tags.getHoes());
        } else if (challengesData.getForCardType().equals(EMPTY)) {
            return !(player.getInventory().getSelected().getItem() instanceof DiggerItem);
        }
        return false;
    }

    public void setChallenges(Map<ResourceLocation, ChallengesData> dataMap) {
        this.challenges = dataMap;
    }
}
