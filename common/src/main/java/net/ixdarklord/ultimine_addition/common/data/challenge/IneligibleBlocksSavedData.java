package net.ixdarklord.ultimine_addition.common.data.challenge;

import net.ixdarklord.ultimine_addition.config.ConfigHandler;
import net.ixdarklord.ultimine_addition.core.FTBUltimineAddition;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.saveddata.SavedData;
import org.jetbrains.annotations.NotNull;

import java.util.*;

import static net.ixdarklord.ultimine_addition.core.FTBUltimineAddition.LOGGER;

public class IneligibleBlocksSavedData extends SavedData {
    public static final String DATA_KEY = FTBUltimineAddition.MOD_ID + ".ineligible_blocks";
    private final ServerLevel level;
    private final Map<ChunkPos, List<BlockEntry>> chunkEntries; // Changed to Map<ChunkPos, List<BlockEntry>>

    public IneligibleBlocksSavedData(ServerLevel level, Map<ChunkPos, List<BlockEntry>> chunkEntries) {
        this.level = level;
        this.chunkEntries = chunkEntries;
    }

    public void add(Entity entity, BlockInfo blockInfo) {
        ResourceLocation entityId = BuiltInRegistries.ENTITY_TYPE.getKey(entity.getType());
        UUID entityUUID = entity.getUUID();
        ChunkPos chunkPos = level.getChunk(blockInfo.pos).getPos();

        List<BlockEntry> blockEntryList = chunkEntries.computeIfAbsent(chunkPos, k -> new ArrayList<>());

        Optional<BlockEntry> existingEntry = blockEntryList.stream()
                .filter(entry -> entry.placerData.entityId.equals(entityId) && entry.placerData.entityUUID.equals(entityUUID))
                .findFirst();

        if (existingEntry.isPresent()) {
            BlockEntry blockEntry = existingEntry.get();
            if (!blockEntry.placedBlocks.contains(blockInfo)) {
                blockEntry.placedBlocks.add(blockInfo);
                setDirty();
            }
        } else {
            BlockEntry blockEntry = new BlockEntry(new PlacerData(entityId, entityUUID), new ArrayList<>(Collections.singletonList(blockInfo)));
            blockEntryList.add(blockEntry);
            setDirty();
        }

        if (ConfigHandler.SERVER.INELIGIBLE_BLOCKS_LOGGER.get()) {
            ResourceLocation blockId = BuiltInRegistries.BLOCK.getKey(blockInfo.blockState.getBlock());
            LOGGER.debug("[Ineligible Blocks] Block added at: {} with ID: {} by {}", blockInfo.pos, blockId, entityId);
        }
    }

    public void remove(BlockPos pos) {
        ChunkPos chunkPos = level.getChunk(pos).getPos();
        List<BlockEntry> blockEntryList = chunkEntries.get(chunkPos);
        if (blockEntryList == null) return;

        boolean isDirty = false;
        BlockState removedBlockState = null;

        Iterator<BlockEntry> blockEntryIterator = blockEntryList.iterator();
        while (blockEntryIterator.hasNext()) {
            BlockEntry blockEntry = blockEntryIterator.next();
            Iterator<BlockInfo> blockInfoIterator = blockEntry.placedBlocks.iterator();

            while (blockInfoIterator.hasNext()) {
                BlockInfo blockInfo = blockInfoIterator.next();
                if (blockInfo.pos.equals(pos)) {
                    removedBlockState = blockInfo.blockState;
                    blockInfoIterator.remove();
                    isDirty = true;
                }
            }

            if (blockEntry.placedBlocks.isEmpty()) {
                blockEntryIterator.remove();
                isDirty = true;
            }
        }

        if (blockEntryList.isEmpty()) {
            chunkEntries.remove(chunkPos);
            isDirty = true;
        }

        if (isDirty) {
            setDirty();
            if (ConfigHandler.SERVER.INELIGIBLE_BLOCKS_LOGGER.get() && removedBlockState != null) {
                ResourceLocation blockId = BuiltInRegistries.BLOCK.getKey(removedBlockState.getBlock());
                LOGGER.debug("[Ineligible Blocks] Block removed at: {} with ID: {}", pos, blockId);
            }
        }
    }

