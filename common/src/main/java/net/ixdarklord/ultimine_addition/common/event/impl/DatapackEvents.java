package net.ixdarklord.ultimine_addition.common.event.impl;

import dev.architectury.event.Event;
import dev.architectury.event.EventFactory;
import net.minecraft.core.RegistryAccess;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.packs.resources.CloseableResourceManager;

public class DatapackEvents {
    public static final Event<PreReload> PRE_RELOAD = EventFactory.createLoop(new PreReload[0]);
    public static final Event<SyncContents> SYNC = EventFactory.createLoop(new SyncContents[0]);
    public static final Event<PostReload> POST_RELOAD = EventFactory.createLoop(new PostReload[0]);
    public static final Event<TagUpdate> TAG_UPDATE = EventFactory.createLoop(new TagUpdate[0]);

    public interface PostReload {
        void init(MinecraftServer minecraftServer, CloseableResourceManager closeableResourceManager, boolean flag);
    }

    public interface PreReload {
        void init(MinecraftServer minecraftServer, CloseableResourceManager closeableResourceManager);
    }

    public interface SyncContents {
        void init(ServerPlayer serverPlayer, boolean flag);
    }

    public interface TagUpdate {
        void init(RegistryAccess registryAccess, Cause cause, boolean flag);

        enum Cause {
            SERVER_DATA_LOAD,
            CLIENT_PACKET_RECEIVED
        }
    }
}
