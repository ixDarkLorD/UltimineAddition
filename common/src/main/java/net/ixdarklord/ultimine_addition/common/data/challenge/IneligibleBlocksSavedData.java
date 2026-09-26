package net.ixdarklord.ultimine_addition.common.data.challenge;

import net.minecraft.world.level.saveddata.SavedDataType;
import net.minecraft.core.UUIDUtil;
import com.mojang.serialization.Codec;
import net.ixdarklord.ultimine_addition.config.ConfigHandler;
import net.ixdarklord.ultimine_addition.core.FTBUltimineAddition;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.resources.Identifier;
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
    // Keeps the 1.21.1 NBT layout, wrapped in a codec as SavedData now requires.
    public static final Codec<IneligibleBlocksSavedData> CODEC = CompoundTag.CODEC.xmap(
            tag -> new IneligibleBlocksSavedData(deserializeChunkEntries(tag)),
            IneligibleBlocksSavedData::serializeChunkEntries);
    public static final SavedDataType<IneligibleBlocksSavedData> TYPE = new SavedDataType<>(
            FTBUltimineAddition.id("ineligible_blocks"), IneligibleBlocksSavedData::new, CODEC, DataFixTypes.LEVEL);
    private final Map<ChunkPos, List<BlockEntry>> chunkEntries;

    public IneligibleBlocksSavedData() {
        this(new HashMap<>());
    }

    public IneligibleBlocksSavedData(Map<ChunkPos, List<BlockEntry>> chunkEntries) {
        this.chunkEntries = chunkEntries;
    }

    public void add(Entity entity, BlockInfo blockInfo) {
        Identifier entityId = BuiltInRegistries.ENTITY_TYPE.getKey(entity.getType());
        UUID entityUUID = entity.getUUID();
        ChunkPos chunkPos = ChunkPos.containing(blockInfo.pos);

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
            Identifier blockId = BuiltInRegistries.BLOCK.getKey(blockInfo.blockState.getBlock());
            LOGGER.debug("[Ineligible Blocks] Block added at: {} with ID: {} by {}", blockInfo.pos, blockId, entityId);
        }
    }

    public void remove(BlockPos pos) {
        ChunkPos chunkPos = ChunkPos.containing(pos);
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
                Identifier blockId = BuiltInRegistries.BLOCK.getKey(removedBlockState.getBlock());
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
        return level.getDataStorage().computeIfAbsent(TYPE);
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
        ListTag entriesListTag = tag.getListOrEmpty("ChunkEntries");

        for (int i = 0; i < entriesListTag.size(); i++) {
            CompoundTag entryTag = entriesListTag.getCompoundOrEmpty(i);
            ChunkPos chunkPos = readChunkPos(entryTag.getCompoundOrEmpty("ChunkPos"));
            List<BlockEntry> blockEntryList = entryTag.getListOrEmpty("BlockEntries").stream()
                    .map(blockEntryTag -> BlockEntry.deserialize((CompoundTag) blockEntryTag))
                    .toList();
            chunkEntries.put(chunkPos, new ArrayList<>(blockEntryList));
        }

        return chunkEntries;
    }

    private static CompoundTag writeChunkPos(ChunkPos chunkPos) {
        CompoundTag NBT = new CompoundTag();
        NBT.putInt("X", chunkPos.x());
        NBT.putInt("Z", chunkPos.z());
        return NBT;
    }

    private static ChunkPos readChunkPos(CompoundTag tag) {
        return new ChunkPos(tag.getIntOr("X", 0), tag.getIntOr("Z", 0));
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
                blockTag.store("Pos", BlockPos.CODEC, info.pos);
                blocksList.add(blockTag);
            }

            CompoundTag placer = new CompoundTag();
            placer.putString("Id", placerData.entityId.toString());
            placer.store("UUID", UUIDUtil.CODEC, placerData.entityUUID);

            tag.put("Placer", placer);
            tag.put("Blocks", blocksList);
            return tag;
        }

        public static BlockEntry deserialize(CompoundTag tag) {
            CompoundTag placer = tag.getCompoundOrEmpty("Placer");
            Identifier id = Identifier.parse(placer.getStringOr("Id", ""));
            UUID uuid = placer.read("UUID", UUIDUtil.CODEC).orElseThrow();

            List<BlockInfo> blockInfoList = new ArrayList<>();
            ListTag blocksListTag = tag.getListOrEmpty("Blocks");
            for (int i = 0; i < blocksListTag.size(); i++) {
                CompoundTag tag2 = blocksListTag.getCompoundOrEmpty(i);
                CompoundTag stateTag = tag2.getCompoundOrEmpty("State");

                BlockState blockState = NbtUtils.readBlockState(BuiltInRegistries.BLOCK, stateTag);
                Optional<BlockPos> blockPos = tag2.read("Pos", BlockPos.CODEC);
                blockPos.ifPresent(pos -> blockInfoList.add(new BlockInfo(pos, blockState)));
            }

            return new BlockEntry(new PlacerData(id, uuid), blockInfoList);
        }
    }

    public record PlacerData(Identifier entityId, UUID entityUUID) {
        @Override
        public boolean equals(Object o) {
            if (!(o instanceof PlacerData(Identifier id, UUID uuid))) return false;
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
            return "{\"State\": \"%s\", \"Pos\": \"%s\"}".formatted(NbtUtils.writeBlockState(blockState), pos.toShortString());
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