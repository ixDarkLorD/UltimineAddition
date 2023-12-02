package net.ixdarklord.ultimine_addition.core.forge;

import dev.architectury.platform.Platform;
import net.ixdarklord.ultimine_addition.integration.curios.CuriosIntegration;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

@SuppressWarnings("unused")
public class ServicePlatformSlotAPIImpl {
    public static boolean isModLoaded() {
        return Platform.isModLoaded("curios");
    }
    public static ItemStack getSkillsRecordItem(Player player) {
        return CuriosIntegration.getSkillsRecordItem(player);
    }
}
