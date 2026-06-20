package net.ixdarklord.ultimine_addition.hooks;

import dev.architectury.injectables.annotations.ExpectPlatform;
import net.minecraft.client.KeyMapping;

public class KeyBindingHooks {
    @ExpectPlatform
    public static boolean isMatches(KeyMapping keyMapping, int keyCode, int scanCode) {
        throw new AssertionError();
    }
}
