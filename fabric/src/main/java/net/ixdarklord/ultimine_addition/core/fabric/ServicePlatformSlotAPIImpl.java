package net.ixdarklord.ultimine_addition.core.fabric;

import dev.architectury.platform.Platform;
import net.ixdarklord.ultimine_addition.core.ServicePlatform;
import net.ixdarklord.ultimine_addition.integration.trinkets.TrinketsIntegration;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public final class ServicePlatformSlotAPIImpl implements ServicePlatform.SlotAPI {

    @Override
    public String getAPIName() {
        return "trinkets";
    }

    @Override
    public boolean isModLoaded() {
        return Platform.isModLoaded(getAPIName());
    }

    @Override
    public ItemStack getSkillsRecordItem(Player player) {
        return TrinketsIntegration.getSkillsRecordItem(player);
    }
}
