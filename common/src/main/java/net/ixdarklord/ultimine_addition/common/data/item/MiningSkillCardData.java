package net.ixdarklord.ultimine_addition.common.data.item;

import dev.architectury.platform.Platform;
import net.ixdarklord.ultimine_addition.client.handler.ToastHandler;
import net.ixdarklord.ultimine_addition.common.config.ConfigHandler;
import net.ixdarklord.ultimine_addition.common.data.DataHandler;
import net.ixdarklord.ultimine_addition.common.data.challenge.ChallengesManager;
import net.ixdarklord.ultimine_addition.common.item.MiningSkillCardItem;
import net.ixdarklord.ultimine_addition.core.Constants;
import net.ixdarklord.ultimine_addition.core.Registration;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

public class MiningSkillCardData extends DataHandler<MiningSkillCardData, ItemStack> {
    private ItemStack stack;
    private ItemStack displayItem;
    private Map<Identifier, InfoData> currentChallenges = new TreeMap<>();
    private final List<Identifier> finishedChallenges = new ArrayList<>();
    private MiningSkillCardItem.Tier tier = MiningSkillCardItem.Tier.Unlearned;
    private int potionPoints;

    public MiningSkillCardData initChallenges() {
        if (tier == MiningSkillCardItem.Tier.Mastered) return this;
        AtomicInteger slotId = new AtomicInteger(1);
        AtomicInteger quantity = new AtomicInteger();
        MiningSkillCardItem.Type type = ((MiningSkillCardItem)stack.getItem()).getType();
        switch (tier) {
            case Unlearned -> quantity.set(ConfigHandler.COMMON.TIER_0_CHALLENGES_AMOUNT.get());
            case Novice -> quantity.set(ConfigHandler.COMMON.TIER_1_CHALLENGES_AMOUNT.get());
            case Apprentice -> quantity.set(ConfigHandler.COMMON.TIER_2_CHALLENGES_AMOUNT.get());
            case Adept -> quantity.set(ConfigHandler.COMMON.TIER_3_CHALLENGES_AMOUNT.get());
        }
        if (tier != MiningSkillCardItem.Tier.Unlearned && tier != MiningSkillCardItem.Tier.Mastered)
            setPotionPoints(this.getMaxPotionPoints());

        currentChallenges.clear();
        ChallengesManager.INSTANCE.getRandomChallenges(quantity.get(), type, this.tier).forEach((location, data) -> {
            currentChallenges.put(new Identifier(slotId.get(), location), new InfoData(data.getRequiredAmount()));
            slotId.getAndIncrement();
        });
        return this;
    }

    public MiningSkillCardData validateChallenges() {
        if (this.tier == MiningSkillCardItem.Tier.Mastered) return this;

        MiningSkillCardItem.Type type = ((MiningSkillCardItem)stack.getItem()).getType();
        if (!this.currentChallenges.isEmpty()) {
            Collection<Identifier> removedChallenges = new HashSet<>();
            this.currentChallenges.forEach((identifier, infoData) -> {
                if (!ChallengesManager.INSTANCE.getAllChallenges().containsKey(identifier.id)) {
                    removedChallenges.add(identifier);
                }
            });
            removedChallenges.forEach(identifier -> {
                AtomicBoolean isDone = new AtomicBoolean(true);
                this.currentChallenges.remove(identifier);
                do {
                    ChallengesManager.INSTANCE.getRandomChallenges(1, type, this.tier).forEach((location, data) -> {
                        if (this.currentChallenges.keySet().stream().filter(identifier1 -> identifier1.id.equals(location)).toList().isEmpty()) {
                            this.currentChallenges.put(new Identifier(identifier.order, location), new InfoData(data.getRequiredAmount()));
                            if (ConfigHandler.COMMON.CHALLENGE_MANAGER_LOGGER.get() || Platform.isDevelopmentEnvironment()) {
                                ChallengesManager.LOGGER.info("Changed the challenge from: id:\"{}\" to: id:\"{}\"", identifier.id, location);
                            }
                            isDone.set(false);
                        }
                    });
                } while (isDone.get());
            });
        } else initChallenges();
        return this;
    }

    public MiningSkillCardData setDisplayTool(ItemStack stack) {
        this.displayItem = stack;
        return this;
    }

