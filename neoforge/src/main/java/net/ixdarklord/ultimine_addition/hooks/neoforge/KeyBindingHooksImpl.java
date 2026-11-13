package net.ixdarklord.ultimine_addition.hooks.neoforge;

import com.mojang.blaze3d.platform.InputConstants;
import net.ixdarklord.ultimine_addition.hooks.KeyBindingHooks;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.input.KeyEvent;

public class KeyBindingHooksImpl {
    /**
     * {@link KeyBindingHooks#isMatches(KeyMapping, int, int)}
     */
    public static boolean isMatches(KeyMapping keyMapping, int keyCode, int scanCode) {
        return keyMapping.isActiveAndMatches(InputConstants.getKey(new KeyEvent(keyCode, scanCode, 0)));
    }
}
