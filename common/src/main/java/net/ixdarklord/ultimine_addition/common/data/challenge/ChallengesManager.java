package net.ixdarklord.ultimine_addition.common.data.challenge;

import net.ixdarklord.coolcatcore.api.platform.Platform;
import net.ixdarklord.ultimine_addition.config.UAServerConfig;
import net.minecraft.resources.FileToIdConverter;
import net.minecraft.util.ExtraCodecs;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.mojang.serialization.JsonOps;
import net.ixdarklord.ultimine_addition.common.data.card.DataCardTypes;
import net.ixdarklord.ultimine_addition.common.item.MiningSkillCardItem;
import net.ixdarklord.ultimine_addition.util.ItemUtils;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.tags.TagKey;
import net.minecraft.util.Mth;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import org.apache.commons.compress.utils.Lists;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import static net.ixdarklord.ultimine_addition.common.item.MiningSkillCardItem.Type.*;
import static net.ixdarklord.ultimine_addition.core.FTBUltimineAddition.LOGGER;

public class ChallengesManager extends SimpleJsonResourceReloadListener<JsonElement> {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
    public static ChallengesManager INSTANCE = new ChallengesManager();
    private Map<Identifier, ChallengeData> challenges = new TreeMap<>();

    public ChallengesManager() {
        super(ExtraCodecs.JSON, FileToIdConverter.json("challenges"));
    }

    @Override
    protected void apply(@NotNull Map<Identifier, JsonElement> object, @NotNull ResourceManager resourceManager, @NotNull ProfilerFiller profiler) {
        // Challenges name card types: the data packs' own are read first.
        DataCardTypes.load(resourceManager);
        challenges.clear();
        object.forEach((location, json) -> {
            AtomicReference<ChallengeData> challenge = new AtomicReference<>();
            challenge.set(ChallengeData.CODEC.parse(JsonOps.INSTANCE, json).getOrThrow(err ->
                    new IllegalStateException("There is an issue with (%s) challenge JSON file.\n".formatted(location.toString()) + err)));
            if (challenge.get().challengeType() == ChallengeData.Type.INTERACT_WITH_BLOCK || challenge.get().challengeType() == ChallengeData.Type.INTERACT_WITH_BLOCK_CONSUME) {
                LOGGER.warn("This challenge type ({}) you choose in ({}) isn't stable! You may have to change the type until a new update comes to fix it.", challenge.get().challengeType().getTypeId(), location.toString());
            }
            challenges.put(location, challenge.get());
        });
        this.dropCardTypesWithoutChallenges();
    }

    // A data pack card type needs challenges for every tier it climbs: a card with none to roll would have nothing to
    // do. Such a type is left out (its cards are inert, like those of a removed type) and the log says which tier.
    private void dropCardTypesWithoutChallenges() {
        List<MiningSkillCardItem.Type> kept = new ArrayList<>();
        for (MiningSkillCardItem.Type type : MiningSkillCardItem.Type.getDataTypes()) {
            MiningSkillCardItem.Tier missing = null;
            for (MiningSkillCardItem.Tier tier : List.of(MiningSkillCardItem.Tier.Unlearned, MiningSkillCardItem.Tier.Novice, MiningSkillCardItem.Tier.Apprentice, MiningSkillCardItem.Tier.Adept)) {
                if (this.challenges.values().stream().noneMatch(data -> data.forCardType().equals(type) && data.forCardTier().isEligible(tier))) {
                    missing = tier;
                    break;
                }
            }
            if (missing == null) {
                kept.add(type);
            } else {
                LOGGER.error("Skipping the Mining Skill Card type {}: it has no challenges for the {} tier (challenge files with \"for_card_type\": \"{}\")",
                        type.getId(), missing.name().toLowerCase(), type.getId());
            }
        }
        MiningSkillCardItem.Type.setDataTypes(kept);
    }

