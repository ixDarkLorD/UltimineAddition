package net.ixdarklord.ultimine_addition.common.event.impl;

import dev.architectury.event.Event;
import dev.architectury.event.EventFactory;
import net.minecraft.core.RegistryAccess;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.packs.resources.CloseableResourceManager;

public class DatapackEvents {
    public static final Event<PreReload> PRE_RELOAD = EventFactory.createLoop();
    public static final Event<SyncContents> SYNC = EventFactory.createLoop();
    public static final Event<PostReload> POST_RELOAD = EventFactory.createLoop();
    public static final Event<TagUpdate> TAG_UPDATE = EventFactory.createLoop();

    public interface PreReload {
        void init(MinecraftServer server, CloseableResourceManager resourceManager);
    }

    public interface SyncContents {
        void init(ServerPlayer player, boolean isJoined);
    }

    public interface PostReload {
        void init(MinecraftServer server, CloseableResourceManager resourceManager, boolean isSuccess);
    }

    public interface TagUpdate {
        void init(RegistryAccess registryAccess, Cause cause, boolean shouldUpdateStaticData);

        enum Cause {
            /**
             * The tag update is caused by the server loading datapack data. Note that in single player this still happens
             * on the client thread.
             */
            SERVER_DATA_LOAD,
            /**
             * The tag update is caused by the client receiving the tag data from the server.
             */
            CLIENT_PACKET_RECEIVED
        }
    }
}
