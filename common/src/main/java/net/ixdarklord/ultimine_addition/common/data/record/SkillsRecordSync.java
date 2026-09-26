package net.ixdarklord.ultimine_addition.common.data.record;

import net.ixdarklord.ultimine_addition.common.data.item.SkillsRecordData;
import net.ixdarklord.ultimine_addition.common.item.SkillsRecordItem;
import net.ixdarklord.ultimine_addition.core.ServicePlatform;
import net.ixdarklord.ultimine_addition.network.PayloadHandler;
import net.ixdarklord.ultimine_addition.network.payloads.SkillsRecordPayload;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Pushes Skills Records to the players carrying them whenever their version changes (checked every tick),
 * so every server-side {@link SkillsRecordData#save()} reaches the client within a tick.
 */
public final class SkillsRecordSync {
    /** Player UUID -> record UUID -> last version sent. */
    private static final Map<UUID, Map<UUID, Integer>> SENT = new HashMap<>();

    private SkillsRecordSync() {}

    public static void tick(ServerPlayer player) {
        SkillsRecordSavedData storage = SkillsRecordSavedData.get(player.level().getServer());
        Map<UUID, Integer> sent = SENT.computeIfAbsent(player.getUUID(), k -> new HashMap<>());

        Inventory inventory = player.getInventory();
        for (int i = 0; i < inventory.getContainerSize(); i++) {
            syncStack(player, storage, sent, inventory.getItem(i));
        }
        if (ServicePlatform.get().slotAPI().isModLoaded()) {
            syncStack(player, storage, sent, ServicePlatform.get().slotAPI().getSkillsRecordItem(player));
        }
    }

    private static void syncStack(ServerPlayer player, SkillsRecordSavedData storage, Map<UUID, Integer> sent, ItemStack stack) {
        if (!(stack.getItem() instanceof SkillsRecordItem)) return;
        // Links unlinked/old stacks too (e.g. ones in accessory slots, which don't get inventoryTick).
        SkillsRecordData data = storage.resolve(stack);
        Integer version = sent.get(data.getUUID());
        if (version == null || version != data.getVersion()) {
            send(player, storage, data);
            sent.put(data.getUUID(), data.getVersion());
        }
    }

    /** Sends a record now (menu open, client request). */
    public static void send(ServerPlayer player, SkillsRecordSavedData storage, SkillsRecordData data) {
        PayloadHandler.sendToPlayer(new SkillsRecordPayload.SyncRecord(data, storage.getHistoriesFor(data)), player);
        data.onServerUpdate();
        SENT.computeIfAbsent(player.getUUID(), k -> new HashMap<>()).put(data.getUUID(), data.getVersion());
    }

    public static void forget(ServerPlayer player) {
        SENT.remove(player.getUUID());
    }

    public static void clear() {
        SENT.clear();
    }
}
