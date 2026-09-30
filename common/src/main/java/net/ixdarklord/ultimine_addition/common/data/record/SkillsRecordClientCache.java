package net.ixdarklord.ultimine_addition.common.data.record;

import net.ixdarklord.ultimine_addition.client.gui.toasts.ChallengesToast;
import net.ixdarklord.ultimine_addition.common.data.item.MiningSkillCardData;
import net.ixdarklord.ultimine_addition.common.data.item.SkillsRecordData;
import net.ixdarklord.ultimine_addition.network.PayloadHandler;
import net.ixdarklord.ultimine_addition.network.payloads.SkillsRecordPayload;
import net.minecraft.Util;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public final class SkillsRecordClientCache {
    private static final long REQUEST_COOLDOWN_MS = 2_000L;
    private static final Map<UUID, SkillsRecordData> RECORDS = new HashMap<>();
    private static final Map<UUID, CardProgress> CARDS = new HashMap<>();
    private static final Map<UUID, CardHistory> HISTORIES = new HashMap<>();
    private static final Map<UUID, Long> RECORD_REQUESTS = new HashMap<>();
    private static final Map<UUID, Long> CARD_REQUESTS = new HashMap<>();

    private SkillsRecordClientCache() {}

    public static Optional<SkillsRecordData> get(ItemStack stack) {
        UUID id = SkillsRecordLink.getId(stack);
        if (id == null) return Optional.empty();

        SkillsRecordData data = RECORDS.get(id);
        if (data == null && cooldownPassed(RECORD_REQUESTS, id)) PayloadHandler.sendToServer(new SkillsRecordPayload.RequestRecord(id));
        return Optional.ofNullable(data);
    }

    public static Optional<SkillsRecordData> get(UUID id) {
        return Optional.ofNullable(RECORDS.get(id));
    }

    public static @Nullable CardProgress getProgress(UUID cardId) {
        CardProgress progress = CARDS.get(cardId);
        if (progress == null && cooldownPassed(CARD_REQUESTS, cardId)) PayloadHandler.sendToServer(new SkillsRecordPayload.RequestCard(cardId));
        return progress;
    }

    public static Optional<CardHistory> getHistory(UUID cardId) {
        return Optional.ofNullable(HISTORIES.get(cardId));
    }

    public static SkillsRecordData accept(SkillsRecordData incoming) {
        RECORD_REQUESTS.remove(incoming.getUUID());
        SkillsRecordData existing = RECORDS.get(incoming.getUUID());
        if (existing == null) {
            RECORDS.put(incoming.getUUID(), incoming);
            return incoming;
        }
        existing.copyFrom(incoming);
        return existing;
    }

    public static void acceptCards(List<CardSync> cards) {
        for (CardSync sync : cards) {
            CARD_REQUESTS.remove(sync.id());
            HISTORIES.put(sync.id(), sync.history());
            if (!sync.card().isEmpty()) {
                for (MiningSkillCardData.Challenge finished : sync.progress().getFinished()) {
                    ChallengesToast.run(finished, sync.card());
                }
            }
            CardProgress existing = CARDS.get(sync.id());
            if (existing == null) {
                sync.progress().clearFinished();
                CARDS.put(sync.id(), sync.progress());
            } else {
                existing.copyFrom(sync.progress());
            }
        }
    }

    private static boolean cooldownPassed(Map<UUID, Long> requests, UUID id) {
        long now = Util.getMillis();
        Long last = requests.get(id);
        if (last != null && now - last < REQUEST_COOLDOWN_MS) return false;
        requests.put(id, now);
        return true;
    }

    public static void clear() {
        RECORDS.clear();
        CARDS.clear();
        HISTORIES.clear();
        RECORD_REQUESTS.clear();
        CARD_REQUESTS.clear();
    }
}
