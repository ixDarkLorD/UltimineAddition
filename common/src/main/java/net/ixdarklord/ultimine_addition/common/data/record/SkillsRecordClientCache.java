package net.ixdarklord.ultimine_addition.common.data.record;

import net.ixdarklord.ultimine_addition.common.data.item.SkillsRecordData;
import net.ixdarklord.ultimine_addition.network.PayloadHandler;
import net.ixdarklord.ultimine_addition.network.payloads.SkillsRecordPayload;
import net.minecraft.util.Util;
import net.minecraft.world.item.ItemStack;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * The client's copies of Skills Records, filled by {@link SkillsRecordPayload.SyncRecord}.
 * Records the player carries are pushed by the server; others (e.g. a record in a chest being hovered)
 * are requested on first use. Cleared on disconnect.
 * <p>
 * Kept free of client-only classes so common code can reference it.
 */
public final class SkillsRecordClientCache {
    private static final long REQUEST_COOLDOWN_MS = 2_000L;
    private static final Map<UUID, SkillsRecordData> RECORDS = new HashMap<>();
    private static final Map<UUID, CardHistory> HISTORIES = new HashMap<>();
    private static final Map<UUID, Long> REQUESTS = new HashMap<>();

    private SkillsRecordClientCache() {}

    public static Optional<SkillsRecordData> get(ItemStack stack) {
        UUID id = SkillsRecordLink.getId(stack);
        if (id == null) return Optional.empty();

        SkillsRecordData data = RECORDS.get(id);
        if (data == null) request(id);
        return Optional.ofNullable(data);
    }

    public static Optional<SkillsRecordData> get(UUID id) {
        return Optional.ofNullable(RECORDS.get(id));
    }

    public static Optional<CardHistory> getHistory(UUID cardId) {
        return Optional.ofNullable(HISTORIES.get(cardId));
    }

    /** Stores a synced record, updating the existing instance in place so open screens keep working with it. */
    public static SkillsRecordData accept(SkillsRecordData incoming, Map<UUID, CardHistory> histories) {
        HISTORIES.putAll(histories);
        REQUESTS.remove(incoming.getUUID());

        SkillsRecordData existing = RECORDS.get(incoming.getUUID());
        if (existing == null) {
            RECORDS.put(incoming.getUUID(), incoming);
            existing = incoming;
        } else {
            existing.copyFrom(incoming);
        }
        return existing.onClientUpdate();
    }

    private static void request(UUID id) {
        long now = Util.getMillis();
        Long last = REQUESTS.get(id);
        if (last != null && now - last < REQUEST_COOLDOWN_MS) return;
        REQUESTS.put(id, now);
        PayloadHandler.sendToServer(new SkillsRecordPayload.RequestRecord(id));
    }

    public static void clear() {
        RECORDS.clear();
        HISTORIES.clear();
        REQUESTS.clear();
    }
}
