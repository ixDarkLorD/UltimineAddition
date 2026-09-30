package net.ixdarklord.ultimine_addition.common.data.record;

import net.ixdarklord.ultimine_addition.common.data.item.MiningSkillCardData;
import net.ixdarklord.ultimine_addition.common.data.item.SkillsRecordData;
import net.ixdarklord.ultimine_addition.common.item.SkillsRecordItem;
import net.ixdarklord.ultimine_addition.core.ServicePlatform;
import net.ixdarklord.ultimine_addition.network.PayloadHandler;
import net.ixdarklord.ultimine_addition.network.payloads.SkillsRecordPayload;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public final class SkillsRecordSync {
    private static final Map<UUID, Map<UUID, Integer>> SENT_RECORDS = new HashMap<>();
    private static final Map<UUID, Map<UUID, Integer>> SENT_CARDS = new HashMap<>();

    private SkillsRecordSync() {}

    public static void tick(ServerPlayer player) {
        SkillsRecordSavedData storage = SkillsRecordSavedData.get(player.level().getServer());
        List<CardSync> cards = new ArrayList<>();

        Inventory inventory = player.getInventory();
        for (int i = 0; i < inventory.getContainerSize(); i++) {
            syncStack(player, storage, inventory.getItem(i), cards);
        }
        if (ServicePlatform.get().slotAPI().isModLoaded()) {
            syncStack(player, storage, ServicePlatform.get().slotAPI().getSkillsRecordItem(player), cards);
        }
        sendCards(player, cards);
    }

    private static void syncStack(ServerPlayer player, SkillsRecordSavedData storage, ItemStack stack, List<CardSync> cards) {
        if (stack.getItem() instanceof SkillsRecordItem) {
            // Also links stacks in accessory slots, which don't get inventoryTick.
            SkillsRecordData data = storage.resolve(stack);
            Map<UUID, Integer> sent = SENT_RECORDS.computeIfAbsent(player.getUUID(), k -> new HashMap<>());
            Integer version = sent.get(data.getUUID());
            if (version == null || version != data.getVersion()) {
                PayloadHandler.sendToPlayer(new SkillsRecordPayload.SyncRecord(data.snapshot()), player);
                sent.put(data.getUUID(), data.getVersion());
            }
            for (ItemStack card : data.getCardSlots()) collectCard(player, storage, card, cards);
        } else {
            collectCard(player, storage, stack, cards);
        }
    }

    private static void collectCard(ServerPlayer player, SkillsRecordSavedData storage, ItemStack stack, List<CardSync> cards) {
        if (!MiningSkillCardData.hasData(stack)) return;
        MiningSkillCardData card = MiningSkillCardData.load(stack);
        if (card.isCreativeItem()) return;
        storage.ensure(card);
        int version = storage.getCardVersion(card);
        Map<UUID, Integer> sent = SENT_CARDS.computeIfAbsent(player.getUUID(), k -> new HashMap<>());
        Integer last = sent.get(card.getUUID());
        if (last == null || last != version) {
            cards.add(storage.createSync(card, stack));
            sent.put(card.getUUID(), version);
        }
    }

    public static void send(ServerPlayer player, SkillsRecordSavedData storage, SkillsRecordData data) {
        PayloadHandler.sendToPlayer(new SkillsRecordPayload.SyncRecord(data.snapshot()), player);
        SENT_RECORDS.computeIfAbsent(player.getUUID(), k -> new HashMap<>()).put(data.getUUID(), data.getVersion());
        sendCards(player, storage.createSyncsFor(data));
    }

    public static void sendCards(ServerPlayer player, List<CardSync> cards) {
        if (!cards.isEmpty()) PayloadHandler.sendToPlayer(new SkillsRecordPayload.SyncCards(cards), player);
    }

    public static void forget(ServerPlayer player) {
        SENT_RECORDS.remove(player.getUUID());
        SENT_CARDS.remove(player.getUUID());
    }

    public static void clear() {
        SENT_RECORDS.clear();
        SENT_CARDS.clear();
    }
}
