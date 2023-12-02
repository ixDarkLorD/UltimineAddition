package net.ixdarklord.ultimine_addition.common.event;

import dev.architectury.event.CompoundEventResult;
import dev.architectury.event.EventResult;
import dev.architectury.event.events.common.BlockEvent;
import dev.architectury.event.events.common.ChunkEvent;
import net.ixdarklord.ultimine_addition.common.data.chunk.ChunkData;
import net.ixdarklord.ultimine_addition.common.data.chunk.ChunkManager;
import net.ixdarklord.ultimine_addition.common.event.impl.BlockToolModificationEvent;
import net.ixdarklord.ultimine_addition.common.event.impl.ChunkUnloadEvent;

import java.util.concurrent.atomic.AtomicBoolean;

public class ChunkEvents {
    public static void init() {
        AtomicBoolean i = new AtomicBoolean();
        BlockToolModificationEvent.EVENT.register((originalState, finalState, context, toolAction, simulate) -> {
            i.set(true);
            return CompoundEventResult.pass();
        });
        BlockEvent.PLACE.register((level, pos, state, placer) -> {
            ChunkData data = ChunkManager.INSTANCE.getChunkData(level.getChunk(pos));
            if (!i.get() && placer != null) data.addBlock(placer, new ChunkData.BlockInfo(state, pos));
            i.set(false);
            return EventResult.pass();
        });

        BlockEvent.BREAK.register((level, pos, state, player, xp) -> {
            ChunkData data = ChunkManager.INSTANCE.getChunkData(level.getChunk(pos));
            data.removeBlock(new ChunkData.BlockInfo(state, pos));
            return EventResult.pass();
        });

        ChunkEvent.LOAD_DATA.register((chunk, level, nbt) -> ChunkManager.INSTANCE.loadChunk(level, chunk, nbt));
        ChunkEvent.SAVE_DATA.register((chunk, level, nbt) -> ChunkManager.INSTANCE.validateChunkData(level).saveChunkData(chunk, nbt));
        ChunkUnloadEvent.EVENT.register((chunk, level) -> ChunkManager.INSTANCE.unloadChunk(chunk));
    }
}
