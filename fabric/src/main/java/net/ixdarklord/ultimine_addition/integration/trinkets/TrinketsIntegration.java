package net.ixdarklord.ultimine_addition.integration.trinkets;

import dev.emi.trinkets.api.TrinketsApi;
import net.ixdarklord.ultimine_addition.core.Registration;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public class TrinketsIntegration {
    public static ItemStack getSkillsRecordItem(Player player) {
        return TrinketsApi.getTrinketComponent(player)
                .map(trinketComponent -> trinketComponent.getEquipped(Registration.SKILLS_RECORD.get()))
                .filter(tuples -> !tuples.isEmpty())
                .map(tuples -> tuples.get(0).getB())
                .orElse(ItemStack.EMPTY);
    }
}
