package net.ixdarklord.ultimine_addition.core.forge;

import dev.architectury.platform.Platform;
import net.ixdarklord.ultimine_addition.core.ServicePlatform;
import net.ixdarklord.ultimine_addition.integration.curios.CuriosIntegration;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public final class ServicePlatformSlotAPIImpl implements ServicePlatform.SlotAPI {
    public String getAPIName() {
        return "curios";
    }

    public boolean isModLoaded() {
        return Platform.isModLoaded(this.getAPIName());
    }

    public ItemStack getSkillsRecordItem(Player player) {
        return CuriosIntegration.getItem(player);
    }
}
