package net.ixdarklord.ultimine_addition.core.fabric;

import dev.architectury.platform.Platform;
import net.ixdarklord.ultimine_addition.integration.trinkets.TrinketsIntegration;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public final class ServicePlatformSlotAPIImpl {
    public static boolean isModLoaded() {
        return Platform.isModLoaded("trinkets");
    }
    public static ItemStack getSkillsRecordItem(Player player) {
        return TrinketsIntegration.getSkillsRecordItem(player);
    }
}