    public boolean isBlockPlacedByEntity(BlockPos pos) {
        return chunkEntries.values().stream()
                .flatMap(List::stream)
                .flatMap(blockEntry -> blockEntry.placedBlocks.stream())
                .anyMatch(blockInfo -> blockInfo.pos.equals(pos));
    }

    public Map<ChunkPos, List<BlockEntry>> getChunkEntries() {
        return chunkEntries;
    }

    public static IneligibleBlocksSavedData getOrCreate(ServerLevel level) {
        return level.getDataStorage().computeIfAbsent(getFactory(level), DATA_KEY);
    }

    public static Factory<IneligibleBlocksSavedData> getFactory(ServerLevel level) {
        return new Factory<>(() -> create(level), (NBT, provider) -> load(level, NBT), DataFixTypes.LEVEL);
    }

    private static IneligibleBlocksSavedData create(ServerLevel level) {
        return new IneligibleBlocksSavedData(level, new HashMap<>());
    }

    private static IneligibleBlocksSavedData load(ServerLevel level, CompoundTag NBT) {
        return new IneligibleBlocksSavedData(level, deserializeChunkEntries(NBT));
    }

    @Override
    public @NotNull CompoundTag save(CompoundTag NBT, HolderLookup.Provider registries) {
        NBT.merge(serializeChunkEntries());
        return NBT;
    }

    private CompoundTag serializeChunkEntries() {
        ListTag entriesTagList = new ListTag();
        for (Map.Entry<ChunkPos, List<BlockEntry>> entry : chunkEntries.entrySet()) {
            ChunkPos chunkPos = entry.getKey();
            List<BlockEntry> blockEntryList = entry.getValue();

            CompoundTag entryTag = new CompoundTag();
            entryTag.put("ChunkPos", writeChunkPos(chunkPos));
            entryTag.put("BlockEntries", blockEntryList.stream()
                    .map(BlockEntry::serialize)
                    .collect(ListTag::new, AbstractList::add, AbstractCollection::addAll));
            entriesTagList.add(entryTag);
        }

        CompoundTag finalTag = new CompoundTag();
        finalTag.put("ChunkEntries", entriesTagList);
        return finalTag;
    }

    private static Map<ChunkPos, List<BlockEntry>> deserializeChunkEntries(CompoundTag tag) {
        Map<ChunkPos, List<BlockEntry>> chunkEntries = new HashMap<>();
        ListTag entriesListTag = tag.getList("ChunkEntries", 10);

        for (int i = 0; i < entriesListTag.size(); i++) {
            CompoundTag entryTag = entriesListTag.getCompound(i);
            ChunkPos chunkPos = readChunkPos(entryTag.getCompound("ChunkPos"));
            List<BlockEntry> blockEntryList = entryTag.getList("BlockEntries", 10).stream()
                    .map(blockEntryTag -> BlockEntry.deserialize((CompoundTag) blockEntryTag))
                    .toList();
            chunkEntries.put(chunkPos, new ArrayList<>(blockEntryList));
        }

        return chunkEntries;
    }

    private static CompoundTag writeChunkPos(ChunkPos chunkPos) {
        CompoundTag NBT = new CompoundTag();
        NBT.putInt("X", chunkPos.x);
        NBT.putInt("Z", chunkPos.z);
        return NBT;
    }

    private static ChunkPos readChunkPos(CompoundTag tag) {
        return new ChunkPos(tag.getInt("X"), tag.getInt("Z"));
    }

