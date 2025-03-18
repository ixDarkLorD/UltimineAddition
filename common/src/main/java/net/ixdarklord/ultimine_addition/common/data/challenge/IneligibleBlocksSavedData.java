package net.ixdarklord.ultimine_addition.common.data.challenge;

import net.ixdarklord.ultimine_addition.config.ConfigHandler;
import net.ixdarklord.ultimine_addition.core.UltimineAddition;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.stream.Collectors;

public class IneligibleBlocksSavedData extends SavedData {
    public static final String DATA_KEY = UltimineAddition.MOD_ID + ".ineligible_blocks";
    private final Logger LOGGER = LoggerFactory.getLogger(UltimineAddition.MOD_NAME + "/IneligibleBlocks");
    private final ServerLevel level;
    private final Map<ChunkPos, List<BlockEntry>> ChunkEntries;

    public IneligibleBlocksSavedData(ServerLevel level, Map<ChunkPos, List<BlockEntry>> ChunkEntries) {
        this.level = level;
        this.ChunkEntries = ChunkEntries;
    }

    public void add(Entity entity, BlockInfo blockInfo) {
        ResourceLocation id = BuiltInRegistries.ENTITY_TYPE.getKey(entity.getType());
        ChunkPos chunkPos = level.getChunk(blockInfo.pos).getPos();
        List<BlockEntry> blockEntryList = ChunkEntries.get(chunkPos);
        if (blockEntryList == null) blockEntryList = new ArrayList<>();

        boolean isPosExists = false;
        for (BlockEntry blockEntry : blockEntryList) {
            for (BlockInfo info : blockEntry.placedBlocks) {
                if (info.pos.equals(blockInfo.pos)) {
                    isPosExists = true;
                    break;
                }
            }
        }

        boolean isIdentifierExists = false;
        for (BlockEntry blockEntry : blockEntryList) {
            if (!blockEntry.isEntityMatched(id, entity.getUUID())) continue;
            if (isPosExists) {
                for (BlockInfo info : blockEntry.placedBlocks) {
                    if (info.pos.equals(blockInfo.pos)) continue;
                    info.blockState = blockInfo.blockState;
                    break;
                }
            } else blockEntry.placedBlocks.add(blockInfo);
            isIdentifierExists = true;
            break;
        }

        if (!isIdentifierExists)
            blockEntryList.add(new BlockEntry(id, entity.getUUID(), new ArrayList<>(Collections.singleton(blockInfo))));
        this.ChunkEntries.put(chunkPos, blockEntryList);
        printDebug();
    }

    public void remove(BlockPos pos) {
        List<BlockEntry> removedList = new ArrayList<>();
        ChunkPos chunkPos = level.getChunk(pos).getPos();
        List<BlockEntry> blockEntryList = ChunkEntries.get(chunkPos);
        if (blockEntryList == null) return;

        for (BlockEntry blockEntry : blockEntryList) {
            blockEntry.placedBlocks.removeIf(info -> info.pos.equals(pos));
            if (blockEntry.placedBlocks.isEmpty()) removedList.add(blockEntry);
        }

        if (!removedList.isEmpty())
            for (Map.Entry<ChunkPos, List<BlockEntry>> entry : ChunkEntries.entrySet()) {
                if (!entry.getKey().equals(chunkPos)) continue;
                entry.getValue().removeIf(removedList::contains);
            }

        if (blockEntryList.isEmpty())
            ChunkEntries.remove(chunkPos);
        printDebug();
    }

    public void printDebug() {
        if (!ConfigHandler.SERVER.INELIGIBLE_BLOCKS_LOGGER.get()) return;
        ChunkEntries.forEach((chunkPos, chunkEntries) -> LOGGER.debug("[Ineligible Blocks] {\"ChunkPos\": {}, \"Entries\": [{}]}", chunkPos, chunkEntries.parallelStream()
                .map(BlockEntry::toJSONFormat)
                .collect(Collectors.joining(", "))));
    }

    public boolean isBlockPlacedByEntity(BlockPos pos) {
        return !this.ChunkEntries.values().stream()
                .flatMap(List::stream)
                .map(BlockEntry::placedBlocks)
                .flatMap(List::stream)
                .filter(b -> b.pos.equals(pos))
                .toList()
                .isEmpty();
    }

    public Map<ChunkPos, List<BlockEntry>> getChunkEntries() {
        return ChunkEntries;
    }

    public static IneligibleBlocksSavedData getOrCreate(ServerLevel level) {
        return level.getDataStorage().computeIfAbsent(new Factory<>(() -> create(level), (NBT, provider) -> load(level, NBT), DataFixTypes.LEVEL), DATA_KEY);
    }

    private static IneligibleBlocksSavedData create(ServerLevel level) {
        return new IneligibleBlocksSavedData(level, new HashMap<>());
    }

    private static IneligibleBlocksSavedData load(ServerLevel level, CompoundTag NBT) {
        return new IneligibleBlocksSavedData(level, deserializeIneligibleBlocks(NBT));
    }

    @Override
    public @NotNull CompoundTag save(CompoundTag NBT, HolderLookup.Provider registries) {
        NBT.merge(serializeIneligibleBlocks());
        return NBT;
    }

    @Override
    public boolean isDirty() {
        return true;
    }

