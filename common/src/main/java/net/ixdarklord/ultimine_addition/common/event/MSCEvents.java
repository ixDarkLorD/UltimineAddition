package net.ixdarklord.ultimine_addition.common.event;

import net.ixdarklord.ultimine_addition.common.progression.TimedChallenge;
import net.ixdarklord.coolcatcore.api.event.v2.common.BlockEvents;
import net.ixdarklord.coolcatcore.api.event.v2.common.PlayerEvents;
import net.ixdarklord.coolcatcore.api.event.v2.common.ServerLifecycleEvents;
import net.ixdarklord.coolcatcore.api.event.v2.core.EventResult;
import net.ixdarklord.coolcatcore.api.event.v2.core.EventResultHolder;
import net.ixdarklord.coolcatcore.api.platform.Platform;
import net.ixdarklord.ultimine_addition.config.UAServerConfig;
import net.ixdarklord.ultimine_addition.common.progression.ChallengeBoosts;
import net.ixdarklord.ultimine_addition.common.data.record.SkillsRecordSync;
import com.mojang.datafixers.util.Pair;
import dev.ftb.mods.ftbultimine.FTBUltimine;
import net.ixdarklord.coolcatcore.api.utils.SlotReference;
import net.ixdarklord.ultimine_addition.common.data.challenge.ChallengeData;
import net.ixdarklord.ultimine_addition.common.data.item.MiningSkillCardData;
import net.ixdarklord.ultimine_addition.common.data.item.SkillsRecordData;
import net.ixdarklord.ultimine_addition.common.effect.MineGoJuiceEffect;
import net.ixdarklord.ultimine_addition.common.event.impl.BlockToolModificationEvent;
import net.ixdarklord.ultimine_addition.common.item.MiningSkillCardItem;
import net.ixdarklord.ultimine_addition.common.item.ModItems;
import net.ixdarklord.ultimine_addition.core.FTBUltimineAddition;
import net.ixdarklord.ultimine_addition.network.PayloadHandler;
import net.ixdarklord.ultimine_addition.network.payloads.PlayConsumeEffectPayload;
import net.ixdarklord.ultimine_addition.util.ItemUtils;
import net.ixdarklord.ultimine_addition.util.ToolAction;
import net.ixdarklord.ultimine_addition.util.ToolActions;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;

public class MSCEvents {
    public static void init() {
        PlayerEvents.END_TICK.register(instance -> {
            if (!(instance instanceof ServerPlayer player)) return;
            validateCards(player);
            cardBonusEffect(player);
            SkillsRecordSync.tick(player);
            // A few seconds after joining: the day's challenge, while it is still to do.
            if (player.tickCount == 100) TimedChallenge.announce(player, false);
        });
        PlayerEvents.LEAVE.register(SkillsRecordSync::forget);
        PlayerEvents.LEAVE.register(ChallengeBoosts::forget);
        ServerLifecycleEvents.STOPPED.register(server -> SkillsRecordSync.clear());

        BlockEvents.BREAK.register((level, pos, state, player) -> {
            if (player instanceof ServerPlayer serverPlayer) TimedChallenge.onAction(serverPlayer, state, pos, ChallengeData.Type.BREAK_BLOCK);
            List<SlotReference.Player> slots = ItemUtils.getSlotReferences(player, ModItems.SKILLS_RECORD, false);
            if (slots.isEmpty()) return EventResult.pass();
            for (SlotReference.Player slot : slots) {
                SkillsRecordData data = SkillsRecordData.get(slot.get(), player.level());
                Pair<Boolean, Boolean> taskProcess = data.initTaskValidator(state, pos, player, ChallengeData.Type.BREAK_BLOCK);
                if (taskProcess.getFirst()) {
                    data.save();
                }
                if (taskProcess.getSecond()) {
                    player.level().removeBlock(pos, false);
                    PayloadHandler.sendToTarget(new PlayConsumeEffectPayload(pos, state, player.getId()), player.serverLevel(), pos, 128);
                }
            }
            return EventResult.pass();
        });

        BlockToolModificationEvent.EVENT.register((originalState, context, toolAction, simulate) -> {
            if (context.getPlayer() instanceof ServerPlayer player && !FTBUltimine.getInstance().getOrCreatePlayerData(player).isPressed()) {
                return onBlockToolModificationEvent(originalState, context, toolAction, simulate);
            }
            return EventResultHolder.pass();
        });
    }

    private static void cardBonusEffect(ServerPlayer player) {
        if (!UAServerConfig.CARD_MASTERED_EFFECT.get()) return;
        List<SlotReference.Player> slots = ItemUtils.getSlotReferences(player, stack -> stack.is(ModItems.SKILLS_RECORD) || (stack.getItem() instanceof MiningSkillCardItem item && item.getType(stack) != MiningSkillCardItem.Type.EMPTY), false);
        List<MiningSkillCardData> dataList = slots.stream()
                .map(SlotReference.Player::get)
                .flatMap(itemStack -> {
                    Stream<ItemStack> stream = Stream.of(itemStack);
                    if (!itemStack.is(ModItems.SKILLS_RECORD)) return stream;

                    SkillsRecordData recordData = SkillsRecordData.get(itemStack, player.level());
                    List<ItemStack> list = recordData.getCardSlots().stream().filter(stack -> !stack.isEmpty()).toList();
                    return list.isEmpty() ? stream : list.stream();
                })
                .map(MiningSkillCardData::load)
                .filter(data -> data.getTier() == MiningSkillCardItem.Tier.Mastered)
                .filter(distinctByKey(data -> data.getType().getId() + ":" + data.getTier().name()))
                .toList();

        for (MiningSkillCardData data : dataList) {
            MineGoJuiceEffect.giveEffect(player, data.getType());
        }
    }