    public void validateBlocks(ServerLevel level) {
        boolean isDirty = false;

        Iterator<Map.Entry<ChunkPos, List<BlockEntry>>> chunkIterator = chunkEntries.entrySet().iterator();
        while (chunkIterator.hasNext()) {
            Map.Entry<ChunkPos, List<BlockEntry>> entry = chunkIterator.next();
            List<BlockEntry> blockEntryList = entry.getValue();

            Iterator<BlockEntry> blockEntryIterator = blockEntryList.iterator();
            while (blockEntryIterator.hasNext()) {
                BlockEntry blockEntry = blockEntryIterator.next();
                Iterator<BlockInfo> blockInfoIterator = blockEntry.placedBlocks.iterator();

                while (blockInfoIterator.hasNext()) {
                    BlockInfo blockInfo = blockInfoIterator.next();
                    if (!level.getBlockState(blockInfo.pos).is(blockInfo.blockState.getBlock())) {
                        blockInfoIterator.remove();
                        isDirty = true;
                    }
                }

                if (blockEntry.placedBlocks.isEmpty()) {
                    blockEntryIterator.remove();
                    isDirty = true;
                }
            }

            if (blockEntryList.isEmpty()) {
                chunkIterator.remove();
                isDirty = true;
            }
        }

        if (isDirty) {
            setDirty();
        }
    }

    public record BlockEntry(PlacerData placerData, List<BlockInfo> placedBlocks) {
        public CompoundTag serialize() {
            CompoundTag tag = new CompoundTag();
            ListTag blocksList = new ListTag();
            for (BlockInfo info : placedBlocks) {
                CompoundTag blockTag = new CompoundTag();
                blockTag.put("State", NbtUtils.writeBlockState(info.blockState));
                blockTag.put("Pos", NbtUtils.writeBlockPos(info.pos));
                blocksList.add(blockTag);
            }

            CompoundTag placer = new CompoundTag();
            placer.putString("Id", placerData.entityId.toString());
            placer.putUUID("UUID", placerData.entityUUID);

            tag.put("Placer", placer);
            tag.put("Blocks", blocksList);
            return tag;
        }

        public static BlockEntry deserialize(CompoundTag tag) {
            CompoundTag placer = tag.getCompound("Placer");
            ResourceLocation id = ResourceLocation.parse(placer.getString("Id"));
            UUID uuid = placer.getUUID("UUID");

            List<BlockInfo> blockInfoList = new ArrayList<>();
            ListTag blocksListTag = tag.getList("Blocks", 10);
            for (int i = 0; i < blocksListTag.size(); i++) {
                CompoundTag tag2 = blocksListTag.getCompound(i);
                CompoundTag stateTag = tag2.getCompound("State");

                BlockState blockState = NbtUtils.readBlockState(BuiltInRegistries.BLOCK.asLookup(), stateTag);
                Optional<BlockPos> blockPos = NbtUtils.readBlockPos(tag2, "Pos");
                blockPos.ifPresent(pos -> blockInfoList.add(new BlockInfo(pos, blockState)));
            }

            return new BlockEntry(new PlacerData(id, uuid), blockInfoList);
        }
    }

    public record PlacerData(ResourceLocation entityId, UUID entityUUID) {
        @Override
        public boolean equals(Object o) {
            if (!(o instanceof PlacerData(ResourceLocation id, UUID uuid))) return false;
            return Objects.equals(entityUUID, uuid) && Objects.equals(entityId, id);
        }

        @Override
        public int hashCode() {
            return Objects.hash(entityId, entityUUID);
        }
    }

    public record BlockInfo(BlockPos pos, BlockState blockState) {
        @Override
        public String toString() {
            return "{\"State\": \"%s\", \"Pos\": \"%s\"}".formatted(NbtUtils.writeBlockState(blockState), NbtUtils.writeBlockPos(pos));
        }

        @Override
        public boolean equals(Object o) {
            if (!(o instanceof BlockInfo(BlockPos pos1, BlockState state))) return false;
            return Objects.equals(blockState, state) && Objects.equals(pos, pos1);
        }

        @Override
        public int hashCode() {
            return Objects.hash(blockState, pos);
        }
    }
}