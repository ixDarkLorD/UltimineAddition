package net.ixdarklord.ultimine_addition.hooks.fabric;

import net.minecraft.client.input.KeyEvent;
import net.ixdarklord.ultimine_addition.hooks.KeyBindingHooks;
import net.minecraft.client.KeyMapping;

public class KeyBindingHooksImpl {
    /**
     * {@link KeyBindingHooks#isMatches(KeyMapping, int, int)}
     */
    public static boolean isMatches(KeyMapping keyMapping, int keyCode, int scanCode) {
        return keyMapping.matches(new KeyEvent(keyCode, scanCode, 0));
    }
}
