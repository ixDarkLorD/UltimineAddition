package net.ixdarklord.ultimine_addition.common.data.challenge;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.mojang.serialization.JsonOps;
import dev.architectury.platform.Platform;
import net.ixdarklord.ultimine_addition.common.item.MiningSkillCardItem;
import net.ixdarklord.ultimine_addition.config.ConfigHandler;
import net.ixdarklord.ultimine_addition.util.ItemUtils;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

public class ChallengesManager extends SimpleJsonResourceReloadListener {
    private static final Gson GSON = (new GsonBuilder()).setPrettyPrinting().disableHtmlEscaping().create();
    public static ChallengesManager INSTANCE = new ChallengesManager();
    public static final Logger LOGGER = LoggerFactory.getLogger("Ultimine Addition/ChallengesManager");
    private Map<ResourceLocation, ChallengeData> challenges = new TreeMap<>();

    public ChallengesManager() {
        super(GSON, "challenges");
    }

    protected void apply(@NotNull Map<ResourceLocation, JsonElement> object, @NotNull ResourceManager resourceManager, @NotNull ProfilerFiller profiler) {
        this.challenges.clear();
        object.forEach((location, json) -> {
            AtomicReference<ChallengeData> challenge = new AtomicReference<>();
            challenge.set(ChallengeData.CODEC.parse(JsonOps.INSTANCE, json).getOrThrow(false, (err) -> {
                LOGGER.error("There is an issue with ({}) challenge JSON file.", location.toString());
                LOGGER.error(err);
            }));
            if (challenge.get().challengeType() == ChallengeData.Type.INTERACT_WITH_BLOCK || challenge.get().challengeType() == ChallengeData.Type.INTERACT_WITH_BLOCK_CONSUME) {
                LOGGER.warn("This challenge type ({}) you choose in ({}) isn't stable! You may have to change the type until a new update comes to fix it.", challenge.get().challengeType().getTypeId(), location.toString());
            }

            this.challenges.put(location, challenge.get());
        });
    }

    public Map<ResourceLocation, ChallengeData> getRandomChallenges(int quantity, MiningSkillCardItem.Type type, MiningSkillCardItem.Tier tier) {
        if (quantity > this.challenges.values().stream().filter((data) -> data.forCardType().equals(type) && data.forCardTier().isEligible(tier)).toList().size()) {
            String error = String.format("There aren't enough %s %s challenges for tier %s to add it to Mining Skill Card.", quantity, type.id().toLowerCase(), tier.name().toLowerCase());
            LOGGER.error(error);
            return new HashMap<>();
        } else {
            Map<ResourceLocation, ChallengeData> randomValues = new HashMap<>();
            Random random = new Random();

            while (randomValues.size() < quantity) {
                int randomIndex = random.nextInt(this.challenges.size());
                ResourceLocation[] keys = this.challenges.keySet().toArray(new ResourceLocation[0]);
                ResourceLocation randomKey = keys[randomIndex];
                if (this.challenges.get(randomKey).forCardType().equals(type) && this.challenges.get(randomKey).forCardTier().isEligible(tier)) {
                    randomValues.put(randomKey, this.challenges.get(randomKey));
                }
            }

            if (ConfigHandler.SERVER.CHALLENGE_MANAGER_LOGGER.get() || Platform.isDevelopmentEnvironment()) {
                LOGGER.info("Added Challenges:");
                randomValues.forEach((location, data) -> LOGGER.info("id: {}", location));
            }

            return randomValues;
        }
    }

    public Optional<ChallengeData> getChallengeData(ResourceLocation id) {
        return Optional.ofNullable(this.getAllChallenges().get(id));
    }

    public Map<ResourceLocation, ChallengeData> getAllChallenges() {
        return this.challenges;
    }

