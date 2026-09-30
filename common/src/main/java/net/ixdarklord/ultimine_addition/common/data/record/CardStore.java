package net.ixdarklord.ultimine_addition.common.data.record;

import net.ixdarklord.coolcatcore.api.platform.Platform;
import net.ixdarklord.ultimine_addition.common.data.item.MiningSkillCardData;
import net.minecraft.server.MinecraftServer;
import org.jetbrains.annotations.Nullable;

// Side by thread: the server thread uses the SavedData, anything else the client cache.
public final class CardStore {
    private CardStore() {}

    private static @Nullable MinecraftServer serverThread() {
        MinecraftServer server = Platform.getServer();
        return server != null && server.isSameThread() ? server : null;
    }

    public static boolean isServerThread() {
        return serverThread() != null;
    }

    public static @Nullable CardProgress progress(MiningSkillCardData card) {
        MinecraftServer server = serverThread();
        if (server != null) return SkillsRecordSavedData.get(server).findProgress(card);
        return SkillsRecordClientCache.getProgress(card.getUUID());
    }

    public static void changed(MiningSkillCardData card) {
        MinecraftServer server = serverThread();
        if (server != null) SkillsRecordSavedData.get(server).onCardChanged(card);
    }
}
