package net.ixdarklord.ultimine_addition.integration.trinkets;

import eu.pb4.trinkets.api.TrinketSlotAccess;
import eu.pb4.trinkets.api.TrinketsApi;
import net.ixdarklord.ultimine_addition.core.Registration;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public class TrinketsIntegration {
    public static ItemStack getSkillsRecordItem(Player player) {
        return TrinketsApi.getAttachment(player)
                .findFirst(Registration.SKILLS_RECORD.get())
                .map(TrinketSlotAccess::get)
                .orElse(ItemStack.EMPTY);
    }
}