    public MiningSkillCardData setAmount(ResourceLocation challengeId, int value) {
        InfoData infoData = this.getChallenge(challengeId);
        if (infoData == null) return this;

        int requiredAmount = infoData.requiredValue;
        infoData.setCurrentValue(Math.min(value, requiredAmount));
        this.checkChallengeAccomplishment(challengeId, this.getChallenge(challengeId));
        if (this.isAllChallengesCompleted()) {
            this.tier = this.tier.next();
            this.initChallenges();
            this.finishedChallenges.add(new Identifier(0, new ResourceLocation("completed")));
        } else this.checkChallengeAccomplishment(challengeId, this.getChallenge(challengeId));
        return this;
    }

    public MiningSkillCardData addAmount(ResourceLocation challengeId, int value) {
        InfoData infoData = this.getChallenge(challengeId);
        if (infoData == null) return this;

        int currentAmount = infoData.currentValue;
        int requiredAmount = infoData.requiredValue;
        if (currentAmount >= requiredAmount) return this;

        infoData.setCurrentValue(Math.min(currentAmount + value, requiredAmount));
        if (this.isAllChallengesCompleted()) {
            this.tier = this.tier.next();
            this.initChallenges();
            this.finishedChallenges.add(new Identifier(0, new ResourceLocation("completed")));
        } else this.checkChallengeAccomplishment(challengeId, this.getChallenge(challengeId));
        return this;
    }
    public MiningSkillCardData accomplishChallenge(ResourceLocation challengeId) {
        InfoData infoData = this.getChallenge(challengeId);
        if (infoData == null) return this;

        int requiredAmount = infoData.requiredValue;
        infoData.setCurrentValue(requiredAmount);
        if (this.isAllChallengesCompleted()) {
            this.tier = this.tier.next();
            this.initChallenges();
            this.finishedChallenges.add(new Identifier(0, new ResourceLocation("completed")));
        } else this.checkChallengeAccomplishment(challengeId, this.getChallenge(challengeId));
        return this;
    }

