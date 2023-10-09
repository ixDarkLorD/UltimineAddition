package net.ixdarklord.ultimine_addition.common.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.datafixers.util.Pair;
import net.ixdarklord.ultimine_addition.common.command.arguments.CardSlotsArgument;
import net.ixdarklord.ultimine_addition.common.command.arguments.CardTierArgument;
import net.ixdarklord.ultimine_addition.common.command.arguments.ChallengesArgument;
import net.ixdarklord.ultimine_addition.common.data.challenge.ChallengesData;
import net.ixdarklord.ultimine_addition.common.data.item.MiningSkillCardData;
import net.ixdarklord.ultimine_addition.common.data.item.SkillsRecordData;
import net.ixdarklord.ultimine_addition.common.item.MiningSkillCardItem;
import net.ixdarklord.ultimine_addition.common.item.SkillsRecordItem;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Objects;

public class CardsCommand {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher, CommandBuildContext ignored, Commands.CommandSelection ignored2) {
        dispatcher.register(Commands.literal("ultimine_addition").requires(p -> p.hasPermission(Commands.LEVEL_GAMEMASTERS))
                .then(Commands.literal("mining_skill_card")
                        .then(Commands.literal("challenges")
                                .then(Commands.argument("targets", EntityArgument.players())
                                        .then(Commands.argument("challengeId", ChallengesArgument.data())
                                                .then(Commands.literal("self")
                                                        .then(Commands.literal("set_point").then(Commands.argument("amount", IntegerArgumentType.integer(0)).executes(context -> updateChallengeValue(context.getSource(), EntityArgument.getPlayers(context, "targets"), ChallengesArgument.getData(context, "challengeId"), -1, IntegerArgumentType.getInteger(context, "amount"), true))))
                                                        .then(Commands.literal("add_point").then(Commands.argument("amount", IntegerArgumentType.integer(0)).executes(context -> updateChallengeValue(context.getSource(), EntityArgument.getPlayers(context, "targets"), ChallengesArgument.getData(context, "challengeId"), -1, IntegerArgumentType.getInteger(context, "amount"), false))))
                                                        .then(Commands.literal("accomplish").executes(context -> updateChallengeValue(context.getSource(), EntityArgument.getPlayers(context, "targets"), ChallengesArgument.getData(context, "challengeId"), -1, -1, null)))
                                                )
                                                .then(Commands.literal("skills_record").then(Commands.argument("card_slot", CardSlotsArgument.slots())
                                                        .then(Commands.literal("set_point").then(Commands.argument("amount", IntegerArgumentType.integer(0)).executes(context -> updateChallengeValue(context.getSource(), EntityArgument.getPlayers(context, "targets"), ChallengesArgument.getData(context, "challengeId"), CardSlotsArgument.getSlot(context, "card_slot"), IntegerArgumentType.getInteger(context, "amount"), true))))
                                                        .then(Commands.literal("add_point").then(Commands.argument("amount", IntegerArgumentType.integer(0)).executes(context -> updateChallengeValue(context.getSource(), EntityArgument.getPlayers(context, "targets"), ChallengesArgument.getData(context, "challengeId"), CardSlotsArgument.getSlot(context, "card_slot"), IntegerArgumentType.getInteger(context, "amount"), false))))
                                                        .then(Commands.literal("accomplish").executes(context -> updateChallengeValue(context.getSource(), EntityArgument.getPlayers(context, "targets"), ChallengesArgument.getData(context, "challengeId"), CardSlotsArgument.getSlot(context, "card_slot"), -1, null)))
                                                )))))
                        .then(Commands.literal("change_tier")
                                .then(Commands.argument("targets", EntityArgument.players())
                                        .then(Commands.literal("self")
                                                .then(Commands.argument("tier", CardTierArgument.tier()).executes(context -> setTier(context.getSource(), EntityArgument.getPlayers(context, "targets"), CardTierArgument.getTier(context, "tier"), -1)))
                                        )
                                        .then(Commands.literal("skills_record").then(Commands.argument("card_slot", CardSlotsArgument.slots())
                                                .then(Commands.argument("tier", CardTierArgument.tier()).executes(context -> setTier(context.getSource(), EntityArgument.getPlayers(context, "targets"), CardTierArgument.getTier(context, "tier"), CardSlotsArgument.getSlot(context, "card_slot"))
                                                ))))))));
    }

    private static int updateChallengeValue(CommandSourceStack source, @NotNull Collection<ServerPlayer> targets, Pair<ResourceLocation, ChallengesData> cData, int slotIndex, int amount, Boolean replaceValue) {
        int i = 0;
        for (ServerPlayer player : targets) {
            ItemStack stack = getCard(player, slotIndex);
            if (stack.getItem() instanceof MiningSkillCardItem card && card.getData(stack).isChallengeExists(cData.getFirst())) {
                MiningSkillCardData data = card.getData(stack).sendToClient(player);
                int oldValue = data.getChallenge(cData.getFirst()).getCurrent();
                if (amount > -1) {
                    if (replaceValue) {
                        data.setAmount(cData.getFirst(), amount).saveData(stack);
                    } else if (data.getChallenge(cData.getFirst()).getCurrent() < data.getChallenge(cData.getFirst()).getRequired()) {
                        data.addAmount(cData.getFirst(), amount).saveData(stack);
                    } else {
                        source.sendFailure(Component.translatable("command.ultimine_addition.challenge.accomplished", cData.getFirst().toString()).withStyle(ChatFormatting.RED));
                        i++;
                        return i;
                    }
                } else {
                    if (!card.getData(stack).isChallengeAccomplished(cData.getFirst())) {
                        data.accomplishChallenge(cData.getFirst()).saveData(stack);
                    } else {
                        source.sendFailure(Component.translatable("command.ultimine_addition.challenge.accomplished", cData.getFirst().toString()).withStyle(ChatFormatting.RED));
                        i++;
                        return i;
                    }
                }
                int newValue = data.getChallenge(cData.getFirst()).getCurrent();
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
                source.sendFailure(Component.translatable("command.ultimine_addition.cards.not_found", stack.getHoverName()).withStyle(ChatFormatting.RED));
                i++;
            }
        }
        return i;
    }

    private static int setTier(CommandSourceStack source, @NotNull Collection<ServerPlayer> targets, MiningSkillCardItem.Tier tier, int slot) {
        int i = 0;
        for (ServerPlayer player : targets) {
            ItemStack stack = getCard(player, slot);
            if (stack.getItem() instanceof MiningSkillCardItem card && card.getData(stack).getTier() != tier) {
                MiningSkillCardData data = card.getData(stack);
                data.sendToClient(player);
                data.setTier(tier);
                data.initChallenges();
                data.saveData(stack);
                i++;

                if (player == source.getPlayer()) {
                    source.sendSuccess(() -> Component.translatable("command.ultimine_addition.cards.tier.set.success", stack.getHoverName(), tier.name()).withStyle(ChatFormatting.DARK_AQUA), true);
                }
                if (i > 1 && player != source.getPlayer() && !player.hasPermissions(2)) {
                    player.displayClientMessage(Component.translatable("command.ultimine_addition.cards.tier.set.receiver", stack.getHoverName(), tier.name(), Objects.requireNonNull(source.getPlayer()).getName().getString()).withStyle(ChatFormatting.GRAY), false);
                }
                if (i > 1) {
                    source.sendSuccess(() -> Component.translatable("command.ultimine_addition.cards.tier.set.sender", stack.getHoverName(), tier.name()).withStyle(ChatFormatting.GRAY), true);
                    int x = 1;
                    for (ServerPlayer p : targets) {
                        if (p != source.getPlayer()) {
                            int finalX = x;
                            source.sendSuccess(() -> Component.literal(finalX + ": " + p.getName().getString()).withStyle(ChatFormatting.YELLOW), true);
                            x++;
                        }
                    }
                }
            } else if (stack.getItem() instanceof MiningSkillCardItem card && card.getData(stack).getTier() == tier) {
                source.sendFailure(Component.translatable("command.ultimine_addition.cards.tier.set.already_setted", stack.getHoverName().getString().toLowerCase(), tier.name()).withStyle(ChatFormatting.RED));
                i++;
            } else if (player == source.getPlayer()) {
                source.sendFailure(Component.translatable("command.ultimine_addition.cards.not_found", stack.getHoverName()).withStyle(ChatFormatting.RED));
                i++;
            }
        }
        return i;
    }

    private static ItemStack getCard(Player player, int slotIndex) {
        if (slotIndex == -1) {
            if (player.getMainHandItem().getItem() instanceof MiningSkillCardItem) {
                return player.getMainHandItem();
            } else if (player.getOffhandItem().getItem() instanceof MiningSkillCardItem){
                return player.getOffhandItem();
            }
        } else {
            ItemStack cardHolder = null;
            if (player.getMainHandItem().getItem() instanceof SkillsRecordItem) {
                cardHolder = player.getMainHandItem();
            } else if (player.getOffhandItem().getItem() instanceof SkillsRecordItem){
                cardHolder = player.getOffhandItem();
            }
            if (cardHolder != null) {
                var data = new SkillsRecordData().loadData(cardHolder);
                return data.getCardSlots().get(slotIndex);
            }
        }
        return ItemStack.EMPTY;
    }
}
