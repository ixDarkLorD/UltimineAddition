package net.ixdarklord.ultimine_addition.common.event;

import com.mojang.datafixers.util.Pair;
import dev.architectury.event.CompoundEventResult;
import dev.architectury.event.EventResult;
import dev.architectury.event.events.common.BlockEvent;
import dev.architectury.event.events.common.TickEvent;
import dev.architectury.hooks.level.entity.PlayerHooks;
import dev.ftb.mods.ftbultimine.FTBUltimine;
import net.ixdarklord.coolcatlib.api.util.SlotReference;
import net.ixdarklord.ultimine_addition.common.data.challenge.ChallengesData;
import net.ixdarklord.ultimine_addition.common.data.item.MiningSkillCardData;
import net.ixdarklord.ultimine_addition.common.data.item.SkillsRecordData;
import net.ixdarklord.ultimine_addition.common.effect.MineGoJuiceEffect;
import net.ixdarklord.ultimine_addition.common.event.impl.BlockToolModificationEvent;
import net.ixdarklord.ultimine_addition.common.item.MiningSkillCardItem;
import net.ixdarklord.ultimine_addition.common.item.ModItems;
import net.ixdarklord.ultimine_addition.config.ConfigHandler;
import net.ixdarklord.ultimine_addition.core.FTBUltimineAddition;
import net.ixdarklord.ultimine_addition.util.ItemUtils;
import net.ixdarklord.ultimine_addition.util.ToolAction;
import net.ixdarklord.ultimine_addition.util.ToolActions;
import net.minecraft.core.registries.BuiltInRegistries;
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

        BlockEvent.BREAK.register((level, pos, state, pl, xp) -> {
            if (pl instanceof ServerPlayer player) {
                List<SlotReference.Player> slots = ItemUtils.getSlotReferences(player, ModItems.SKILLS_RECORD);
                if (slots.isEmpty()) return EventResult.pass();
                for (SlotReference.Player slot : slots) {
                    SkillsRecordData data = SkillsRecordData.loadData(slot.getItem());
                    Pair<Boolean, Boolean> taskProcess = data.initTaskValidator(state, pos, player, ChallengesData.Type.BREAK_BLOCK);
                    if (taskProcess.getFirst()) {
                        data.sendToClient(player, slot.getIndex()).saveData(slot.getItem());
                    }
                }
            }
            return EventResult.pass();
        });

        BlockToolModificationEvent.EVENT.register((originalState, finalState, context, toolAction, simulate) -> {
            if (context.getPlayer() instanceof ServerPlayer player && !FTBUltimine.instance.getOrCreatePlayerData(player).isPressed()) {
                return onBlockToolModificationEvent(originalState, finalState, context, toolAction, simulate);
            }
            return CompoundEventResult.pass();
        });
    }

    private static void cardBonusEffect(ServerPlayer player) {
        if (!ConfigHandler.SERVER.CARD_MASTERED_EFFECT.get()) return;
        List<SlotReference.Player> slots = ItemUtils.getSlotReferences(player, stack -> stack.is(ModItems.SKILLS_RECORD) || (stack.getItem() instanceof MiningSkillCardItem item && item.getType() != MiningSkillCardItem.Type.EMPTY));
        List<MiningSkillCardData> dataList = slots.stream()
                .map(SlotReference.Player::getItem)
                .flatMap(itemStack -> {
                    Stream<ItemStack> stream = Stream.of(itemStack);
                    if (!itemStack.is(ModItems.SKILLS_RECORD)) return stream;

                    SkillsRecordData recordData = SkillsRecordData.loadData(itemStack);
                    List<ItemStack> list = recordData.getCardSlots().stream().filter(stack -> !stack.isEmpty()).toList();
                    return list.isEmpty() ? stream : list.stream();
                })
                .map(MiningSkillCardData::loadData)
                .filter(data -> data.getTier() == MiningSkillCardItem.Tier.Mastered)
                .filter(distinctByKey(data -> BuiltInRegistries.ITEM.getKey(data.get().getItem()) + ":" + data.getTier().name()))
                .toList();

        for (MiningSkillCardData data : dataList) {
            MiningSkillCardItem item = (MiningSkillCardItem) data.get().getItem();
            MineGoJuiceEffect.giveEffect(player, item.getType());
        }
    }

    public static <T> Predicate<T> distinctByKey(Function<? super T, ?> keyExtractor) {
        Set<Object> seen = new HashSet<>();
        return t -> seen.add(keyExtractor.apply(t));
    }

    private static void validateCards(ServerPlayer player) {
        if (!ConfigHandler.SERVER.SPEC.isLoaded() || player.tickCount % (20 * ConfigHandler.SERVER.CARD_VALIDATOR.get()) != 0) return;
        List<SlotReference.Player> slots = ItemUtils.getSlotReferences(player, stack -> stack.is(ModItems.SKILLS_RECORD) || stack.getItem() instanceof MiningSkillCardItem);

        Function<ItemStack, Boolean> validateCardFunction = itemStack -> {
            if (itemStack.isEmpty() || !(itemStack.getItem() instanceof MiningSkillCardItem cardItem))
                return false;

            boolean needSync = false;
            MiningSkillCardData oldCardData = MiningSkillCardData.loadData(itemStack);
            if (oldCardData.isCreativeItem()) {
                MiningSkillCardData newCardData = MiningSkillCardData.create(cardItem.getType()).setDataHolder(itemStack);
                newCardData.setTier(oldCardData.getTier()).initChallenges().saveData(itemStack);
                FTBUltimineAddition.LOGGER.debug("[Data Tracker] Card UUID have been changed! {}", "[O: %s | N: %s]".formatted(oldCardData.getUUID(), newCardData.getUUID()));
                needSync = true;
            }
            if (MiningSkillCardData.loadData(itemStack).validateChallenges())
                needSync = true;
            return needSync;
        };

        for (SlotReference.Player slot : slots) {
            if (slot.getItem().is(ModItems.SKILLS_RECORD)) {
                SkillsRecordData recordData = SkillsRecordData.loadData(slot.getItem());
                boolean needSync = false;
                for (ItemStack stack : recordData.getCardSlots()) {
                    if (validateCardFunction.apply(stack)) needSync = true;
                }
                if (needSync)
                    recordData.sendToClient(player, slot.getIndex()).saveData(slot.getItem());
            } else {
                validateCardFunction.apply(slot.getItem());
            }
        }
    }

    public static CompoundEventResult<BlockState> onBlockToolModificationEvent(BlockState originalState, BlockState finalState, @NotNull UseOnContext context, ToolAction toolAction, boolean simulate) {
        ServerPlayer player = (ServerPlayer) context.getPlayer();
        if (player == null) return CompoundEventResult.pass();
        if (PlayerHooks.isFake(player)) return CompoundEventResult.pass();
        if (!context.getLevel().isClientSide()) {
            List<SlotReference.Player> slots = ItemUtils.getSlotReferences(player, ModItems.SKILLS_RECORD);
            if (slots.isEmpty()) return CompoundEventResult.pass();
            for (SlotReference.Player slot : slots) {
                var data = SkillsRecordData.loadData(slot.getItem());
                Pair<Boolean, Boolean> taskProcess = Pair.of(false, false);
                if (toolAction == ToolActions.AXE_STRIP) {
                    taskProcess = data.initTaskValidator(originalState, context.getClickedPos(), player, ChallengesData.Type.STRIP_BLOCK);

                } else if (toolAction == ToolActions.SHOVEL_FLATTEN) {
                    taskProcess = data.initTaskValidator(originalState, context.getClickedPos(), player, ChallengesData.Type.FLATTEN_BLOCK);

                } else if (toolAction == ToolActions.HOE_TILL && context.getLevel().getBlockState(context.getClickedPos().above()).isAir()) {
                    taskProcess = data.initTaskValidator(originalState, context.getClickedPos(), player, ChallengesData.Type.TILLING_BLOCK);
                }

                if (ConfigHandler.SERVER.CHALLENGE_MANAGER_LOGGER.get()) {
                    FTBUltimineAddition.LOGGER.debug("[Challenge Tracker] Action: {}, Is Task Succeed: {}, Block: {}", toolAction.name(), taskProcess.getFirst(), originalState.getBlock().getName().getString());
                }

                if (taskProcess.getFirst()) {
                    data.saveData(slot.getItem());
                    if (taskProcess.getSecond()) {
                        return CompoundEventResult.interruptTrue(Blocks.AIR.defaultBlockState());
                    }
                }
            }
        }
        return CompoundEventResult.pass();
    }
}