    public MiningSkillCardData setChallenges(Map<Identifier, InfoData> map) {
        this.currentChallenges = map;
        return this;
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
    public boolean isChallengeAccomplished(ResourceLocation challengeId) {
        InfoData infoData = this.getChallenge(challengeId);
        if (infoData == null || !this.isChallengeExists(challengeId)) return false;
        return infoData.currentValue >= infoData.requiredValue;
    }
    private boolean isAllChallengesCompleted() {
        AtomicInteger allCurrentValues = new AtomicInteger();
        AtomicInteger allRequiredValues = new AtomicInteger();
        this.currentChallenges.forEach((identifier, infoData) -> {
            allCurrentValues.addAndGet(infoData.currentValue);
            allRequiredValues.addAndGet(infoData.requiredValue);
        });
        return allCurrentValues.get() >= allRequiredValues.get();
    }

    public ItemStack get() {
        return this.stack;
    }
    public ItemStack getDisplayItem() {
        if (this.displayItem.isEmpty()) {
            MiningSkillCardItem.Type type = ((MiningSkillCardItem)stack.getItem()).getType();
            return type.defaultDisplayItem();
        }
        return this.displayItem;
    }
    @Nullable
    public InfoData getChallenge(ResourceLocation challengeId) {
        AtomicReference<InfoData> result = new AtomicReference<>(null);
        this.currentChallenges.forEach((identifier, infoData) -> {
            if (identifier.id.equals(challengeId)) result.set(infoData);
        });
        return result.get();
    }
    public Map<Identifier, InfoData> getChallenges() {
        return this.currentChallenges;
    }
    public boolean isChallengeExists(ResourceLocation challengeId) {
        AtomicBoolean result = new AtomicBoolean();
        this.currentChallenges.forEach((identifier, integers) -> {
            if (identifier.id.equals(challengeId)) result.set(true);
        });
        return result.get();
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
    public void clientUpdate() {
        finishedChallenges.forEach(identifier -> ToastHandler.playChallengeToast(identifier, stack));
    }

    @Override
    public void saveData(ItemStack stack) {
        CompoundTag NBT = (CompoundTag) stack.getOrCreateTag().get(this.NBTBase);
        if (NBT == null) NBT = new CompoundTag();

        if (this.displayItem != null)
            NBT.putString("DisplayItem", Constants.getIdAsString(Objects.requireNonNull(Registration.ITEMS.getRegistrar().getId(this.displayItem.getItem()))));
        NBT.putInt("Tier", this.tier.getValue());
        NBT.putInt("PotionPoint", this.potionPoints);
        NBT.merge(getNBTFromChallenges(this.currentChallenges));
        stack.getOrCreateTag().put(this.NBTBase, NBT);
        super.saveData(stack);
    }

    @Override
    public MiningSkillCardData loadData(ItemStack stack) {
        CompoundTag NBT = (CompoundTag) stack.getOrCreateTag().get(this.NBTBase);
        if (NBT == null) NBT = new CompoundTag();

        this.displayItem = Objects.requireNonNull(Registration.ITEMS.getRegistrar().get(new ResourceLocation(NBT.getString("DisplayItem")))).getDefaultInstance();
        this.tier = getTierFromInt(NBT.getInt("Tier"));
        this.potionPoints = NBT.getInt("PotionPoint");
        this.currentChallenges = getChallengesFromNBT(NBT);
        this.stack = stack;
        return this;
    }

    @Override
    public void toNetwork(FriendlyByteBuf buf) {
        buf.writeItem(this.stack);
        this.encodeFinishedChallenges(buf);
    }

    public static MiningSkillCardData fromNetwork(FriendlyByteBuf buf) {
        return new MiningSkillCardData().loadData(buf.readItem()).decodeFinishedChallenges(buf);
    }

    private void encodeFinishedChallenges(FriendlyByteBuf buf) {
        buf.writeCollection(this.finishedChallenges, (buffer, identifier) -> {
            buffer.writeInt(identifier.order);
            buffer.writeResourceLocation(identifier.id);
        });
    }

    private MiningSkillCardData decodeFinishedChallenges(FriendlyByteBuf buf) {
        this.finishedChallenges.addAll(buf.readList(buffer -> new Identifier(buffer.readInt(), buffer.readResourceLocation())));
        return this;
    }

    private static MiningSkillCardItem.Tier getTierFromInt(int value) {
        return MiningSkillCardItem.Tier.fromInt(value);
    }

    private static CompoundTag getNBTFromChallenges(Map<Identifier, InfoData> value) {
        ListTag listTag = new ListTag();
        value.forEach((identifier, infoData) -> {
            CompoundTag tag = new CompoundTag();
            tag.putInt("Order", identifier.order);
            tag.putString("Id", identifier.id.toString());
            tag.putBoolean("IsPinned", infoData.isPinned);
            tag.putInt("CurrentAmount", infoData.currentValue);
            tag.putInt("RequiredAmount", infoData.requiredValue);
            listTag.add(tag);
        });
        var tag = new CompoundTag();
        tag.put("Challenges", listTag);
        return tag;
    }

    private static Map<Identifier, InfoData> getChallengesFromNBT(CompoundTag NBT) {
        Map<Identifier, InfoData> result = new TreeMap<>();
        ListTag listTag = new ListTag();
        if (NBT != null) listTag = NBT.getList("Challenges", 10);

        for (int i = 0; i < listTag.size(); ++i) {
            CompoundTag tag = listTag.getCompound(i);
            int order = tag.getInt("Order");
            ResourceLocation id = new ResourceLocation(tag.getString("Id"));
            boolean pinned = tag.getBoolean("IsPinned");
            int current = tag.getInt("CurrentAmount");
            int required = tag.getInt("RequiredAmount");
            result.put(new Identifier(order, id), new InfoData(pinned, current, required));
        }
        return result;
    }

    private void checkChallengeAccomplishment(ResourceLocation challengeId, InfoData infoData) {
        AtomicReference<Identifier> identifier = new AtomicReference<>();
        this.currentChallenges.forEach((i, integers) -> {
            if (i.id.equals(challengeId)) identifier.set(i);
        });
        if (identifier.get() == null) return;
        if (infoData.currentValue >= infoData.requiredValue && !this.finishedChallenges.contains(identifier.get()))
            this.finishedChallenges.add(identifier.get());
    }

    public record Identifier(int order, ResourceLocation id) implements Comparable<Identifier> {
        @Override
        public int compareTo(@NotNull MiningSkillCardData.Identifier other) {
            return Integer.compare(this.order, other.order);
        }
    }

    public static class InfoData {
        private boolean isPinned;
        private int currentValue;
        private final int requiredValue;

        public InfoData(int requiredValue) {
            this(0, requiredValue);
        }

        public InfoData(int currentValue, int requiredValue) {
            this(false, currentValue, requiredValue);
        }

        public InfoData(boolean isPinned, int currentValue, int requiredValue) {
            this.isPinned = isPinned;
            this.currentValue = currentValue;
            this.requiredValue = requiredValue;
        }

        public void togglePinned() {
            isPinned ^= true;
        }

        public void setCurrentValue(int currentValue) {
            this.currentValue = currentValue;
        }

        public boolean isPinned() {
            return isPinned;
        }

        public int getCurrentValue() {
            return currentValue;
        }

        public int getRequiredValue() {
            return requiredValue;
        }
    }
}
