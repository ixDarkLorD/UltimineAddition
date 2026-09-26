package net.ixdarklord.ultimine_addition.common.data.record;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.ixdarklord.ultimine_addition.common.data.item.MiningSkillCardData;
import net.ixdarklord.ultimine_addition.common.data.item.SkillsRecordData;
import net.ixdarklord.ultimine_addition.core.FTBUltimineAddition;
import net.minecraft.core.UUIDUtil;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;
import org.jetbrains.annotations.Nullable;

import java.util.*;

/**
 * Server-wide storage of every Skills Record (contents and settings, keyed by the record's UUID) and of every
 * Mining Skill Card's {@link CardHistory} (keyed by the card's UUID, so the history follows the card between records).
 * <p>
 * Like the Building Gadgets templates, the item only links to its entry. Copies of a record item (creative
 * pick-block, commands) share one entry, and entries of destroyed records stay in the file.
 */
public final class SkillsRecordSavedData extends SavedData {
    public static final Codec<SkillsRecordSavedData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            SkillsRecordData.CODEC.listOf().optionalFieldOf("Records", List.of()).forGetter(data -> List.copyOf(data.records.values())),
            Codec.unboundedMap(UUIDUtil.STRING_CODEC, CardHistory.CODEC).optionalFieldOf("Cards", Map.of()).forGetter(data -> data.cards)
    ).apply(instance, SkillsRecordSavedData::new));

    public static final SavedDataType<SkillsRecordSavedData> TYPE = new SavedDataType<>(
            FTBUltimineAddition.id("skills_records"), SkillsRecordSavedData::new, CODEC, DataFixTypes.LEVEL);

    private final Map<UUID, SkillsRecordData> records = new HashMap<>();
    private final Map<UUID, CardHistory> cards;

    public SkillsRecordSavedData() {
        this(List.of(), Map.of());
    }

    private SkillsRecordSavedData(List<SkillsRecordData> records, Map<UUID, CardHistory> cards) {
        records.forEach(this::add);
        this.cards = new HashMap<>(cards);
    }

    public static SkillsRecordSavedData get(MinecraftServer server) {
        return server.getDataStorage().computeIfAbsent(TYPE);
    }

    private void add(SkillsRecordData data) {
        data.attach(this);
        this.records.put(data.getUUID(), data);
    }

    public @Nullable SkillsRecordData get(UUID id) {
        return this.records.get(id);
    }

    /**
     * The record a stack links to. Unlinked stacks get a new record; stacks still holding the pre-SavedData
     * contents have them moved into the storage (into a fresh record if the UUID is already taken by another copy,
     * so duplicated old items don't lose their contents).
     */
    public SkillsRecordData resolve(ItemStack stack) {
        SkillsRecordLink link = stack.get(SkillsRecordLink.DATA_COMPONENT);
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
            stack.set(SkillsRecordLink.DATA_COMPONENT, SkillsRecordLink.of(id));
        }
        return data;
    }

    /** Called by {@link SkillsRecordData#save()}. */
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
                this.cards.computeIfAbsent(card.getUUID(), uuid -> new CardHistory()).observe(card, now);
            });
        }
    }

    public Optional<CardHistory> getCardHistory(UUID cardId) {
        return Optional.ofNullable(this.cards.get(cardId));
    }

    /** The histories of the cards currently inside a record, for syncing. */
    public Map<UUID, CardHistory> getHistoriesFor(SkillsRecordData data) {
        Map<UUID, CardHistory> result = new HashMap<>();
        for (int i = 0; i < SkillsRecordData.CARD_SLOTS; i++) {
            data.getCardData(i).map(MiningSkillCardData::getUUID).ifPresent(uuid -> {
                CardHistory history = this.cards.get(uuid);
                if (history != null) result.put(uuid, history);
            });
        }
        return result;
    }
}
