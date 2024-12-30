package net.ixdarklord.ultimine_addition.core.neoforge;

import dev.architectury.platform.Platform;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

@SuppressWarnings("unused")
public class ServicePlatformSlotAPIImpl {
    public static boolean isModLoaded() {
        return Platform.isModLoaded("trinkets");
    }
    public static ItemStack getSkillsRecordItem(Player player) {
        return ItemStack.EMPTY;
    }
}
