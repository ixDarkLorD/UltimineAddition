package net.ixdarklord.ultimine_addition.client.handler;

import net.minecraft.client.Minecraft;
import net.minecraft.world.item.CreativeModeTabs;

// The creative tabs are built once and cached until the features, permissions or registries change; the playstyle
// mode changes the mod's tab, so the cache is dropped and the creative screen rebuilds every tab (and its search)
// the next time it opens.
public final class PlaystyleModeClient {
    private PlaystyleModeClient() {}

    public static void onModeChanged() {
        Minecraft.getInstance().execute(() -> CreativeModeTabs.CACHED_PARAMETERS = null);
    }
}