    public Map<Identifier, ChallengeData> getRandomChallenges(int quantity, MiningSkillCardItem.Type type, MiningSkillCardItem.Tier tier) {
        int available = challenges.values().stream().filter(data -> (data.forCardType().equals(type) && data.forCardTier().isEligible(tier))).toList().size();
        if (available > 0 && quantity > available) {
            // Fewer challenges than the tier rolls (the server config's challenges_amount): the card gets them all.
            LOGGER.warn("Only {} {} challenge(s) for tier {}, not the {} a card rolls: using all of them", available, type.getId(), tier.name().toLowerCase(), quantity);
            quantity = available;
        }
        if (quantity > available) {
            String error = String.format("There aren't enough %s %s challenges for tier %s to add it to Mining Skill Card.", quantity, type.getId().toLowerCase(), tier.name().toLowerCase());
            throw new IllegalArgumentException(error);
        }

        Map<Identifier, ChallengeData> randomValues = new HashMap<>();
        Random random = new Random();
        while (randomValues.size() < quantity) {
            int randomIndex = random.nextInt(challenges.size());
            Identifier[] keys = challenges.keySet().toArray(new Identifier[0]);
            Identifier randomKey = keys[randomIndex];
            if (challenges.get(randomKey).forCardType().equals(type) && challenges.get(randomKey).forCardTier().isEligible(tier)) {
                randomValues.put(randomKey, challenges.get(randomKey));
            }
        }
        if (UAServerConfig.CHALLENGE_MANAGER_LOGGER.get() || Platform.isDevelopmentEnvironment()) {
            LOGGER.debug("/----------[Challenge Tracker]-----------/");
            LOGGER.debug("| Added Challenges:");
            randomValues.forEach((location, data) -> LOGGER.debug("|> ID: {}", location));
            LOGGER.debug("/----------------------------------------/");
        }
        return randomValues;
    }

    public Optional<ChallengeData> getChallengeData(Identifier id) {
        return Optional.ofNullable(this.getAllChallenges().get(id));
    }

    public Map<Identifier, ChallengeData> getAllChallenges() {
        return this.challenges;
    }

    public void validateAllChallenges() {
        List<Identifier> markedToRemove = new ArrayList<>();
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

    public List<Block> utilizeTargetedBlocks(ChallengeData data) {
        List<Block> list = new ArrayList<>();
        if (data.targetedBlocks() == null) return new ArrayList<>();
        for (String value : data.targetedBlocks()) {
            if (value.startsWith("#")) {
                List<Block> blocks = new ArrayList<>();
                BuiltInRegistries.BLOCK.get(TagKey.create(Registries.BLOCK, Identifier.parse(value.replaceAll("#", "")))).ifPresent(holders ->
                        blocks.addAll(holders.stream().map(Holder::value).toList()));
                if (!blocks.isEmpty()) list.addAll(blocks);
            } else {
                Block block = BuiltInRegistries.BLOCK.getValue(Identifier.parse(value));
                if (block != Blocks.AIR) list.add(block);
            }
        }
        return list;
    }

    public boolean isCorrectTool(Player player, ChallengeData challengeData) {
        if (challengeData.forCardType().isCustomType()) {
            // The tools of this challenge's own type, not of any data pack type.
            return challengeData.forCardType().utilizeRequiredTools().contains(ItemUtils.getItemInHand(player, true).getItem());
        } else if (challengeData.forCardType().equals(PICKAXE)) {
            return ItemUtils.isItemInHandPickaxe(player);
        } else if (challengeData.forCardType().equals(AXE)) {
            return ItemUtils.isItemInHandAxe(player);
        } else if (challengeData.forCardType().equals(SHOVEL)) {
            return ItemUtils.isItemInHandShovel(player);
        } else if (challengeData.forCardType().equals(HOE)) {
            return ItemUtils.isItemInHandHoe(player);
        }
        return false;
    }

    public void setChallenges(Map<Identifier, ChallengeData> dataMap) {
        this.challenges = dataMap;
    }

    public List<Component> createChallengeDescription(Identifier id, Style style) {
        return this.createChallengeDescription(id, style, -1.0F, null);
    }

    public List<Component> createChallengeDescription(Identifier id, Style style, float cycle, Style cycleStyle) {
        List<Component> components = Lists.newArrayList();
        ChallengeData data = this.challenges.get(id);
        if (data == null) {
            return components;
        } else {
            List<Item> items = this.utilizeTargetedBlocks(data).stream().map(Block::asItem).toList();
            if (items.isEmpty()) {
                return components;
            } else if (items.size() == 1) {
                ItemStack stack = items.getFirst().getDefaultInstance();
                return Collections.singletonList(Component.translatable("challenge.ultimine_addition.%s".formatted(data.challengeType().getTypeId()), new Object[]{stack.getHoverName()}).append("."));
            } else {
                components.add(Component.translatable("challenge.ultimine_addition.%s".formatted(data.challengeType().getTypeId()), Component.translatable("challenge.ultimine_addition.various_blocks").append(":")));
                if (cycle != -1.0F) {
                    ItemStack stack = items.get(Mth.floor(cycle) % items.size()).getDefaultInstance();
                    components.add(Component.literal("• ").append(stack.getHoverName()).withStyle(cycleStyle));
                } else {
                    for(Item item : items) {
                        ItemStack stack = item.getDefaultInstance();
                        components.add(Component.literal("• ").append(stack.getHoverName()).withStyle(style));
                    }
                }

                return components;
            }
        }
    }
}
