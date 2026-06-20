package net.ixdarklord.ultimine_addition.common.data.item;

import com.google.common.collect.Lists;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.architectury.platform.Platform;
import net.ixdarklord.coolcatlib.api.data.ItemDataComponent;
import net.ixdarklord.coolcatlib.api.utils.CodecUtils;
import net.ixdarklord.coolcatlib.api.utils.ItemStackHelper;
import net.ixdarklord.ultimine_addition.client.gui.toasts.ChallengesToast;
import net.ixdarklord.ultimine_addition.common.data.challenge.ChallengesManager;
import net.ixdarklord.ultimine_addition.common.item.MiningSkillCardItem;
import net.ixdarklord.ultimine_addition.config.ConfigHandler;
import net.ixdarklord.ultimine_addition.core.FTBUltimineAddition;
import net.ixdarklord.ultimine_addition.network.PacketHandler;
import net.ixdarklord.ultimine_addition.network.packets.MiningSkillCardPacket;
import net.minecraft.core.UUIDUtil;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.ApiStatus.Internal;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public final class MiningSkillCardData extends ItemDataComponent<MiningSkillCardData> {
    public static final ResourceLocation DATA_ID = FTBUltimineAddition.id("mining_skill_card_data");
    public static final Codec<MiningSkillCardData> CODEC = RecordCodecBuilder.create((instance) -> instance.group(UUIDUtil.CODEC.optionalFieldOf("UUID", UUID.randomUUID()).forGetter(MiningSkillCardData::getUUID), MiningSkillCardItem.Tier.CODEC.fieldOf("Tier").forGetter(MiningSkillCardData::getTier), ItemStack.CODEC.fieldOf("DisplayItem").forGetter(MiningSkillCardData::getDisplayItem), ExtraCodecs.NON_NEGATIVE_INT.optionalFieldOf("PotionPoints", 0).forGetter(MiningSkillCardData::getPotionPoints), MiningSkillCardData.Challenge.CODEC.listOf().optionalFieldOf("Challenges", Lists.newArrayList()).forGetter(MiningSkillCardData::getChallenges)).apply(instance, MiningSkillCardData::new));
    private final @NotNull UUID uuid;
    private MiningSkillCardItem.Tier tier;
    private ItemStack displayItem;
    private int potionPoints;
    private final List<Challenge> challenges;
    private final List<Challenge> finishedChallenges = Lists.newArrayList();

    private MiningSkillCardData(@NotNull UUID uuid, MiningSkillCardItem.Tier tier, ItemStack displayItem, int potionPoints, List<Challenge> challenges) {
        super(DATA_ID, CODEC);
        this.uuid = uuid;
        this.tier = tier;
        this.displayItem = displayItem;
        this.potionPoints = potionPoints;
        this.challenges = Lists.newArrayList(challenges);
    }

    public static MiningSkillCardData create(MiningSkillCardItem.Type type) {
        return new MiningSkillCardData(UUID.randomUUID(), MiningSkillCardItem.Tier.Unlearned, type.defaultDisplayItem().getDefaultInstance(), 0, Lists.newArrayList());
    }

    @Internal
    public static ItemStack createForCreativeTab(MiningSkillCardItem cardItem, MiningSkillCardItem.Tier tier) {
        ItemStack stack = cardItem.getDefaultInstance();
        MiningSkillCardData data = new MiningSkillCardData(UUID.fromString("00000000-0000-0000-0000-000000000000"), tier, cardItem.getType().defaultDisplayItem().getDefaultInstance(), 0, Lists.newArrayList());
        data.stack = stack;
        data.save();
        return stack;
    }

    public static MiningSkillCardData load(ItemStack stack) {
        MiningSkillCardItem.Type type = MiningSkillCardItem.Type.EMPTY;
        Item item = stack.getItem();
        if (item instanceof MiningSkillCardItem cardItem) {
            type = cardItem.getType();
        }

        CompoundTag tag = stack.getOrCreateTag().getCompound(DATA_ID.toString());
        MiningSkillCardData data = tag.isEmpty() ? create(type) : CodecUtils.decode(CODEC, tag);
        return data.setStack(stack);
    }

    public static boolean hasData(ItemStack stack) {
        if (stack != null && !stack.isEmpty() && stack.getItem() instanceof MiningSkillCardItem) {
            CompoundTag tag = stack.getOrCreateTag().getCompound(DATA_ID.toString());
            return !tag.isEmpty();
        } else {
            return false;
        }
    }

    public MiningSkillCardData onClientUpdate() {
        for (Challenge finishedChallenge : this.finishedChallenges) {
            ChallengesToast.run(finishedChallenge, this.stack);
        }

        this.finishedChallenges.clear();
        return this;
    }

    public MiningSkillCardData onServerUpdate() {
        this.finishedChallenges.clear();
        return this;
    }

    public MiningSkillCardData initChallenges() {
        if (!(this.stack.getItem() instanceof MiningSkillCardItem)) {
            FTBUltimineAddition.LOGGER.error("You've tried to initiate challenges on item can't accept it: {}", this.stack.getDescriptionId());
            return this;
        } else if (this.tier == MiningSkillCardItem.Tier.Mastered) {
            this.challenges.clear();
            return this;
        } else {
            AtomicInteger slotId = new AtomicInteger(1);
            AtomicInteger quantity = new AtomicInteger();
            MiningSkillCardItem.Type type = ((MiningSkillCardItem) this.stack.getItem()).getType();
            quantity.set(ConfigHandler.SERVER.CARD_CHALLENGES_AMOUNT.getValue(this.tier));
            if (this.tier != MiningSkillCardItem.Tier.Unlearned && this.tier != MiningSkillCardItem.Tier.Mastered) {
                this.resetPotionPoints();
            }

            this.challenges.clear();
            ChallengesManager.INSTANCE.getRandomChallenges(quantity.get(), type, this.tier).forEach((location, data) -> {
                this.challenges.add(new Challenge(location, slotId.get(), data.getRequiredAmount()));
                slotId.getAndIncrement();
            });
            return this;
        }
    }

    public boolean validateChallenges() {
        if (this.tier == MiningSkillCardItem.Tier.Mastered) {
            return false;
        } else {
            MiningSkillCardItem.Type type = ((MiningSkillCardItem) this.stack.getItem()).getType();
            if (!this.challenges.isEmpty()) {
                Collection<Challenge> removedChallenges = new TreeSet<>();
                this.challenges.forEach((challengeData) -> {
                    if (!ChallengesManager.INSTANCE.getAllChallenges().containsKey(challengeData.id)) {
                        removedChallenges.add(challengeData);
                    }

                });
                removedChallenges.forEach((challengeData) -> {
                    AtomicBoolean isDone = new AtomicBoolean(true);
                    this.challenges.remove(challengeData);

                    do {
                        ChallengesManager.INSTANCE.getRandomChallenges(1, type, this.tier).forEach((location, data) -> {
                            if (this.challenges.stream().filter((challengeData1) -> challengeData1.id.equals(location)).toList().isEmpty()) {
                                this.challenges.add(new Challenge(location, challengeData.order, data.getRequiredAmount()));
                                if (ConfigHandler.SERVER.CHALLENGE_MANAGER_LOGGER.get() || Platform.isDevelopmentEnvironment()) {
                                    FTBUltimineAddition.LOGGER.debug("Changing the invalid challenge! id:\"{}\" to: id:\"{}\"", challengeData.id, location);
                                }

                                isDone.set(false);
                            }

                        });
                    } while (isDone.get());

                });
                return !removedChallenges.isEmpty();
            } else {
                this.initChallenges();
                return true;
            }
        }
    }

    public MiningSkillCardData setDisplayItem(ItemStack stack) {
        this.displayItem = stack;
        return this;
    }

    public MiningSkillCardData addAmount(ResourceLocation challengeId, int value) {
        Optional<Challenge> challengeData = this.getChallenge(challengeId);
        if (challengeData.isEmpty()) {
            return this;
        } else {
            int currentAmount = challengeData.get().currentPoints;
            int requiredAmount = challengeData.get().requiredPoints;
            return currentAmount >= requiredAmount ? this : this.setAmount(challengeId, currentAmount + value);
        }
    }

    public void accomplishChallenge(ResourceLocation challengeId) {
        Optional<Challenge> challengeData = this.getChallenge(challengeId);
        challengeData.ifPresent(challenge -> this.setAmount(challengeId, challenge.requiredPoints));
    }

    public MiningSkillCardData setAmount(ResourceLocation challengeId, int value) {
        Optional<Challenge> challengeData = this.getChallenge(challengeId);
        if (challengeData.isPresent()) {
            Challenge challenge = challengeData.get();
            int requiredAmount = challenge.requiredPoints;
            challenge.currentPoints = Math.min(value, requiredAmount);
            this.checkChallengeAccomplishment(challenge);
        }
        return this;
    }

    public MiningSkillCardData togglePinned(ResourceLocation challengeId) {
        this.getChallenge(challengeId).ifPresent((challenge) -> challenge.isPinned ^= true);
        return this;
    }

    public void setPotionPoints(int value) {
        this.potionPoints = value;
    }

    public MiningSkillCardData consumePotionPoint(int value) {
        this.potionPoints = Math.max(0, this.potionPoints - value);
        return this;
    }

    public void resetPotionPoints() {
        this.setPotionPoints(this.getMaxPotionPoints());
    }

    public MiningSkillCardData setTier(MiningSkillCardItem.Tier tier) {
        this.tier = tier;
        return this;
    }

    public ItemStack getDisplayItem() {
        return this.displayItem;
    }

    public Optional<Challenge> getChallenge(ResourceLocation challengeId) {
        for (Challenge challenge : this.challenges) {
            if (challenge.id.equals(challengeId)) {
                return Optional.of(challenge);
            }
        }

        return Optional.empty();
    }

    private void checkChallengeAccomplishment(Challenge challenge) {
        if (this.isAllChallengesCompleted()) {
            this.tier = this.tier.next();
            this.initChallenges();
            this.finishedChallenges.add(new Challenge());
        } else {
            if (this.isChallengeAccomplished(challenge) && !this.finishedChallenges.contains(challenge)) {
                this.finishedChallenges.add(challenge);
            }

        }
    }

    public boolean isChallengeAccomplished(Challenge challenge) {
        return this.isChallengeAccomplished(challenge.id);
    }

    public boolean isChallengeAccomplished(ResourceLocation challengeId) {
        Optional<Challenge> challengeData = this.getChallenge(challengeId);
        return challengeData.filter((data) -> data.currentPoints >= data.requiredPoints).isPresent();
    }

    public boolean isAllChallengesCompleted() {
        return this.getChallenges().stream().allMatch(this::isChallengeAccomplished);
    }

    public List<Challenge> getChallenges() {
        return this.challenges;
    }

    public @NotNull UUID getUUID() {
        return this.uuid;
    }

    public MiningSkillCardItem.Tier getTier() {
        return this.tier;
    }

    public boolean isPotionPointsFull() {
        return this.potionPoints >= this.getMaxPotionPoints();
    }

    public int getPotionPoints() {
        return this.potionPoints;
    }

    public int getMaxPotionPoints() {
        return ConfigHandler.SERVER.CARD_POTION_POINTS.getDefaultValue(this.tier);
    }

    public boolean isCreativeItem() {
        return this.uuid.equals(UUID.fromString("00000000-0000-0000-0000-000000000000"));
    }

    public void writeFinishedChallenges(FriendlyByteBuf buf) {
        buf.writeJsonWithCodec(MiningSkillCardData.Challenge.CODEC.listOf(), this.finishedChallenges);
    }

    public MiningSkillCardData readFinishedChallenges(FriendlyByteBuf buf) {
        this.finishedChallenges.addAll(buf.readJsonWithCodec(Challenge.CODEC.listOf()));
        return this;
    }

    public MiningSkillCardData sendToClient(ServerPlayer player, int slotIndex) {
        PacketHandler.sendToPlayer(new MiningSkillCardPacket(slotIndex, this), player);
        return this.onServerUpdate();
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        } else if (!(o instanceof MiningSkillCardData that)) {
            return false;
        } else {
            return this.uuid.equals(that.uuid) && this.tier == that.tier && ItemStack.isSameItemSameTags(this.displayItem, that.displayItem) && this.potionPoints == that.potionPoints && this.challenges.equals(that.challenges);
        }
    }

    public int hashCode() {
        return Objects.hash(this.uuid, this.tier, this.potionPoints, this.challenges) + ItemStackHelper.hashItemAndTags(this.displayItem);
    }

    public String toString() {
        return "MiningSkillCardData{uuid=" + this.uuid + ", tier=" + this.tier + ", displayItem=" + this.displayItem + ", potionPoints=" + this.potionPoints + ", challenges=" + this.challenges + "}";
    }

    public static class Challenge implements Comparable<Challenge> {
        public static final Codec<Challenge> CODEC = RecordCodecBuilder.create((instance) -> instance.group(ResourceLocation.CODEC.fieldOf("Id").forGetter(Challenge::getId), ExtraCodecs.NON_NEGATIVE_INT.optionalFieldOf("Order", 0).forGetter(Challenge::getOrder), ExtraCodecs.NON_NEGATIVE_INT.fieldOf("RequiredPoints").forGetter(Challenge::getRequiredPoints), ExtraCodecs.NON_NEGATIVE_INT.optionalFieldOf("CurrentPoints", 0).forGetter(Challenge::getCurrentPoints), Codec.BOOL.optionalFieldOf("IsPinned", false).forGetter(Challenge::isPinned)).apply(instance, Challenge::new));
        private final ResourceLocation id;
        private final int order;
        private final int requiredPoints;
        private int currentPoints;
        private boolean isPinned;

        private Challenge() {
            this(new ResourceLocation("completed"), 0, 1);
        }

        private Challenge(ResourceLocation id, int order, int requiredPoints) {
            this(id, order, requiredPoints, 0, false);
        }

        private Challenge(ResourceLocation id, int order, int requiredPoints, int currentPoints, boolean isPinned) {
            this.id = id;
            this.order = order;
            this.requiredPoints = requiredPoints;
            this.currentPoints = currentPoints;
            this.isPinned = isPinned;
        }

        public ResourceLocation getId() {
            return this.id;
        }

        public int getOrder() {
            return this.order;
        }

        public boolean isPinned() {
            return this.isPinned;
        }

        public int getCurrentPoints() {
            return this.currentPoints;
        }

        public int getRequiredPoints() {
            return this.requiredPoints;
        }

        public int compareTo(@NotNull Challenge other) {
            return Integer.compare(this.order, other.order);
        }

        public boolean equals(Object o) {
            if (this == o) {
                return true;
            } else if (!(o instanceof Challenge that)) {
                return false;
            } else {
                return this.id.equals(that.id) && this.order == that.order && this.currentPoints == that.currentPoints && this.requiredPoints == that.requiredPoints && this.isPinned == that.isPinned;
            }
        }

        public int hashCode() {
            return Objects.hash(this.id, this.order, this.currentPoints, this.requiredPoints, this.isPinned);
        }

        public String toString() {
            return "Challenge{id=" + this.id + ", order=" + this.order + ", currentPoints=" + this.currentPoints + ", requiredPoints=" + this.requiredPoints + ", isPinned=" + this.isPinned + "}";
        }
    }
}
