package net.ixdarklord.ultimine_addition.core.neoforge;

import dev.architectury.platform.Platform;
import net.ixdarklord.ultimine_addition.core.ServicePlatform;
import net.ixdarklord.ultimine_addition.integration.curios.CuriosIntegration;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public class ServicePlatformSlotAPIImpl implements ServicePlatform.SlotAPI {
    @Override
    public String getAPIName() {
        return "curios";
    }

    @Override
    public boolean isModLoaded() {
        return Platform.isModLoaded(getAPIName());
    }

    @Override
    public ItemStack getSkillsRecordItem(Player player) {
        return CuriosIntegration.getSkillsRecord(player);
    }
}
