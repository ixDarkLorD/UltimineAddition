package net.ixdarklord.ultimine_addition.common.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.datafixers.util.Pair;
import dev.architectury.platform.Platform;
import net.ixdarklord.ultimine_addition.common.command.arguments.CardSlotsArgument;
import net.ixdarklord.ultimine_addition.common.command.arguments.CardTierArgument;
import net.ixdarklord.ultimine_addition.common.command.arguments.ChallengesArgument;
import net.ixdarklord.ultimine_addition.common.data.challenge.ChallengesData;
import net.ixdarklord.ultimine_addition.common.data.item.MiningSkillCardData;
import net.ixdarklord.ultimine_addition.common.data.item.SkillsRecordData;
import net.ixdarklord.ultimine_addition.common.item.MiningSkillCardItem;
import net.ixdarklord.ultimine_addition.common.item.SkillsRecordItem;
import net.ixdarklord.ultimine_addition.core.ServicePlatform;
import net.ixdarklord.ultimine_addition.core.UltimineAddition;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Objects;
import java.util.Optional;

public class CardsCommand {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher, CommandBuildContext ignored, Commands.CommandSelection ignored2) {
        var command = UltimineAddition.getCommandPrompt(Commands.LEVEL_GAMEMASTERS)
                .then(Commands.literal("mining_skill_card")
                        .then(Commands.literal("challenges")
                                .then(Commands.argument("targets", EntityArgument.players())
                                        .then(Commands.argument("challenge_id", ChallengesArgument.data())
                                                .then(Commands.literal("in_hand")
                                                        .then(Commands.literal("set_point").then(Commands.argument("amount", IntegerArgumentType.integer(0)).executes(context -> updateChallengeValue(context.getSource(), EntityArgument.getPlayers(context, "targets"), false, ChallengesArgument.getData(context, "challenge_id"), -1, IntegerArgumentType.getInteger(context, "amount"), true))))
                                                        .then(Commands.literal("add_point").then(Commands.argument("amount", IntegerArgumentType.integer(0)).executes(context -> updateChallengeValue(context.getSource(), EntityArgument.getPlayers(context, "targets"), false, ChallengesArgument.getData(context, "challenge_id"), -1, IntegerArgumentType.getInteger(context, "amount"), false))))
                                                        .then(Commands.literal("accomplish").executes(context -> updateChallengeValue(context.getSource(), EntityArgument.getPlayers(context, "targets"), false, ChallengesArgument.getData(context, "challenge_id"), -1, -1, null)))
                                                )
                                                .then(Commands.literal("in_skills_record").then(Commands.argument("in_holder_slot", BoolArgumentType.bool()).then(Commands.argument("card_slot", CardSlotsArgument.slots())
                                                        .then(Commands.literal("set_point").then(Commands.argument("amount", IntegerArgumentType.integer(0)).executes(context -> updateChallengeValue(context.getSource(), EntityArgument.getPlayers(context, "targets"), BoolArgumentType.getBool(context, "in_holder_slot"), ChallengesArgument.getData(context, "challenge_id"), CardSlotsArgument.getSlot(context, "card_slot"), IntegerArgumentType.getInteger(context, "amount"), true))))
                                                        .then(Commands.literal("add_point").then(Commands.argument("amount", IntegerArgumentType.integer(0)).executes(context -> updateChallengeValue(context.getSource(), EntityArgument.getPlayers(context, "targets"), BoolArgumentType.getBool(context, "in_holder_slot"), ChallengesArgument.getData(context, "challenge_id"), CardSlotsArgument.getSlot(context, "card_slot"), IntegerArgumentType.getInteger(context, "amount"), false))))
                                                        .then(Commands.literal("accomplish").executes(context -> updateChallengeValue(context.getSource(), EntityArgument.getPlayers(context, "targets"), BoolArgumentType.getBool(context, "in_holder_slot"), ChallengesArgument.getData(context, "challenge_id"), CardSlotsArgument.getSlot(context, "card_slot"), -1, null)))))
                                                )
                                        )))
                        .then(Commands.literal("change_tier")
                                .then(Commands.argument("targets", EntityArgument.players())
                                        .then(Commands.literal("in_hand")
                                                .then(Commands.argument("tier", CardTierArgument.tier()).executes(context -> setTier(context.getSource(), EntityArgument.getPlayers(context, "targets"), CardTierArgument.getTier(context, "tier"), false, -1)))
                                        )
                                        .then(Commands.literal("in_skills_record").then(Commands.argument("in_holder_slot", BoolArgumentType.bool()).then(Commands.argument("card_slot", CardSlotsArgument.slots())
                                                .then(Commands.argument("tier", CardTierArgument.tier()).executes(context -> setTier(context.getSource(), EntityArgument.getPlayers(context, "targets"), CardTierArgument.getTier(context, "tier"), BoolArgumentType.getBool(context, "in_holder_slot"), CardSlotsArgument.getSlot(context, "card_slot"))
                                                ))))))));
        dispatcher.register(command);
    }

    private static int updateChallengeValue(CommandSourceStack source, @NotNull Collection<ServerPlayer> targets, boolean inHolderSlot, Pair<ResourceLocation, ChallengesData> cData, int slot, int amount, Boolean replaceValue) {
        int i = 0;
        try {
            for (ServerPlayer player : targets) {
                Pair<Optional<MiningSkillCardData>, Optional<SkillsRecordData>> cardOrHolder = getCardOrRecord(player, inHolderSlot, slot);
                if (cardOrHolder.getFirst().isEmpty()) {
                    source.sendFailure(Component.translatable("command.ultimine_addition.cards.not_found").withStyle(ChatFormatting.RED));
                    return i;
                }

                MiningSkillCardData cardData = cardOrHolder.getFirst().get();
                Optional<MiningSkillCardData.ChallengeHolder> challengeHolder = cardData.getChallenge(cData.getFirst());
                if (challengeHolder.isPresent()) {
                    int oldValue = challengeHolder.get().getCurrentPoints();
                    int newValue;

                    if (amount > -1) {
                        if (replaceValue) {
                            newValue = amount;
                            cardData.setAmount(cData.getFirst(), amount).sendToClient(player).saveData(cardData.get());
                            cardOrHolder.getSecond().ifPresent(data -> data.sendToClient(player).saveData(data.get()));

                        } else if (!cardData.isChallengeAccomplished(cData.getFirst())) {
                            newValue = oldValue + amount;
                            cardData.addAmount(cData.getFirst(), amount).sendToClient(player).saveData(cardData.get());
                            cardOrHolder.getSecond().ifPresent(data -> data.sendToClient(player).saveData(data.get()));

                        } else {
                            source.sendFailure(Component.translatable("command.ultimine_addition.challenge.accomplished", cData.getFirst().toString()).withStyle(ChatFormatting.RED));
                            i++;
                            return i;
                        }
                    } else {
                        if (!cardData.isChallengeAccomplished(cData.getFirst())) {
                            newValue = challengeHolder.get().getRequiredPoints();
                            cardData.accomplishChallenge(cData.getFirst()).sendToClient(player).saveData(cardData.get());
                            cardOrHolder.getSecond().ifPresent(data -> data.sendToClient(player).saveData(data.get()));
                        } else {
                            source.sendFailure(Component.translatable("command.ultimine_addition.challenge.accomplished", cData.getFirst().toString()).withStyle(ChatFormatting.RED));
                            i++;
                            return i;
                        }
                    }
                    i++;

                    if (player == source.getPlayer()) {
                        source.sendSuccess(() -> Component.translatable("command.ultimine_addition.challenge.success", cData.getFirst().toString(), oldValue, newValue).withStyle(ChatFormatting.DARK_AQUA), true);
                    }
                    if (i > 1 && player != source.getPlayer() && !player.hasPermissions(2)) {
                        player.displayClientMessage(Component.translatable("command.ultimine_addition.challenge.receiver", cData.getFirst().toString(), oldValue, newValue, Objects.requireNonNull(source.getPlayer()).getName().getString()).withStyle(ChatFormatting.GRAY), false);
                    }
                    if (i > 1) {
                        source.sendSuccess(() -> Component.translatable("command.ultimine_addition.challenge.sender", cData.getFirst().toString(), oldValue, newValue).withStyle(ChatFormatting.GRAY), true);
                        int x = 1;
                        for (ServerPlayer p : targets) {
                            if (p != source.getPlayer()) {
                                int finalX = x;
                                source.sendSuccess(() -> Component.literal(finalX + ": " + p.getName().getString()).withStyle(ChatFormatting.YELLOW), true);
                                x++;
                            }
                        }
                    }
                } else if (player == source.getPlayer()) {
                    source.sendFailure(Component.translatable("command.ultimine_addition.challenge.not_found", cData.getFirst().toString()));
                    return i;
                }
            }
        } catch (Exception err) {
            if (Platform.isDevelopmentEnvironment())
                UltimineAddition.LOGGER.error("Error occurred while executing the command!", err);
        }
        return i;
    }

    private static int setTier(CommandSourceStack source, @NotNull Collection<ServerPlayer> targets, MiningSkillCardItem.Tier tier, boolean inHolderSlot, int slot) {
        int i = 0;
        try {
            for (ServerPlayer player : targets) {
                Pair<Optional<MiningSkillCardData>, Optional<SkillsRecordData>> cardOrHolder = getCardOrRecord(player, inHolderSlot, slot);
                if (cardOrHolder.getFirst().isEmpty()) {
                    source.sendFailure(Component.translatable("command.ultimine_addition.cards.not_found").withStyle(ChatFormatting.RED));
                    return i;
                }

                MiningSkillCardData cardData = cardOrHolder.getFirst().get();
                if (cardData.getTier() != tier) {
                    cardData.setTier(tier).initChallenges().sendToClient(player).saveData(cardData.get());
                    cardOrHolder.getSecond().ifPresent(data -> data.sendToClient(player).saveData(data.get()));
                    i++;

                    if (player == source.getPlayer()) {
                        source.sendSuccess(() -> Component.translatable("command.ultimine_addition.cards.tier.set.success", cardData.get().getHoverName(), tier.name()).withStyle(ChatFormatting.DARK_AQUA), true);
                    }
                    if (i > 1 && player != source.getPlayer() && !player.hasPermissions(2)) {
                        player.displayClientMessage(Component.translatable("command.ultimine_addition.cards.tier.set.receiver", cardData.get().getHoverName(), tier.name(), Objects.requireNonNull(source.getPlayer()).getName().getString()).withStyle(ChatFormatting.GRAY), false);
                    }
                    if (i > 1) {
                        source.sendSuccess(() -> Component.translatable("command.ultimine_addition.cards.tier.set.sender", cardData.get().getHoverName(), tier.name()).withStyle(ChatFormatting.GRAY), true);
                        int x = 1;
                        for (ServerPlayer p : targets) {
                            if (p != source.getPlayer()) {
                                int finalX = x;
                                source.sendSuccess(() -> Component.literal(finalX + ": " + p.getName().getString()).withStyle(ChatFormatting.YELLOW), true);
                                x++;
                            }
                        }
                    }
                } else if (cardData.getTier() == tier) {
                    source.sendFailure(Component.translatable("command.ultimine_addition.cards.tier.set.already_setted", cardData.get().getHoverName().getString().toLowerCase(), tier.name()).withStyle(ChatFormatting.RED));
                    i++;
                }
            }
        } catch (Exception err) {
            if (Platform.isDevelopmentEnvironment())
                UltimineAddition.LOGGER.error("Error occurred!", err);
        }
        return i;
    }

    private static Pair<Optional<MiningSkillCardData>, Optional<SkillsRecordData>> getCardOrRecord(ServerPlayer player, boolean inHolderSlot, int slot) {
        if (slot == -1) {
            if (player.getMainHandItem().getItem() instanceof MiningSkillCardItem item) {
                return Pair.of(Optional.of(item.getData(player.getMainHandItem())), Optional.empty());
            } else if (player.getOffhandItem().getItem() instanceof MiningSkillCardItem item) {
                return Pair.of(Optional.of(item.getData(player.getOffhandItem())), Optional.empty());
            }
        } else {
            if (inHolderSlot && ServicePlatform.SlotAPI.getSkillsRecordItem(player).getItem() instanceof SkillsRecordItem item) {
                ItemStack cardStack = item.getData(ServicePlatform.SlotAPI.getSkillsRecordItem(player)).getCardSlots().get(slot);
                return Pair.of(Optional.of(MiningSkillCardData.loadData(cardStack)), Optional.of(item.getData(ServicePlatform.SlotAPI.getSkillsRecordItem(player))));
            } else if (player.getMainHandItem().getItem() instanceof SkillsRecordItem item) {
                ItemStack cardStack = item.getData(player.getMainHandItem()).getCardSlots().get(slot);
                return Pair.of(Optional.of(MiningSkillCardData.loadData(cardStack)), Optional.of(item.getData(player.getMainHandItem())));
            } else if (player.getOffhandItem().getItem() instanceof SkillsRecordItem item) {
                ItemStack cardStack = item.getData(player.getOffhandItem()).getCardSlots().get(slot);
                return Pair.of(Optional.of(MiningSkillCardData.loadData(cardStack)), Optional.of(item.getData(player.getOffhandItem())));
            }
        }
        return Pair.of(Optional.empty(), Optional.empty());
    }
}
