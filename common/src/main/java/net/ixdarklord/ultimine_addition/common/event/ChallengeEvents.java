package net.ixdarklord.ultimine_addition.common.event;

import com.mojang.datafixers.util.Pair;
import dev.architectury.event.CompoundEventResult;
import dev.architectury.event.EventResult;
import dev.architectury.event.events.common.BlockEvent;
import dev.architectury.event.events.common.TickEvent;
import dev.architectury.hooks.level.entity.PlayerHooks;
import dev.architectury.registry.ReloadListenerRegistry;
import dev.ftb.mods.ftbultimine.FTBUltimine;
import net.ixdarklord.coolcat_lib.util.InventoryHelper;
import net.ixdarklord.ultimine_addition.common.config.ConfigHandler;
import net.ixdarklord.ultimine_addition.common.config.PlaystyleMode;
import net.ixdarklord.ultimine_addition.common.data.challenge.ChallengesData;
import net.ixdarklord.ultimine_addition.common.data.challenge.ChallengesManager;
import net.ixdarklord.ultimine_addition.common.data.chunk.ChunkManager;
import net.ixdarklord.ultimine_addition.common.data.item.MinerCertificateData;
import net.ixdarklord.ultimine_addition.common.data.item.MiningSkillCardData;
import net.ixdarklord.ultimine_addition.common.data.item.SkillsRecordData;
import net.ixdarklord.ultimine_addition.common.event.impl.BlockToolModificationEvent;
import net.ixdarklord.ultimine_addition.common.event.impl.DatapackEvents;
import net.ixdarklord.ultimine_addition.common.event.impl.ToolAction;
import net.ixdarklord.ultimine_addition.common.event.impl.ToolActions;
import net.ixdarklord.ultimine_addition.common.item.MiningSkillCardItem;
import net.ixdarklord.ultimine_addition.common.item.ModItems;
import net.ixdarklord.ultimine_addition.common.item.SkillsRecordItem;
import net.ixdarklord.ultimine_addition.common.network.PacketHandler;
import net.ixdarklord.ultimine_addition.common.network.packet.SyncChallengesPacket;
import net.ixdarklord.ultimine_addition.common.tag.ModBlockTags;
import net.ixdarklord.ultimine_addition.core.ServicePlatform;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.packs.PackType;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class ChallengeEvents {
    public static void init() {
        if (ConfigHandler.COMMON.PLAYSTYLE_MODE.get() == PlaystyleMode.LEGACY) {
            legacyFunctions();
            return;
        }

        ReloadListenerRegistry.register(PackType.SERVER_DATA, ChallengesManager.INSTANCE);
        DatapackEvents.TAG_UPDATE.register((registryAccess, updateCause, shouldUpdateStaticData) -> {
            if (updateCause == DatapackEvents.TagUpdate.Cause.SERVER_DATA_LOAD) {
                ChallengesManager.INSTANCE.validateAllChallenges();
            }
        });

        DatapackEvents.SYNC.register((player, isJoined) -> {
            if (!ChallengesManager.INSTANCE.getAllChallenges().isEmpty()) {
                PacketHandler.sendToPlayer(new SyncChallengesPacket(ChallengesManager.INSTANCE.getAllChallenges()), player);
            }
        });

        TickEvent.PLAYER_POST.register(instance -> {
            if (instance instanceof ServerPlayer player) {
                if (instance.tickCount % (20 * ConfigHandler.COMMON.CHALLENGE_VALIDATOR.get()) == 0) {
                    if (ServicePlatform.SlotAPI.isModLoaded()) {
                        ItemStack stack = ServicePlatform.SlotAPI.getSkillsRecordItem(player);
                        if (stack.getItem() instanceof SkillsRecordItem item) {
                            item.getData(stack).getCardSlots()
                                    .stream().filter(stack1 -> stack1.getItem() instanceof MiningSkillCardItem)
                                    .forEach(stack1 -> new MiningSkillCardData().loadData(stack1).sendToClient(player).validateChallenges().saveData(stack1));
                        }
                    }
                    player.getInventory().items.stream()
                            .filter(stack -> stack.hasTag() && (stack.getItem() instanceof MiningSkillCardItem || stack.getItem() instanceof SkillsRecordItem))
                            .forEach(stack -> {
                                if (stack.getItem() instanceof SkillsRecordItem item) {
                                    item.getData(stack).getCardSlots()
                                            .stream().filter(stack1 -> stack1.getItem() instanceof MiningSkillCardItem)
                                            .forEach(stack1 -> new MiningSkillCardData().loadData(stack1).sendToClient(player).validateChallenges().saveData(stack1));
                                } else {
                                    new MiningSkillCardData().loadData(stack).sendToClient(player).validateChallenges().saveData(stack);
                                }
                            });
                }
            }
        });

        BlockEvent.BREAK.register((level, pos, state, player, xp) -> {
            if (!level.isClientSide()) {
                List<ItemStack> items = listMatchingItem(player, ModItems.SKILLS_RECORD);
                if (items.isEmpty()) return EventResult.pass();
                for (ItemStack stack : items) {
                    SkillsRecordData data = new SkillsRecordData().loadData(stack);
                    Pair<Boolean, Boolean> taskProcess = data.initTaskValidator(state, pos, player, ChallengesData.Type.BREAK_BLOCK);
                    if (taskProcess.getFirst()) {
                        data.sendToClient(player).saveData(stack);
                    }
                }
            }
            return EventResult.pass();
        });

        BlockToolModificationEvent.EVENT.register((originalState, finalState, context, toolAction, simulate) -> {
            if (context.getPlayer() instanceof ServerPlayer player && !FTBUltimine.instance.get(player).isPressed()) {
                return ChallengeEvents.onBlockToolModificationEvent(originalState, finalState, context, toolAction, simulate);
            } return CompoundEventResult.pass();
        });
    }

    private static void legacyFunctions() {
        BlockEvent.BREAK.register((level, pos, state, player, xp) -> {
            List<ItemStack> stacks = InventoryHelper.listMatchingItem(player.getInventory(), ModItems.MINER_CERTIFICATE);
            boolean isGamemodeCorrect = !player.isCreative() && !player.isSpectator();
            if (stacks.isEmpty()
                    || isGamemodeCorrect && FTBUltimine.instance.canUltimine(player) && FTBUltimine.instance.get(player).isPressed()
                    || !(state.is(ModBlockTags.FORGE_ORES) || state.is(ModBlockTags.FABRIC_ORES))
                    || (isGamemodeCorrect && ChunkManager.INSTANCE.getChunkData(level.getChunk(pos)).isBlockPlacedByEntity(pos)))
                return EventResult.pass();

            for (ItemStack stack : stacks) {
                MinerCertificateData data = new MinerCertificateData().loadData(stack);
                MinerCertificateData.Legacy legacy = data.getLegacy();
                if (legacy != null) {
                    legacy.addMiningPoint(1);
                    data.sendToClient(player).saveData(stack);
                }
            }
            return EventResult.pass();
        });
    }

    @SuppressWarnings("unused")
    public static CompoundEventResult<BlockState> onBlockToolModificationEvent(BlockState originalState, BlockState finalState, @NotNull UseOnContext context, ToolAction toolAction, boolean simulate) {
        Player player = context.getPlayer();
        if (player == null) return CompoundEventResult.pass();
        if (PlayerHooks.isFake(player)) return CompoundEventResult.pass();
        if (!context.getLevel().isClientSide()) {
            List<ItemStack> items = listMatchingItem(player, ModItems.SKILLS_RECORD);
            if (items.isEmpty()) return CompoundEventResult.pass();
            for (ItemStack stack : items) {
                var data = new SkillsRecordData().loadData(stack);
                Pair<Boolean, Boolean> taskProcess = Pair.of(false, false);
                if (toolAction == ToolActions.AXE_STRIP) {
                    taskProcess = data.initTaskValidator(originalState, context.getClickedPos(), player, ChallengesData.Type.STRIP_BLOCK);

                } else if (toolAction == ToolActions.SHOVEL_FLATTEN) {
                    taskProcess = data.initTaskValidator(originalState, context.getClickedPos(), player, ChallengesData.Type.FLATTEN_BLOCK);

                } else if (toolAction == ToolActions.HOE_TILL && context.getLevel().getBlockState(context.getClickedPos().above()).isAir()) {
                    taskProcess = data.initTaskValidator(originalState, context.getClickedPos(), player, ChallengesData.Type.TILLING_BLOCK);
                }
//            Constants.LOGGER.debug("Is Task Succeed: {}, Block: {}", taskProcess.getFirst(), originalState.getBlock().getName().getString());

                if (taskProcess.getFirst()) {
                    data.sendToClient((ServerPlayer) player).saveData(stack);
                    if (taskProcess.getSecond()) {
                        return CompoundEventResult.interruptTrue(Blocks.AIR.defaultBlockState());
                    }
                }
            }
        }
        return CompoundEventResult.pass();
    }

    public static List<ItemStack> listMatchingItem(Player player, Item item) {
        List<ItemStack> result = new ArrayList<>();

        if (ServicePlatform.SlotAPI.isModLoaded()) {
            ItemStack stack = ServicePlatform.SlotAPI.getSkillsRecordItem(player);
            if (!stack.isEmpty() && stack.is(item)) result.add(stack);
        }

        Container inv = player.getInventory();
        for (int i = 0; i < inv.getContainerSize(); i++) {
            ItemStack stack = inv.getItem(i);
            if (!stack.isEmpty() && stack.is(item)) result.add(stack);
        }
        return result;
    }
}
