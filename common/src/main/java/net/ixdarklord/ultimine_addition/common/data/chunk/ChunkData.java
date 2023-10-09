package net.ixdarklord.ultimine_addition.common.data.chunk;

import dev.architectury.platform.Platform;
import dev.architectury.registry.registries.Registries;
import net.ixdarklord.ultimine_addition.common.config.ConfigHandler;
import net.ixdarklord.ultimine_addition.common.data.DataHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.state.BlockState;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

public class ChunkData extends DataHandler<ChunkData, CompoundTag> {
    private Map<EntityIdentifier, List<BlockInfo>> placedBlocks = new HashMap<>();

    public void addBlock(Entity entity, BlockInfo blockInfo) {
        ResourceLocation id = Registries.getId(entity.getType(), ResourceKey.createRegistryKey(new ResourceLocation("entity_type")));
        var list = placedBlocks.get(new EntityIdentifier(id, entity.getUUID()));
        if (list == null) list = new ArrayList<>();
        AtomicBoolean isPosExists = new AtomicBoolean();
        list.forEach(info -> { if (info.pos.equals(blockInfo.pos)) isPosExists.set(true); });
        if (isPosExists.get()) list = list.stream().map(e -> e.pos.equals(blockInfo.pos) ? blockInfo : e).collect(Collectors.toList()); else list.add(blockInfo);
        this.placedBlocks.put(new EntityIdentifier(id, entity.getUUID()), list);
        if (isDebug) printDebug();
    }

    public void removeBlock(BlockInfo blockInfo) {
        AtomicBoolean isEmpty = new AtomicBoolean();
        AtomicReference<EntityIdentifier> entityIdentifier = new AtomicReference<>();
        this.placedBlocks.forEach((identifier, list) -> {
            list.removeIf(info -> info.equals(blockInfo));
            if (list.isEmpty()) {
                isEmpty.set(true);
                entityIdentifier.set(identifier);
            }
        });
        if (isEmpty.get()) this.placedBlocks.remove(entityIdentifier.get());
        if (isDebug) printDebug();
    }

    public boolean isBlockPlacedByEntity(BlockPos pos) {
        return !this.placedBlocks.values().stream().flatMap(List::stream).filter(b -> b.pos.equals(pos)).toList().isEmpty();
    }

    public Map<EntityIdentifier, List<BlockInfo>> getPlacedBlocks() {
        return placedBlocks;
    }

    @Override
    public void saveData(CompoundTag data) {
        CompoundTag NBT = (CompoundTag) data.get(this.NBTBase);
        if (NBT == null) NBT = new CompoundTag();

        NBT.merge(getNBTFromPlacedBlocksList(placedBlocks));
        data.put(this.NBTBase, NBT);
    }

    @Override
    public ChunkData loadData(CompoundTag data) {
        CompoundTag NBT = (CompoundTag) data.get(this.NBTBase);
        if (NBT == null) NBT = new CompoundTag();

        this.placedBlocks = getPlacedBlocksFromNBT(NBT);
        return this;
    }

    @Override
    public void toNetwork(FriendlyByteBuf buf) {}

    @Override
    public void printDebug() {
        if (!ConfigHandler.COMMON.CHUNK_DATA_LOGGER.get() && !Platform.isDevelopmentEnvironment()) return;
        placedBlocks.forEach((identifier, list) -> LOGGER.info("{} | {}", identifier, list.parallelStream().collect(StringBuilder::new,
                (s, blockInfo) -> s.append("{").append(blockInfo.blockState).append(", ").append(blockInfo.pos).append("}"),
                (a, b) -> a.append(", ").append(b))));
    }

    private CompoundTag getNBTFromPlacedBlocksList(Map<EntityIdentifier, List<BlockInfo>> value) {
        ListTag listTag = new ListTag();
        value.forEach((identifier, blockInfoList) -> {
            CompoundTag tag = new CompoundTag();

            ListTag listTag2 = new ListTag();
            blockInfoList.forEach(info -> {
                CompoundTag tag2 = new CompoundTag();
                tag2.put("State", NbtUtils.writeBlockState(info.blockState));
                tag2.put("Pos", NbtUtils.writeBlockPos(info.pos));
                listTag2.add(tag2);
            });

            CompoundTag entity = new CompoundTag();
            entity.putString("Id", identifier.id.toString());
            entity.putUUID("UUID", identifier.uuid);
            tag.put("Entity", entity);
            tag.put("Blocks", listTag2);
            listTag.add(tag);
        });
        var tag = new CompoundTag();
        tag.put("PlacedBlocks", listTag);
        return tag;
    }

    private Map<EntityIdentifier, List<BlockInfo>> getPlacedBlocksFromNBT(CompoundTag NBT) {
        Map<EntityIdentifier, List<BlockInfo>> result = new HashMap<>();
        ListTag listTag = new ListTag();
        if (NBT != null) listTag = NBT.getList("PlacedBlocks", 10);

        for (int i = 0; i < listTag.size(); ++i) {
            CompoundTag tag = listTag.getCompound(i);
            CompoundTag entity = tag.getCompound("Entity");
            ResourceLocation id = new ResourceLocation(entity.getString("Id"));
            UUID uuid = entity.getUUID("UUID");

            List<BlockInfo> blockInfoList = new ArrayList<>();
            ListTag listTag2 = tag.getList("Blocks", 10);
            for (int j = 0; j < listTag2.size(); j++) {
                CompoundTag tag2 =  listTag2.getCompound(i);
                BlockState blockState = NbtUtils.readBlockState((CompoundTag) Objects.requireNonNull(tag2.get("State")));
                BlockPos pos = NbtUtils.readBlockPos((CompoundTag) Objects.requireNonNull(tag2.get("Pos")));
                blockInfoList.add(new BlockInfo(blockState, pos));
            }

            if (!listTag2.isEmpty()) result.put(new EntityIdentifier(id, uuid), blockInfoList);
        }
        return result;
    }

    public record BlockInfo(BlockState blockState, BlockPos pos) {}
    public record EntityIdentifier(ResourceLocation id, UUID uuid) {
        @Override
        public String toString() {
            return String.format("Entity: Id:{%s}, UUID:{%s}", id.toString(), uuid.toString());
        }
    }
}