    private CompoundTag serializeIneligibleBlocks() {
        ListTag entriesTagList = new ListTag();
        for (Map.Entry<ChunkPos, List<BlockEntry>> entry : ChunkEntries.entrySet()) {
            ChunkPos chunkPos = entry.getKey();
            List<BlockEntry> chunkEntries = entry.getValue();

            CompoundTag entryTag = new CompoundTag();
            entryTag.put("Pos", writeChunkPos(chunkPos));
            entryTag.put("Data", chunkEntries.stream()
                    .map(BlockEntry::serialize)
                    .collect(ListTag::new, AbstractList::add, AbstractCollection::addAll));
            entriesTagList.add(entryTag);
        }

        CompoundTag finalTag = new CompoundTag();
        finalTag.put("ChunkEntries", entriesTagList);
        return finalTag;
    }

    private static Map<ChunkPos, List<BlockEntry>> deserializeIneligibleBlocks(CompoundTag tag) {
        Map<ChunkPos, List<BlockEntry>> finalMap = new HashMap<>();
        ListTag entriesListTag = tag.getList("ChunkEntries", 10);

        for (int i = 0; i < entriesListTag.size(); i++) {
            CompoundTag fetchedTag = entriesListTag.getCompound(i);
            ChunkPos chunkPos = readChunkPos(fetchedTag.getCompound("Pos"));
            List<BlockEntry> chunkEntries = fetchedTag.getList("Data", 10).stream()
                    .map(entryTag -> BlockEntry.deserialize((CompoundTag) entryTag))
                    .toList();
            finalMap.put(chunkPos, new ArrayList<>(chunkEntries));
        }
        return finalMap;
    }

    public void validateBlocks(ServerLevel level) {
        for (var entry : ChunkEntries.entrySet()) {
            for (BlockEntry blockEntry : entry.getValue()) {
                blockEntry.placedBlocks.removeIf(blockInfo -> level.getChunkSource().hasChunk(entry.getKey().x, entry.getKey().z) && !level.getBlockState(blockInfo.pos).is(blockInfo.blockState.getBlock()));
            }
        }
    }

    public static CompoundTag writeChunkPos(ChunkPos chunkPos) {
        CompoundTag NBT = new CompoundTag();
        NBT.putInt("X", chunkPos.x);
        NBT.putInt("Z", chunkPos.z);
        return NBT;
    }

    public static ChunkPos readChunkPos(CompoundTag tag) {
        return new ChunkPos(tag.getInt("X"), tag.getInt("Z"));
    }

    public record BlockEntry(ResourceLocation entityId, UUID entityUUID, List<BlockInfo> placedBlocks) {
        public CompoundTag serialize() {
            CompoundTag tag = new CompoundTag();
            ListTag blocksList = new ListTag();
            for (BlockInfo info : placedBlocks) {
                CompoundTag blockTag = new CompoundTag();
                blockTag.put("State", NbtUtils.writeBlockState(info.blockState));
                blockTag.put("Pos", NbtUtils.writeBlockPos(info.pos));
                blocksList.add(blockTag);
            }

            CompoundTag entity = new CompoundTag();
            entity.putString("Id", entityId.toString());
            entity.putUUID("UUID", entityUUID);

            tag.put("Entity", entity);
            tag.put("Blocks", blocksList);
            return tag;
        }

        public static BlockEntry deserialize(CompoundTag tag) {
            CompoundTag entity = tag.getCompound("Entity");
            ResourceLocation id = ResourceLocation.parse(entity.getString("Id"));
            UUID uuid = entity.getUUID("UUID");

            List<BlockInfo> blockInfoList = new ArrayList<>();
            ListTag blocksListTag = tag.getList("Blocks", 10);
            for (int i = 0; i < blocksListTag.size(); i++) {
                CompoundTag tag2 =  blocksListTag.getCompound(i);
                CompoundTag stateTag = tag2.getCompound("State");

                BlockState blockState = NbtUtils.readBlockState(BuiltInRegistries.BLOCK.asLookup(), stateTag);
                Optional<BlockPos> blockPos = NbtUtils.readBlockPos(tag2, "Pos");
                blockPos.ifPresent(pos -> blockInfoList.add(new BlockInfo(blockState, pos)));
            }

            return new BlockEntry(id, uuid, blockInfoList);
        }

        public boolean isEntityMatched(ResourceLocation id, UUID uuid) {
            return this.entityId.equals(id) && this.entityUUID.equals(uuid);
        }

        public String toJSONFormat() {
            return "{\"EntityId\": \"%s\", \"EntityUUID\": \"%s\", \"blocksCount\": %s, \"placedBlocks\": [%s]}"
                    .formatted(entityId, entityUUID, placedBlocks.size(), placedBlocks.parallelStream()
                            .map(BlockInfo::toString)
                            .collect(Collectors.joining(", ")));
        }
    }
    public static class BlockInfo {
        public BlockState blockState;
        public final BlockPos pos;

        public BlockInfo(BlockState state, BlockPos pos) {
            this.blockState = state;
            this.pos = pos;
        }

        @Override
        public String toString() {
            return "{\"State\": \"%s\", \"Pos\": \"%s\"}".formatted(NbtUtils.writeBlockState(blockState), NbtUtils.writeBlockPos(pos));
        }
    }
}
