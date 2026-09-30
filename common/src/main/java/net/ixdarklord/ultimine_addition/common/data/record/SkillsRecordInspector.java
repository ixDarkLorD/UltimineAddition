package net.ixdarklord.ultimine_addition.common.data.record;

import net.ixdarklord.ultimine_addition.common.data.item.MiningSkillCardData;
import net.ixdarklord.ultimine_addition.common.data.item.SkillsRecordData;
import net.ixdarklord.ultimine_addition.common.item.MiningSkillCardItem;
import net.ixdarklord.ultimine_addition.common.item.SkillsRecordItem;
import net.ixdarklord.ultimine_addition.core.ServicePlatform;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;

// A plain-text summary of the Skills Records and Mining Skill Cards a player carries, as this side sees them: the
// server from its saved data, a client from its caches. The inspect commands print it on both sides, so comparing the
// two shows whether the client has been synced.
public final class SkillsRecordInspector {
    private SkillsRecordInspector() {}

    public static List<String> describe(Player player, Function<ItemStack, Optional<SkillsRecordData>> records) {
        List<String> lines = new ArrayList<>();
        List<ItemStack> stacks = new ArrayList<>();
        Inventory inventory = player.getInventory();
        for (int i = 0; i < inventory.getContainerSize(); i++) stacks.add(inventory.getItem(i));
        if (ServicePlatform.get().slotAPI().isModLoaded()) stacks.add(ServicePlatform.get().slotAPI().getSkillsRecordItem(player));

        for (ItemStack stack : stacks) {
            if (stack.getItem() instanceof SkillsRecordItem) {
                Optional<SkillsRecordData> found = records.apply(stack);
                if (found.isEmpty()) {
                    lines.add("record: not known on this side");
                    continue;
                }
                SkillsRecordData record = found.get();
                // (Not the record's version: that's a server-side change counter, never synced.)
                lines.add("record %s ink=%d selected=%d consume=%s".formatted(shortId(record.getUUID()),
                        record.getInkAmount(), record.getSelectedCard(), record.isConsumeModeActive()));
                List<String> slots = new ArrayList<>();
                List<ItemStack> all = record.getAllSlots();
                for (int i = 0; i < all.size(); i++) {
                    ItemStack slot = all.get(i);
                    if (!slot.isEmpty()) slots.add(i + ":" + BuiltInRegistries.ITEM.getKey(slot.getItem()).getPath() + "x" + slot.getCount());
                }
                lines.add("  slots " + slots);
                for (ItemStack card : record.getCardSlots()) describeCard(card, "  ", lines);
            } else {
                describeCard(stack, "", lines);
            }
        }
        if (lines.isEmpty()) lines.add("no Skills Record or Mining Skill Card carried");
        return lines;
    }

    private static void describeCard(ItemStack stack, String indent, List<String> lines) {
        if (!MiningSkillCardData.hasData(stack)) return;
        MiningSkillCardData card = MiningSkillCardData.load(stack);
        List<String> challenges = new ArrayList<>();
        for (MiningSkillCardData.Challenge challenge : card.getChallenges()) {
            challenges.add(challenge.getId() + "=" + challenge.getCurrentPoints() + "/" + challenge.getRequiredPoints());
        }
        // Only Novice to Adept cards brew; "?" until the progress is known on this side.
        MiningSkillCardItem.Tier tier = card.getTier();
        String potion = !card.hasProgress() ? "?"
                : tier == MiningSkillCardItem.Tier.Unlearned || tier == MiningSkillCardItem.Tier.Mastered ? "-"
                : card.getPotionPoints() + "/" + card.getMaxPotionPoints();
        lines.add("%scard %s %s tier=%s potion=%s challenges=%s".formatted(indent, shortId(card.getUUID()), card.getType().getId(),
                tier.name(), potion, challenges));
    }

    private static String shortId(UUID uuid) {
        return uuid.toString().substring(0, 8);
    }
}
