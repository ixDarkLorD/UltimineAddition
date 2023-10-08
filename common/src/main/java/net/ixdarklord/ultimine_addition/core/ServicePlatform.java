package net.ixdarklord.ultimine_addition.core;

import dev.architectury.injectables.annotations.ExpectPlatform;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;

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
    
    public static class Tags {
        @ExpectPlatform
        public static TagKey<Item> getPickaxes() {
            throw new AssertionError();
        }
        @ExpectPlatform
        public static TagKey<Item> getAxes() {
            throw new AssertionError();
        }
        @ExpectPlatform
        public static TagKey<Item> getShovels() {
            throw new AssertionError();
        }
        @ExpectPlatform
        public static TagKey<Item> getHoes() {
            throw new AssertionError();
        }
    }
}
