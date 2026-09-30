package net.ixdarklord.ultimine_addition.common.event.impl;

import net.ixdarklord.coolcatcore.api.event.v2.core.EventInvoker;
import net.minecraft.core.RegistryAccess;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.packs.resources.CloseableResourceManager;

public class DatapackEvents {
    public static final EventInvoker<PreReload> PRE_RELOAD = EventInvoker.create(PreReload.class);
    public static final EventInvoker<SyncContents> SYNC = EventInvoker.create(SyncContents.class);
    public static final EventInvoker<PostReload> POST_RELOAD = EventInvoker.create(PostReload.class);
    public static final EventInvoker<TagUpdate> TAG_UPDATE = EventInvoker.create(TagUpdate.class);

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
