package net.ixdarklord.ultimine_addition.common.event;

import dev.architectury.event.events.common.TickEvent;
import net.ixdarklord.ultimine_addition.common.data.item.MiningSkillCardData;
import net.ixdarklord.ultimine_addition.common.data.item.SkillsRecordData;
import net.ixdarklord.ultimine_addition.common.item.MiningSkillCardItem;
import net.ixdarklord.ultimine_addition.common.item.ModItems;
import net.ixdarklord.ultimine_addition.common.item.SkillsRecordItem;
import net.ixdarklord.ultimine_addition.util.ItemUtils;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

import java.util.List;
import java.util.stream.Collectors;

import static net.ixdarklord.ultimine_addition.config.ConfigHandler.COMMON.MASTERED_CARD_EFFECT;

public class MSCEvents {
    public static void init() {
        TickEvent.PLAYER_POST.register(instance -> {
            if (!MASTERED_CARD_EFFECT.get()) return;
            if (instance instanceof ServerPlayer player) {
                List<ItemStack> recordsList = ItemUtils.listMatchingItem(player, ModItems.SKILLS_RECORD);
                List<ItemStack> cardsList = player.getInventory().items.stream()
                        .filter(stack -> stack.getItem().getClass() == MiningSkillCardItem.class)
                        .collect(Collectors.toList());

                for (ItemStack stack : recordsList) {
                    SkillsRecordData data = ((SkillsRecordItem) stack.getItem()).getData(stack);
                    cardsList.addAll(data.getCardSlots().stream().filter(itemStack -> !itemStack.isEmpty()).toList());
                }

                for (ItemStack stack : cardsList) {
                    if (stack.getItem() instanceof MiningSkillCardItem cardItem) {
                        MiningSkillCardData data = cardItem.getData(stack);
                        if (data.getTier() != MiningSkillCardItem.Tier.Mastered) continue;
                        MiningSkillCardItem.giveMineGoJuice(player, cardItem.getType());
                    }
                }
            }
        });
    }
}
