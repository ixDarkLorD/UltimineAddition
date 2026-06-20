package net.ixdarklord.ultimine_addition.core;

import dev.architectury.injectables.annotations.ExpectPlatform;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;

public interface ServicePlatform {
    @ExpectPlatform
    static ServicePlatform get() {
        throw new AssertionError();
    }

    void registerConfig();

    SlotAPI slotAPI();

    Players players();

    interface Players {
        boolean isPlayerUltimineCapable(Player player);

        void setPlayerUltimineCapability(Player player, boolean flag);

        boolean isCorrectToolForBlock(ItemStack itemStack, BlockState blockState);

        boolean isToolPaxel(ItemStack itemStack);

        double getBlockReachAttribute(Player player);
    }

    interface SlotAPI {
        String getAPIName();

        boolean isModLoaded();

        ItemStack getSkillsRecordItem(Player player);
    }
}
