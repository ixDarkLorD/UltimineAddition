package net.ixdarklord.ultimine_addition.common.data.item;

import com.google.common.collect.Lists;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.architectury.platform.Platform;
import net.ixdarklord.ultimine_addition.client.gui.components.toasts.ChallengesToast;
import net.ixdarklord.ultimine_addition.common.data.DataHandler;
import net.ixdarklord.ultimine_addition.common.data.challenge.ChallengesManager;
import net.ixdarklord.ultimine_addition.common.item.MiningSkillCardItem;
import net.ixdarklord.ultimine_addition.common.network.PacketHandler;
import net.ixdarklord.ultimine_addition.common.network.packet.MiningSkillCardPacket;
import net.ixdarklord.ultimine_addition.config.ConfigHandler;
import net.ixdarklord.ultimine_addition.core.UltimineAddition;
import net.minecraft.core.UUIDUtil;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class MiningSkillCardData extends DataHandler<MiningSkillCardData, ItemStack> {
    public static final Codec<MiningSkillCardData> CODEC;
    public static final StreamCodec<RegistryFriendlyByteBuf, MiningSkillCardData> STREAM_CODEC;
    public static final DataComponentType<MiningSkillCardData> DATA_COMPONENT;

    @NotNull
    private final UUID uuid;
    private MiningSkillCardItem.Tier tier;
    private ItemStack displayItem;
    private int potionPoints;
    private final List<ChallengeHolder> challenges;
    private final List<ChallengeHolder> finishedChallenges;

    private MiningSkillCardData(@NotNull UUID uuid, MiningSkillCardItem.Tier tier, ItemStack displayItem, int potionPoints, List<ChallengeHolder> challenges) {
        this(uuid, tier, displayItem, potionPoints, challenges, Lists.newArrayList());
    }

    private MiningSkillCardData(@NotNull UUID uuid, MiningSkillCardItem.Tier tier, ItemStack displayItem, int potionPoints, List<ChallengeHolder> challenges, List<ChallengeHolder> finishedChallenges) {
        this.uuid = uuid;
        this.tier = tier;
        this.displayItem = displayItem;
        this.potionPoints = potionPoints;
        this.challenges = Lists.newArrayList(challenges);
        this.finishedChallenges = Lists.newArrayList(finishedChallenges);
    }

    public static MiningSkillCardData create(MiningSkillCardItem.Type type) {
        return new MiningSkillCardData(UUID.randomUUID(), MiningSkillCardItem.Tier.Unlearned, type.getDefaultDisplayItem().getDefaultInstance(), 0, Lists.newArrayList());
    }

    public static MiningSkillCardData loadData(ItemStack stack) {
        MiningSkillCardItem.Type type = stack.getItem() instanceof MiningSkillCardItem ? ((MiningSkillCardItem) stack.getItem()).getType() : MiningSkillCardItem.Type.EMPTY;
        return stack.getOrDefault(DATA_COMPONENT, create(type)).setDataHolder(stack);
    }

    @Override
    public void saveData(ItemStack stack) {
        stack.set(DATA_COMPONENT, this);
        super.saveData(stack);
    }

    public MiningSkillCardData sendToClient(ServerPlayer player) {
        PacketHandler.sendToPlayer(new MiningSkillCardPacket(this, this.get()), player);
        this.finishedChallenges.clear();
        return this;
    }

    static {
        CODEC = RecordCodecBuilder.create(instance -> instance.group(
                UUIDUtil.CODEC.optionalFieldOf("UUID", UUID.randomUUID()).forGetter(MiningSkillCardData::getUUID),
                MiningSkillCardItem.Tier.CODEC.fieldOf("Tier").forGetter(MiningSkillCardData::getTier),
                ItemStack.SIMPLE_ITEM_CODEC.fieldOf("DisplayItem").forGetter(MiningSkillCardData::getDisplayItem),
                ExtraCodecs.NON_NEGATIVE_INT.optionalFieldOf("PotionPoints", 0).forGetter(MiningSkillCardData::getPotionPoints),
                ChallengeHolder.CODEC.listOf().optionalFieldOf("Challenges", Lists.newArrayList()).forGetter(MiningSkillCardData::getChallenges)
        ).apply(instance, MiningSkillCardData::new));

        STREAM_CODEC = StreamCodec.composite(
                UUIDUtil.STREAM_CODEC, MiningSkillCardData::getUUID,
                MiningSkillCardItem.Tier.STREAM_CODEC, MiningSkillCardData::getTier,
                ItemStack.STREAM_CODEC, MiningSkillCardData::getDisplayItem,
                ByteBufCodecs.INT, MiningSkillCardData::getPotionPoints,
                ChallengeHolder.STREAM_CODEC.apply(ByteBufCodecs.list()), MiningSkillCardData::getChallenges,
                ChallengeHolder.STREAM_CODEC.apply(ByteBufCodecs.list()), data -> data.finishedChallenges,
                MiningSkillCardData::new
        );

        DATA_COMPONENT = DataComponentType.<MiningSkillCardData>builder().persistent(CODEC).networkSynchronized(STREAM_CODEC).build();
    }

    @Override
    public MiningSkillCardData clientUpdate() {
        for (ChallengeHolder finishedChallenge : this.finishedChallenges)
            ChallengesToast.run(finishedChallenge, dataHolder);
        return this;
    }


    public @NotNull UUID getUUID() {
        return uuid;
    }

    public MiningSkillCardData initChallenges() {
        if (!(this.dataHolder.getItem() instanceof MiningSkillCardItem)) {
            UltimineAddition.LOGGER.error("You've tried to initiate challenges on item can't accept it: {}", this.dataHolder.getDescriptionId());
            return this;
        }

        if (tier == MiningSkillCardItem.Tier.Mastered) {
            challenges.clear();
            return this;
        }

        AtomicInteger slotId = new AtomicInteger(1);
        AtomicInteger quantity = new AtomicInteger();
        MiningSkillCardItem.Type type = ((MiningSkillCardItem) dataHolder.getItem()).getType();
        switch (tier) {
            case Unlearned -> quantity.set(ConfigHandler.COMMON.TIER_0_CHALLENGES_AMOUNT.get());
            case Novice -> quantity.set(ConfigHandler.COMMON.TIER_1_CHALLENGES_AMOUNT.get());
            case Apprentice -> quantity.set(ConfigHandler.COMMON.TIER_2_CHALLENGES_AMOUNT.get());
            case Adept -> quantity.set(ConfigHandler.COMMON.TIER_3_CHALLENGES_AMOUNT.get());
        }
        if (tier != MiningSkillCardItem.Tier.Unlearned && tier != MiningSkillCardItem.Tier.Mastered)
            setPotionPoints(this.getMaxPotionPoints());

        challenges.clear();
        ChallengesManager.INSTANCE.getRandomChallenges(quantity.get(), type, this.tier).forEach((location, data) -> {
            challenges.add(new ChallengeHolder(location, slotId.get(), data.getRequiredAmount()));
            slotId.getAndIncrement();
        });
        return this;
    }

    public MiningSkillCardData validateChallenges() {
        if (this.tier == MiningSkillCardItem.Tier.Mastered) return this;

        MiningSkillCardItem.Type type = ((MiningSkillCardItem) dataHolder.getItem()).getType();
        if (!this.challenges.isEmpty()) {
            Collection<ChallengeHolder> removedChallenges = new TreeSet<>();
            this.challenges.forEach((challengeData) -> {
                if (!ChallengesManager.INSTANCE.getAllChallenges().containsKey(challengeData.id)) {
                    removedChallenges.add(challengeData);
                }
            });
            removedChallenges.forEach(challengeData -> {
                AtomicBoolean isDone = new AtomicBoolean(true);
                this.challenges.remove(challengeData);
                do {
                    ChallengesManager.INSTANCE.getRandomChallenges(1, type, this.tier).forEach((location, data) -> {
                        if (this.challenges.stream().filter(challengeData1 -> challengeData1.id.equals(location)).toList().isEmpty()) {
                            this.challenges.add(new ChallengeHolder(location, challengeData.order, data.getRequiredAmount()));
                            if (ConfigHandler.COMMON.CHALLENGE_MANAGER_LOGGER.get() || Platform.isDevelopmentEnvironment()) {
                                ChallengesManager.LOGGER.info("Changed the challenge from: id:\"{}\" to: id:\"{}\"", challengeData.id, location);
                            }
                            isDone.set(false);
                        }
                    });
                } while (isDone.get());
            });
        } else initChallenges();
        return this;
    }

    public MiningSkillCardData setDisplayItem(ItemStack stack) {
        this.displayItem = stack;
        return this;
    }

    public MiningSkillCardData addAmount(ResourceLocation challengeId, int value) {
        Optional<ChallengeHolder> challengeData = this.getChallenge(challengeId);
        if (challengeData.isEmpty()) return this;

        int currentAmount = challengeData.get().currentPoints;
        int requiredAmount = challengeData.get().requiredPoints;
        if (currentAmount >= requiredAmount) return this;

        return setAmount(challengeId, currentAmount + value);
    }

    public MiningSkillCardData accomplishChallenge(ResourceLocation challengeId) {
        Optional<ChallengeHolder> challengeData = this.getChallenge(challengeId);
        if (challengeData.isEmpty()) return this;

        return setAmount(challengeId, challengeData.get().requiredPoints);
    }

    public MiningSkillCardData setAmount(ResourceLocation challengeId, int value) {
        Optional<ChallengeHolder> challengeData = this.getChallenge(challengeId);
        if (challengeData.isEmpty()) return this;

        int requiredAmount = challengeData.get().requiredPoints;
        challengeData.get().setCurrentPoints(Math.min(value, requiredAmount));

        if (this.isAllChallengesCompleted()) {
            this.tier = this.tier.next();
            this.initChallenges();
            this.finishedChallenges.add(new ChallengeHolder());
        } else this.checkChallengeAccomplishment(challengeData.get());
        return this;
    }

    private void checkChallengeAccomplishment(ChallengeHolder challengeHolder) {
        if (challengeHolder.currentPoints >= challengeHolder.requiredPoints && !this.finishedChallenges.contains(challengeHolder))
            this.finishedChallenges.add(challengeHolder);
    }

    public void setPotionPoints(int value) {
        this.potionPoints = value;
    }

    public MiningSkillCardData consumePotionPoint(int value) {
        this.potionPoints = Math.max(0, this.potionPoints - value);
        return this;
    }

    public MiningSkillCardData setTier(MiningSkillCardItem.Tier tier) {
        this.tier = tier;
        return this;
    }

    public ItemStack getDisplayItem() {
        return this.displayItem;
    }

    public Optional<ChallengeHolder> getChallenge(ResourceLocation challengeId) {
        for (ChallengeHolder challenge : this.challenges) {
            if (challenge.id.equals(challengeId))
                return Optional.of(challenge);
        }
        return Optional.empty();
    }

    public boolean isChallengeAccomplished(ResourceLocation challengeId) {
        Optional<ChallengeHolder> challengeData = this.getChallenge(challengeId);
        return challengeData.filter(data -> data.currentPoints >= data.requiredPoints).isPresent();
    }

    private boolean isAllChallengesCompleted() {
        int allCurrentValues = 0;
        int allRequiredValues = 0;
        for (ChallengeHolder challenge : this.challenges) {
            allCurrentValues += challenge.currentPoints;
            allRequiredValues += challenge.requiredPoints;
        }
        return allCurrentValues >= allRequiredValues;
    }

    public List<ChallengeHolder> getChallenges() {
        return this.challenges;
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
        return switch (this.tier) {
            case Novice -> ConfigHandler.COMMON.TIER_1_POTION_POINTS.get();
            case Apprentice -> ConfigHandler.COMMON.TIER_2_POTION_POINTS.get();
            case Adept -> ConfigHandler.COMMON.TIER_3_POTION_POINTS.get();
            default -> 0;
        };
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof MiningSkillCardData data)) return false;
        return potionPoints == data.potionPoints && tier == data.tier && Objects.equals(displayItem, data.displayItem) && Objects.equals(challenges, data.challenges) && Objects.equals(finishedChallenges, data.finishedChallenges);
    }

    @Override
    public int hashCode() {
        return Objects.hash(tier, displayItem, potionPoints, challenges, finishedChallenges);
    }

    @Override
    public String toString() {
        return "MiningSkillCardData{" +
                "uuid=" + uuid +
                ", tier=" + tier +
                ", displayItem=" + displayItem +
                ", potionPoints=" + potionPoints +
                ", challenges=" + challenges +
                '}';
    }

    public static class ChallengeHolder implements Comparable<ChallengeHolder> {
        public static final Codec<ChallengeHolder> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                ResourceLocation.CODEC.fieldOf("Id").forGetter(ChallengeHolder::getId),
                ExtraCodecs.NON_NEGATIVE_INT.optionalFieldOf("Order", 0).forGetter(ChallengeHolder::getOrder),
                ExtraCodecs.NON_NEGATIVE_INT.optionalFieldOf("CurrentPoints", 0).forGetter(ChallengeHolder::getCurrentPoints),
                ExtraCodecs.NON_NEGATIVE_INT.fieldOf("RequiredPoints").forGetter(ChallengeHolder::getRequiredPoints),
                Codec.BOOL.optionalFieldOf("IsPinned", false).forGetter(ChallengeHolder::isPinned)
        ).apply(instance, ChallengeHolder::new));

        public static final StreamCodec<FriendlyByteBuf, ChallengeHolder> STREAM_CODEC = StreamCodec.composite(
                ResourceLocation.STREAM_CODEC, ChallengeHolder::getId,
                ByteBufCodecs.INT, ChallengeHolder::getOrder,
                ByteBufCodecs.INT, ChallengeHolder::getCurrentPoints,
                ByteBufCodecs.INT, ChallengeHolder::getRequiredPoints,
                ByteBufCodecs.BOOL, ChallengeHolder::isPinned,
                ChallengeHolder::new
        );

        private final ResourceLocation id;
        private final int order;
        private int currentPoints;
        private final int requiredPoints;
        private boolean isPinned;

        private ChallengeHolder() {
            this(ResourceLocation.parse("completed"), 0, 1);
        }

        private ChallengeHolder(ResourceLocation id, int order, int requiredPoints) {
            this(id, order, 0, requiredPoints);
        }

        private ChallengeHolder(ResourceLocation id, int order, int currentPoints, int requiredPoints) {
            this(id, order, currentPoints, requiredPoints, false);
        }

        private ChallengeHolder(ResourceLocation id, int order, int currentPoints, int requiredPoints, boolean isPinned) {
            this.id = id;
            this.order = order;
            this.currentPoints = currentPoints;
            this.requiredPoints = requiredPoints;
            this.isPinned = isPinned;
        }

        @Override
        public int compareTo(@NotNull MiningSkillCardData.ChallengeHolder other) {
            return Integer.compare(this.order, other.order);
        }

        public ResourceLocation getId() {
            return id;
        }

        public int getOrder() {
            return order;
        }

        public void togglePinned() {
            isPinned ^= true;
        }

        public void setCurrentPoints(int currentPoints) {
            this.currentPoints = currentPoints;
        }

        public boolean isPinned() {
            return isPinned;
        }

        public int getCurrentPoints() {
            return currentPoints;
        }

        public int getRequiredPoints() {
            return requiredPoints;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof ChallengeHolder that)) return false;
            return order == that.order && currentPoints == that.currentPoints && requiredPoints == that.requiredPoints && isPinned == that.isPinned && Objects.equals(id, that.id);
        }

        @Override
        public int hashCode() {
            return Objects.hash(id, order, currentPoints, requiredPoints, isPinned);
        }

        @Override
        public String toString() {
            return "ChallengeHolder{" +
                    "id=" + id +
                    ", order=" + order +
                    ", currentPoints=" + currentPoints +
                    ", requiredPoints=" + requiredPoints +
                    ", isPinned=" + isPinned +
                    '}';
        }
    }
}
