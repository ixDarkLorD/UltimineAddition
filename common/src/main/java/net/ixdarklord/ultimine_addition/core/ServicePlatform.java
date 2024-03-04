package net.ixdarklord.ultimine_addition.core;

import dev.architectury.injectables.annotations.ExpectPlatform;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;

@SuppressWarnings("unused")
public class ServicePlatform {
    @ExpectPlatform
    public static void registerConfig() {
        throw new AssertionError();
    }

    public static class SlotAPI {
        @ExpectPlatform
        public static boolean isModLoaded() {
            throw new AssertionError();
        }
        @ExpectPlatform
        public static ItemStack getSkillsRecordItem(Player player) {
            throw new AssertionError();
        }
    }

    public static class Players {
        @ExpectPlatform
        public static boolean isPlayerUltimineCapable(Player player) {
            throw new AssertionError();
        }
        @ExpectPlatform
        public static void setPlayerUltimineCapability(Player player, boolean state) {
            throw new AssertionError();
        }
        @ExpectPlatform
        public static double getReachAttribute(Player player) {
            throw new AssertionError();
        }
        @ExpectPlatform
        public static boolean isCorrectToolForBlock(ItemStack stack, BlockState blockState) {
            throw new AssertionError();
        }
    }
}