    public static <T> Predicate<T> distinctByKey(Function<? super T, ?> keyExtractor) {
        Set<Object> seen = new HashSet<>();
        return t -> seen.add(keyExtractor.apply(t));
    }

    private static void validateCards(ServerPlayer player) {
        if (player.tickCount % (20 * UAServerConfig.CARD_VALIDATOR.get()) != 0) return;
        List<SlotReference.Player> slots = ItemUtils.getSlotReferences(player, stack -> stack.is(ModItems.SKILLS_RECORD) || stack.getItem() instanceof MiningSkillCardItem, false);

        Function<ItemStack, Boolean> validateCardFunction = itemStack -> {
            if (itemStack.isEmpty() || !(itemStack.getItem() instanceof MiningSkillCardItem cardItem) || cardItem.getType(itemStack) == MiningSkillCardItem.Type.EMPTY)
                return false;

            boolean needSync = false;
            MiningSkillCardData oldCardData = MiningSkillCardData.load(itemStack);
            if (oldCardData.isCreativeItem()) {
                MiningSkillCardData newCardData = MiningSkillCardData.create(cardItem.getType(itemStack)).setStack(itemStack);
                newCardData.setTier(oldCardData.getTier()).initChallenges().save();
                FTBUltimineAddition.LOGGER.debug("[Data Tracker] Card UUID have been changed! {}", "[O: %s | N: %s]".formatted(oldCardData.getUUID(), newCardData.getUUID()));
                needSync = true;
            }
            if (MiningSkillCardData.load(itemStack).validateChallenges())
                needSync = true;
            return needSync;
        };

        for (SlotReference.Player slot : slots) {
            if (slot.get().is(ModItems.SKILLS_RECORD)) {
                SkillsRecordData recordData = SkillsRecordData.get(slot.get(), player.level());
                boolean needSync = false;
                for (ItemStack stack : recordData.getCardSlots()) {
                    if (validateCardFunction.apply(stack)) needSync = true;
                }
                if (needSync)
                    recordData.save();
            } else if (validateCardFunction.apply(slot.get())) {
                // Loose card: its challenges live in the storage, so save to persist and sync the change.
                MiningSkillCardData.load(slot.get()).save();
            }
        }
    }

    public static EventResultHolder<BlockState> onBlockToolModificationEvent(BlockState originalState, @NotNull UseOnContext context, ToolAction toolAction, boolean ignoredSimulate) {
        ServerPlayer player = (ServerPlayer) context.getPlayer();
        if (player == null) return EventResultHolder.pass();
        if (Platform.isFakePlayer(player)) return EventResultHolder.pass();
        if (!context.getLevel().isClientSide()) {
            ChallengeData.Type action = toolAction == ToolActions.AXE_STRIP ? ChallengeData.Type.STRIP_BLOCK
                    : toolAction == ToolActions.SHOVEL_FLATTEN ? ChallengeData.Type.FLATTEN_BLOCK
                    : toolAction == ToolActions.HOE_TILL && context.getLevel().getBlockState(context.getClickedPos().above()).isAir() ? ChallengeData.Type.TILLING_BLOCK : null;
            if (action != null && !ignoredSimulate) TimedChallenge.onAction(player, originalState, context.getClickedPos(), action);
            List<SlotReference.Player> slots = ItemUtils.getSlotReferences(player, ModItems.SKILLS_RECORD, false);
            if (slots.isEmpty()) return EventResultHolder.pass();

            for (SlotReference.Player slot : slots) {
                var data = SkillsRecordData.get(slot.get(), player.level());
                Pair<Boolean, Boolean> taskProcess = Pair.of(false, false);
                if (toolAction == ToolActions.AXE_STRIP) {
                    taskProcess = data.initTaskValidator(originalState, context.getClickedPos(), player, ChallengeData.Type.STRIP_BLOCK);

                } else if (toolAction == ToolActions.SHOVEL_FLATTEN) {
                    taskProcess = data.initTaskValidator(originalState, context.getClickedPos(), player, ChallengeData.Type.FLATTEN_BLOCK);

                } else if (toolAction == ToolActions.HOE_TILL && context.getLevel().getBlockState(context.getClickedPos().above()).isAir()) {
                    taskProcess = data.initTaskValidator(originalState, context.getClickedPos(), player, ChallengeData.Type.TILLING_BLOCK);
                }

                if (UAServerConfig.CHALLENGE_MANAGER_LOGGER.get()) {
                    FTBUltimineAddition.LOGGER.debug("[Challenge Tracker] Action: {}, Is Task Succeed: {}, Block: {}", toolAction.name(), taskProcess.getFirst(), originalState.getBlock().getName().getString());
                }

                if (taskProcess.getFirst()) {
                    data.save();

                    if (taskProcess.getSecond()) {
                        PayloadHandler.sendToTarget(new PlayConsumeEffectPayload(context.getClickedPos(), originalState, player.getId()), (ServerLevel)context.getLevel(), context.getClickedPos(), 128);
                        return EventResultHolder.interrupt(Blocks.AIR.defaultBlockState());
                    }
                }
            }
        }
        return EventResultHolder.pass();
    }
}
