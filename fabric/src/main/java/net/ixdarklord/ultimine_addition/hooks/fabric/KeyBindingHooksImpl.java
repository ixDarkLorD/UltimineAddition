package net.ixdarklord.ultimine_addition.hooks.fabric;

import net.minecraft.client.KeyMapping;

public class KeyBindingHooksImpl {
    public static boolean isMatches(KeyMapping keyMapping, int keyCode, int scanCode) {
        return keyMapping.matches(keyCode, scanCode);
    }
}
