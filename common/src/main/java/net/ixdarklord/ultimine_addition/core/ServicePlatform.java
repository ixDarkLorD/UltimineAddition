package net.ixdarklord.ultimine_addition.core;

import dev.architectury.injectables.annotations.ExpectPlatform;
import net.minecraft.world.entity.player.Player;

@SuppressWarnings("unused")
public class ServicePlatform {
    @ExpectPlatform
    public static void registerConfig() {
        throw new AssertionError();
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
    }
}