    public void validateAllChallenges() {
        List<ResourceLocation> markedToRemove = new ArrayList<>();
        this.challenges.forEach((location, challengesData) -> {
            AtomicBoolean isValid = new AtomicBoolean();
            List<Block> blocks = this.utilizeTargetedBlocks(challengesData);
            if (!blocks.isEmpty()) {
                isValid.set(true);
            }

            if (!isValid.get()) {
                markedToRemove.add(location);
                LOGGER.error("There is no valid targeted blocks in ({}) challenge JSON file.", location.toString());
            }

        });
        if (markedToRemove.isEmpty()) {
            LOGGER.info("Loaded {} challenges", this.challenges.size());
        } else {
            int oldSize = this.challenges.size();
            var map = this.challenges;
            Objects.requireNonNull(map);
            markedToRemove.forEach(map::remove);
            LOGGER.info("Loaded {} from {} challenges", this.challenges.size(), oldSize);
        }

    }

    public List<Block> utilizeTargetedBlocks(ChallengeData data) {
        List<Block> list = new ArrayList<>();
        if (data.targetedBlocks() == null) {
            return new ArrayList<>();
        } else {
            for (String value : data.targetedBlocks()) {
                if (value.startsWith("#")) {
                    List<Block> blocks = new ArrayList<>();
                    BuiltInRegistries.BLOCK.getTag(TagKey.create(Registries.BLOCK, new ResourceLocation(value.replace("#", "")))).ifPresent((holders) -> blocks.addAll(holders.stream().map(Holder::value).toList()));
                    if (!blocks.isEmpty()) {
                        list.addAll(blocks);
                    }
                } else {
                    Block block = BuiltInRegistries.BLOCK.get(new ResourceLocation(value));
                    if (block != Blocks.AIR) {
                        list.add(block);
                    }
                }
            }

            return list;
        }
    }

    public boolean isCorrectTool(Player player, ChallengeData challengeData) {
        if (challengeData.forCardType().isCustomType()) {
            return ItemUtils.isItemInHandCustomCardValid(player);
        } else if (challengeData.forCardType().equals(MiningSkillCardItem.Type.PICKAXE)) {
            return ItemUtils.isItemInHandPickaxe(player);
        } else if (challengeData.forCardType().equals(MiningSkillCardItem.Type.AXE)) {
            return ItemUtils.isItemInHandAxe(player);
        } else if (challengeData.forCardType().equals(MiningSkillCardItem.Type.SHOVEL)) {
            return ItemUtils.isItemInHandShovel(player);
        } else {
            return challengeData.forCardType().equals(MiningSkillCardItem.Type.HOE) && ItemUtils.isItemInHandHoe(player);
        }
    }

    public void setChallenges(Map<ResourceLocation, ChallengeData> dataMap) {
        this.challenges = dataMap;
    }

    public List<Component> createChallengeDescription(ResourceLocation id, Style style) {
        return this.createChallengeDescription(id, style, -1.0F, null);
    }

    public List<Component> createChallengeDescription(ResourceLocation id, Style style, float cycle, Style cycleStyle) {
        List<Component> components = Lists.newArrayList();
        ChallengeData data = this.challenges.get(id);
        if (data == null) {
            return components;
        } else {
            List<Item> items = this.utilizeTargetedBlocks(data).stream().map(Block::asItem).toList();
            if (items.isEmpty()) {
                return components;
            } else if (items.size() == 1) {
                ItemStack stack = items.get(0).getDefaultInstance();
                return Collections.singletonList(Component.translatable("challenge.ultimine_addition.%s".formatted(data.challengeType().getTypeId()), new Object[]{stack.getHoverName()}).append("."));
            } else {
                components.add(Component.translatable("challenge.ultimine_addition.%s".formatted(data.challengeType().getTypeId()), Component.translatable("challenge.ultimine_addition.various_blocks").append(":")));
                if (cycle != -1.0F) {
                    ItemStack stack = items.get(Mth.floor(cycle) % items.size()).getDefaultInstance();
                    components.add(Component.literal("• ").append(stack.getHoverName()).withStyle(cycleStyle));
                } else {
                    for (Item item : items) {
                        ItemStack stack = item.getDefaultInstance();
                        components.add(Component.literal("• ").append(stack.getHoverName()).withStyle(style));
                    }
                }

                return components;
            }
        }
    }
}
