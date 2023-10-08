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

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

public class MiningSkillCardData extends DataHandler<MiningSkillCardData, ItemStack> {
    private ItemStack stack;
    private ItemStack displayItem;
    private Map<Identifier, Values> currentChallenges = new TreeMap<>();
    private final List<Identifier> finishedChallenges = new ArrayList<>();
    private MiningSkillCardItem.Tier tier = MiningSkillCardItem.Tier.Unlearned;
    private int potionPoints;

    public record Identifier(int order, ResourceLocation id) implements Comparable<Identifier> {
        @Override
        public int compareTo(@NotNull MiningSkillCardData.Identifier other) {
            return Integer.compare(this.order, other.order);
        }
    }
    public static class Values {
        private int current;
        private final int required;
        public Values(int current, int required) {
            this.current = current;
            this.required = required;
        }

        public int getCurrent() {
            return current;
        }

        public void setCurrent(int current) {
            this.current = current;
        }

        public int getRequired() {
            return required;
        }
    }

    public MiningSkillCardData initChallenges() {
        if (tier == MiningSkillCardItem.Tier.Mastered) return this;
        AtomicInteger slotId = new AtomicInteger(1);
        AtomicInteger quantity = new AtomicInteger();
        MiningSkillCardItem.Type type = ((MiningSkillCardItem)stack.getItem()).getType();
        switch (tier) {
            case Unlearned -> quantity.set(2);
            case Novice -> quantity.set(3);
            case Apprentice -> quantity.set(5);
            case Adept -> quantity.set(8);
        }
        if (tier != MiningSkillCardItem.Tier.Unlearned && tier != MiningSkillCardItem.Tier.Mastered)
            setPotionPoints(this.getMaxPotionPoints());

        currentChallenges.clear();
        ChallengesManager.INSTANCE.getRandomChallenges(quantity.get(), type, this.tier).forEach((location, data) -> {
            currentChallenges.put(new Identifier(slotId.get(), location), new Values(0, data.getRequiredAmount()));
            slotId.getAndIncrement();
        });
        return this;
    }

    public MiningSkillCardData validateChallenges() {
        if (this.tier == MiningSkillCardItem.Tier.Mastered) return this;

        MiningSkillCardItem.Type type = ((MiningSkillCardItem)stack.getItem()).getType();
        if (!this.currentChallenges.isEmpty()) {
            Collection<Identifier> removedChallenges = new HashSet<>();
            this.currentChallenges.forEach((identifier, values) -> {
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
                            this.currentChallenges.put(new Identifier(identifier.order, location), new Values(0, data.getRequiredAmount()));
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
        int requiredAmount = this.getChallenge(challengeId).required;
        this.getChallenge(challengeId).setCurrent(Math.min(value, requiredAmount));
        this.checkChallengeAccomplishment(challengeId, this.getChallenge(challengeId));
        if (this.isAllChallengesCompleted()) {
            this.tier = this.tier.next();
            this.initChallenges();
            this.finishedChallenges.add(new Identifier(0, new ResourceLocation("completed")));
        } else this.checkChallengeAccomplishment(challengeId, this.getChallenge(challengeId));
        return this;
    }

    public MiningSkillCardData addAmount(ResourceLocation challengeId, int value) {
        int currentAmount = this.getChallenge(challengeId).current;
        int requiredAmount = this.getChallenge(challengeId).required;
        if (currentAmount >= requiredAmount) return this;

        this.getChallenge(challengeId).setCurrent(Math.min(currentAmount + value, requiredAmount));
        if (this.isAllChallengesCompleted()) {
            this.tier = this.tier.next();
            this.initChallenges();
            this.finishedChallenges.add(new Identifier(0, new ResourceLocation("completed")));
        } else this.checkChallengeAccomplishment(challengeId, this.getChallenge(challengeId));
        return this;
    }
    public MiningSkillCardData accomplishChallenge(ResourceLocation challengeId) {
        int requiredAmount = this.getChallenge(challengeId).required;
        this.getChallenge(challengeId).setCurrent(requiredAmount);
        if (this.isAllChallengesCompleted()) {
            this.tier = this.tier.next();
            this.initChallenges();
            this.finishedChallenges.add(new Identifier(0, new ResourceLocation("completed")));
        } else this.checkChallengeAccomplishment(challengeId, this.getChallenge(challengeId));
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
        if (!this.isChallengeExists(challengeId)) return false;
        return this.getChallenge(challengeId).current >= this.getChallenge(challengeId).required;
    }
    private boolean isAllChallengesCompleted() {
        AtomicInteger allCurrentValues = new AtomicInteger();
        AtomicInteger allRequiredValues = new AtomicInteger();
        this.currentChallenges.forEach((identifier, values) -> {
            allCurrentValues.addAndGet(values.current);
            allRequiredValues.addAndGet(values.required);
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
    public Values getChallenge(ResourceLocation challengeId) {
        AtomicReference<Values> result = new AtomicReference<>(null);
        this.currentChallenges.forEach((identifier, values) -> {
            if (identifier.id.equals(challengeId)) result.set(values);
        });
        return result.get();
    }
    public Map<Identifier, Values> getChallenges() {
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
            case Novice -> 3;
            case Apprentice -> 2;
            case Adept -> 1;
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

    private static CompoundTag getNBTFromChallenges(Map<Identifier, Values> value) {
        ListTag listTag = new ListTag();
        value.forEach((identifier, values) -> {
            CompoundTag tag = new CompoundTag();
            tag.putInt("Order", identifier.order);
            tag.putString("Id", identifier.id.toString());
            tag.putInt("CurrentAmount", values.current);
            tag.putInt("RequiredAmount", values.required);
            listTag.add(tag);
        });
        var tag = new CompoundTag();
        tag.put("Challenges", listTag);
        return tag;
    }

    private static Map<Identifier, Values> getChallengesFromNBT(CompoundTag NBT) {
        Map<Identifier, Values> result = new TreeMap<>();
        ListTag listTag = new ListTag();
        if (NBT != null) listTag = NBT.getList("Challenges", 10);

        for (int i = 0; i < listTag.size(); ++i) {
            CompoundTag tag = listTag.getCompound(i);
            int order = tag.getInt("Order");
            ResourceLocation id = new ResourceLocation(tag.getString("Id"));
            int current = tag.getInt("CurrentAmount");
            int required = tag.getInt("RequiredAmount");
            result.put(new Identifier(order, id), new Values(current, required));
        }
        return result;
    }

    private void checkChallengeAccomplishment(ResourceLocation challengeId, Values values) {
        AtomicReference<Identifier> identifier = new AtomicReference<>();
        this.currentChallenges.forEach((i, integers) -> {
            if (i.id.equals(challengeId)) identifier.set(i);
        });
        if (identifier.get() == null) return;
        if (values.current >= values.required && !this.finishedChallenges.contains(identifier.get()))
            this.finishedChallenges.add(identifier.get());
    }
}
