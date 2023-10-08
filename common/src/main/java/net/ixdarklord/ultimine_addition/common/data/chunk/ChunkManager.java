package net.ixdarklord.ultimine_addition.common.data.chunk;

import net.ixdarklord.ultimine_addition.core.Constants;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.chunk.ChunkAccess;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ConcurrentModificationException;
import java.util.HashMap;
import java.util.Map;

public class ChunkManager {
    public static ChunkManager INSTANCE = new ChunkManager();
    private final Logger LOGGER = LoggerFactory.getLogger(Constants.MOD_NAME + "/ChunkManager");
    private final Map<ChunkAccess, ChunkData> chunks = new HashMap<>();

    public ChunkData getChunkData(ChunkAccess chunk) {
        if (chunks.isEmpty() || !chunks.containsKey(chunk))
            chunks.put(chunk, new ChunkData());
        return chunks.get(chunk);
    }

    public void loadChunk(ChunkAccess chunk, CompoundTag data) {
        chunks.put(chunk, new ChunkData().loadData(data));
    }

    public void unloadChunk(ChunkAccess chunk) {
        chunks.remove(chunk);
    }

    public void saveChunkData(ChunkAccess chunk, CompoundTag data) {
        if (chunks.isEmpty() || !chunks.containsKey(chunk)) return;
        chunks.get(chunk).enableDebug(LOGGER).saveData(data);
    }

    public ChunkManager validateChunkData(LevelAccessor level) {
        try {
            chunks.forEach((chunk, data) -> data.getPlacedBlocks().forEach((uuid, blockInfoList) ->
                    blockInfoList.removeIf(blockInfo -> level.getBlockState(blockInfo.pos()).getBlock() != blockInfo.blockState().getBlock())));
        } catch (ConcurrentModificationException ignored) {}
        return this;
    }
}
