package net.ixdarklord.ultimine_addition.common.data.record;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.ixdarklord.ultimine_addition.common.data.item.MiningSkillCardData;
import net.ixdarklord.ultimine_addition.common.data.item.SkillsRecordData;
import net.ixdarklord.ultimine_addition.core.FTBUltimineAddition;
import net.minecraft.core.UUIDUtil;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.saveddata.SavedData;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public final class SkillsRecordSavedData extends SavedData {
    public static final Codec<SkillsRecordSavedData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            SkillsRecordData.CODEC.listOf().optionalFieldOf("Records", List.of()).forGetter(data -> List.copyOf(data.records.values())),
            Codec.unboundedMap(UUIDUtil.STRING_CODEC, CardEntry.CODEC).optionalFieldOf("Cards", Map.of()).forGetter(data -> data.cards)
    ).apply(instance, SkillsRecordSavedData::new));

    // 1.20.1 SavedData is plain NBT: load decodes it with the codec and save() encodes it back.
    public static final String DATA_KEY = FTBUltimineAddition.MOD_ID + ".skills_records";

    private final Map<UUID, SkillsRecordData> records = new HashMap<>();
    private final Map<UUID, CardEntry> cards;

    public SkillsRecordSavedData() {
        this(List.of(), Map.of());
    }

    private SkillsRecordSavedData(List<SkillsRecordData> records, Map<UUID, CardEntry> cards) {
        records.forEach(this::add);
        this.cards = new HashMap<>(cards);
    }

    public static SkillsRecordSavedData get(MinecraftServer server) {
        return server.overworld().getDataStorage().computeIfAbsent(SkillsRecordSavedData::load, SkillsRecordSavedData::new, DATA_KEY);
    }

    private static SkillsRecordSavedData load(CompoundTag tag) {
        return CODEC.parse(NbtOps.INSTANCE, tag)
                .resultOrPartial(FTBUltimineAddition.LOGGER::error)
                .orElseGet(SkillsRecordSavedData::new);
    }

    @Override
    public @NotNull CompoundTag save(@NotNull CompoundTag tag) {
        CODEC.encodeStart(NbtOps.INSTANCE, this)
                .resultOrPartial(FTBUltimineAddition.LOGGER::error)
                .ifPresent(encoded -> { if (encoded instanceof CompoundTag compound) tag.merge(compound); });
        return tag;
    }

    private void add(SkillsRecordData data) {
        data.attach(this);
        this.records.put(data.getUUID(), data);
    }

    public @Nullable SkillsRecordData get(UUID id) {
        return this.records.get(id);
    }

    public SkillsRecordData resolve(ItemStack stack) {
        SkillsRecordLink link = SkillsRecordLink.DATA_COMPONENT.get(stack);
        UUID id = SkillsRecordLink.getId(stack);
        Optional<SkillsRecordLink.Legacy> legacy = link == null ? Optional.empty() : link.legacy();

        if (legacy.isPresent() && (id == null || this.records.containsKey(id))) {
            id = UUID.randomUUID();
        }
        if (id == null) id = UUID.randomUUID();

        SkillsRecordData data = this.records.get(id);
        if (data == null) {
            UUID newId = id;
            data = legacy.map(l -> SkillsRecordData.fromLegacy(newId, l)).orElseGet(() -> SkillsRecordData.create(newId));
            this.add(data);
            this.onRecordChanged(data);
        }
        if (link == null || legacy.isPresent() || !id.equals(SkillsRecordLink.getId(stack))) {
            SkillsRecordLink.DATA_COMPONENT.set(stack, SkillsRecordLink.of(id));
        }
        return data;
    }

    public void onRecordChanged(SkillsRecordData data) {
        this.observeCards(data);
        data.bumpVersion();
        this.setDirty();
    }

    private void observeCards(SkillsRecordData data) {
        long now = System.currentTimeMillis();
        for (int i = 0; i < SkillsRecordData.CARD_SLOTS; i++) {
            data.getCardData(i).ifPresent(card -> {
                if (card.isCreativeItem()) return;
                CardEntry entry = this.entry(card);
                if (entry.history.observe(card, now)) entry.version++;
            });
        }
    }

    private CardEntry entry(MiningSkillCardData card) {
        CardEntry entry = this.cards.computeIfAbsent(card.getUUID(), id -> new CardEntry());
        if (entry.progress == null) {
            CardProgress legacy = card.getLegacy();
            CardProgress local = card.takeLocal();
            if (legacy != null) {
                entry.progress = new CardProgress(legacy.getChallenges(), legacy.getPotionPoints(), List.of());
            } else if (local != null && !local.isEmpty()) {
                entry.progress = local;
            } else {
                entry.progress = new CardProgress();
                card.rollChallenges(entry.progress);
            }
            entry.version++;
            this.setDirty();
        }
        if (card.getLegacy() != null) card.clearLegacy();
        return entry;
    }

    public @Nullable CardProgress findProgress(MiningSkillCardData card) {
        CardEntry entry = this.cards.get(card.getUUID());
        return entry == null ? null : entry.progress;
    }

    public CardProgress ensure(MiningSkillCardData card) {
        return this.entry(card).progress;
    }

    public void onCardChanged(MiningSkillCardData card) {
        if (card.isCreativeItem()) return;
        CardEntry entry = this.entry(card);
        entry.history.observe(card, System.currentTimeMillis());
        entry.version++;
        this.setDirty();
    }

    public Optional<CardHistory> getCardHistory(UUID cardId) {
        return Optional.ofNullable(this.cards.get(cardId)).map(entry -> entry.history);
    }

    public int getCardVersion(MiningSkillCardData card) {
        CardEntry entry = this.cards.get(card.getUUID());
        return entry == null ? -1 : entry.version;
    }

    public CardSync createSync(MiningSkillCardData card, ItemStack stack) {
        CardEntry entry = this.entry(card);
        CardSync sync = new CardSync(card.getUUID(), stack.copy(), entry.progress.copy(), entry.history.copy());
        entry.progress.clearFinished();
        return sync;
    }

    public @Nullable CardSync createSync(UUID cardId) {
        CardEntry entry = this.cards.get(cardId);
        if (entry == null || entry.progress == null) return null;
        return new CardSync(cardId, ItemStack.EMPTY, entry.progress.copy(), entry.history.copy());
    }

    public List<CardSync> createSyncsFor(SkillsRecordData data) {
        List<CardSync> result = new ArrayList<>();
        for (int i = 0; i < SkillsRecordData.CARD_SLOTS; i++) {
            ItemStack stack = data.getCardSlots().get(i);
            data.getCardData(i).filter(card -> !card.isCreativeItem()).ifPresent(card -> result.add(this.createSync(card, stack)));
        }
        return result;
    }
}
