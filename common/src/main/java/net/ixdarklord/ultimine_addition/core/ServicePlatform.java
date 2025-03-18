package net.ixdarklord.ultimine_addition.core;

import dev.architectury.injectables.annotations.ExpectPlatform;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;

public interface ServicePlatform {
    @ExpectPlatform
    static ServicePlatform get() {
        throw new UnsupportedOperationException("This method has not been implemented in the loader.");
    }

    void registerConfig();

    SlotAPI slotAPI();

    Players players();

    interface SlotAPI {
        String getAPIName();

        boolean isModLoaded();

        ItemStack getSkillsRecordItem(Player player);
    }

    interface Players {
        boolean isPlayerUltimineCapable(Player player);

        void setPlayerUltimineCapability(Player player, boolean state);

        double getReachAttribute(Player player);

        boolean isCorrectToolForBlock(ItemStack stack, BlockState blockState);

        boolean isToolPaxel(ItemStack stack);
    }
}
