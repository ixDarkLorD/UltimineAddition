package net.ixdarklord.ultimine_addition.common.event.impl;

import dev.architectury.event.Event;
import dev.architectury.event.EventFactory;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.chunk.ChunkAccess;
import org.jetbrains.annotations.Nullable;

public class ChunkUnloadEvent {
    public static final Event<UnloadData> EVENT = EventFactory.createLoop();
    public interface UnloadData {
        void Unload(ChunkAccess chunk, @Nullable ServerLevel level);
    }
}
