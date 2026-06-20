package net.ixdarklord.ultimine_addition.common.event;

import com.mojang.datafixers.util.Pair;
import dev.architectury.event.CompoundEventResult;
import dev.architectury.event.EventResult;
import dev.architectury.event.events.common.BlockEvent;
import dev.architectury.event.events.common.TickEvent;
import dev.architectury.hooks.level.entity.PlayerHooks;
import dev.ftb.mods.ftbultimine.FTBUltimine;
import net.ixdarklord.coolcatlib.api.utils.SlotReference;
import net.ixdarklord.ultimine_addition.common.data.challenge.ChallengeData;
import net.ixdarklord.ultimine_addition.common.data.item.MiningSkillCardData;
import net.ixdarklord.ultimine_addition.common.data.item.SkillsRecordData;
import net.ixdarklord.ultimine_addition.common.effect.MineGoJuiceEffect;
import net.ixdarklord.ultimine_addition.common.event.impl.BlockToolModificationEvent;
import net.ixdarklord.ultimine_addition.common.item.MiningSkillCardItem;
import net.ixdarklord.ultimine_addition.common.item.ModItems;
import net.ixdarklord.ultimine_addition.config.ConfigHandler;
import net.ixdarklord.ultimine_addition.core.FTBUltimineAddition;
import net.ixdarklord.ultimine_addition.network.PayloadHandler;
import net.ixdarklord.ultimine_addition.network.payloads.PlayConsumeEffectPayload;
import net.ixdarklord.ultimine_addition.network.payloads.SkillsRecordPayload;
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
        TickEvent.PLAYER_POST.register(instance -> {
            if (!(instance instanceof ServerPlayer player)) return;
            validateCards(player);
            cardBonusEffect(player);
        });

        BlockEvent.BREAK.register((level, pos, state, player, xp) -> {
            List<SlotReference.Player> slots = ItemUtils.getSlotReferences(player, ModItems.SKILLS_RECORD, false);
            if (slots.isEmpty()) return EventResult.pass();
            for (SlotReference.Player slot : slots) {
                SkillsRecordData data = SkillsRecordData.load(slot.get());
                Pair<Boolean, Boolean> taskProcess = data.initTaskValidator(state, pos, player, ChallengeData.Type.BREAK_BLOCK);
                if (taskProcess.getFirst()) {
                    data.sendToClient(player, slot.getIndex()).save();
                }
                if (taskProcess.getSecond()) {
                    player.level().removeBlock(pos, false);
                    PayloadHandler.sendToTarget(new PlayConsumeEffectPayload(pos, state), player.serverLevel(), pos, 128);
                }
            }
            return EventResult.pass();
        });

        BlockToolModificationEvent.EVENT.register((originalState, context, toolAction, simulate) -> {
            if (context.getPlayer() instanceof ServerPlayer player && !FTBUltimine.getInstance().getOrCreatePlayerData(player).isPressed()) {
                return onBlockToolModificationEvent(originalState, context, toolAction, simulate);
            }
            return CompoundEventResult.pass();
        });
    }

    private static void cardBonusEffect(ServerPlayer player) {
        if (!ConfigHandler.SERVER.CARD_MASTERED_EFFECT.get()) return;
        List<SlotReference.Player> slots = ItemUtils.getSlotReferences(player, stack -> stack.is(ModItems.SKILLS_RECORD) || (stack.getItem() instanceof MiningSkillCardItem item && item.getType() != MiningSkillCardItem.Type.EMPTY), false);
        List<MiningSkillCardData> dataList = slots.stream()
                .map(SlotReference.Player::get)
                .flatMap(itemStack -> {
                    Stream<ItemStack> stream = Stream.of(itemStack);
                    if (!itemStack.is(ModItems.SKILLS_RECORD)) return stream;

                    SkillsRecordData recordData = SkillsRecordData.load(itemStack);
                    List<ItemStack> list = recordData.getCardSlots().stream().filter(stack -> !stack.isEmpty()).toList();
                    return list.isEmpty() ? stream : list.stream();
                })
                .map(MiningSkillCardData::load)
                .filter(data -> data.getTier() == MiningSkillCardItem.Tier.Mastered)
                .filter(distinctByKey(data -> BuiltInRegistries.ITEM.getKey(data.getStack().getItem()) + ":" + data.getTier().name()))
                .toList();

        for (MiningSkillCardData data : dataList) {
            MiningSkillCardItem item = (MiningSkillCardItem) data.getStack().getItem();
            MineGoJuiceEffect.giveEffect(player, item.getType());
        }
    }

    public static <T> Predicate<T> distinctByKey(Function<? super T, ?> keyExtractor) {
        Set<Object> seen = new HashSet<>();
        return t -> seen.add(keyExtractor.apply(t));
    }

    private static void validateCards(ServerPlayer player) {
        if (!ConfigHandler.SERVER.SPEC.isLoaded() || player.tickCount % (20 * ConfigHandler.SERVER.CARD_VALIDATOR.get()) != 0) return;
        List<SlotReference.Player> slots = ItemUtils.getSlotReferences(player, stack -> stack.is(ModItems.SKILLS_RECORD) || stack.getItem() instanceof MiningSkillCardItem, false);

        Function<ItemStack, Boolean> validateCardFunction = itemStack -> {
            if (itemStack.isEmpty() || !(itemStack.getItem() instanceof MiningSkillCardItem cardItem) || cardItem.getType() == MiningSkillCardItem.Type.EMPTY)
                return false;

            boolean needSync = false;
            MiningSkillCardData oldCardData = MiningSkillCardData.load(itemStack);
            if (oldCardData.isCreativeItem()) {
                MiningSkillCardData newCardData = MiningSkillCardData.create(cardItem.getType()).setStack(itemStack);
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
                SkillsRecordData recordData = SkillsRecordData.load(slot.get());
                boolean needSync = false;
                for (ItemStack stack : recordData.getCardSlots()) {
                    if (validateCardFunction.apply(stack)) needSync = true;
                }
                if (needSync)
                    recordData.sendToClient(player, slot.getIndex()).save();
            } else {
                validateCardFunction.apply(slot.get());
            }
        }
    }

    public static CompoundEventResult<BlockState> onBlockToolModificationEvent(BlockState originalState, @NotNull UseOnContext context, ToolAction toolAction, boolean ignoredSimulate) {
        ServerPlayer player = (ServerPlayer) context.getPlayer();
        if (player == null) return CompoundEventResult.pass();
        if (PlayerHooks.isFake(player)) return CompoundEventResult.pass();
        if (!context.getLevel().isClientSide()) {
            List<SlotReference.Player> slots = ItemUtils.getSlotReferences(player, ModItems.SKILLS_RECORD, false);
            if (slots.isEmpty()) return CompoundEventResult.pass();

            for (SlotReference.Player slot : slots) {
                var data = SkillsRecordData.load(slot.get());
                Pair<Boolean, Boolean> taskProcess = Pair.of(false, false);
                if (toolAction == ToolActions.AXE_STRIP) {
                    taskProcess = data.initTaskValidator(originalState, context.getClickedPos(), player, ChallengeData.Type.STRIP_BLOCK);

                } else if (toolAction == ToolActions.SHOVEL_FLATTEN) {
                    taskProcess = data.initTaskValidator(originalState, context.getClickedPos(), player, ChallengeData.Type.FLATTEN_BLOCK);

                } else if (toolAction == ToolActions.HOE_TILL && context.getLevel().getBlockState(context.getClickedPos().above()).isAir()) {
                    taskProcess = data.initTaskValidator(originalState, context.getClickedPos(), player, ChallengeData.Type.TILLING_BLOCK);
                }

                if (ConfigHandler.SERVER.CHALLENGE_MANAGER_LOGGER.get()) {
                    FTBUltimineAddition.LOGGER.debug("[Challenge Tracker] Action: {}, Is Task Succeed: {}, Block: {}", toolAction.name(), taskProcess.getFirst(), originalState.getBlock().getName().getString());
                }

                if (taskProcess.getFirst()) {
                    data.save();
                    PayloadHandler.sendToPlayer(new SkillsRecordPayload.SyncData(slot.getIndex(), data), player);

                    if (taskProcess.getSecond()) {
                        PayloadHandler.sendToTarget(new PlayConsumeEffectPayload(context.getClickedPos(), originalState), (ServerLevel)context.getLevel(), context.getClickedPos(), 128);
                        return CompoundEventResult.interruptTrue(Blocks.AIR.defaultBlockState());
                    }
                }
            }
        }
        return CompoundEventResult.pass();
    }
}
