package net.ixdarklord.ultimine_addition.common.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.datafixers.util.Pair;
import dev.architectury.platform.Platform;
import net.ixdarklord.coolcatlib.api.util.SlotReference;
import net.ixdarklord.ultimine_addition.common.command.arguments.CardHolderArgument;
import net.ixdarklord.ultimine_addition.common.command.arguments.CardTierArgument;
import net.ixdarklord.ultimine_addition.common.command.arguments.ChallengesArgument;
import net.ixdarklord.ultimine_addition.common.data.challenge.ChallengesData;
import net.ixdarklord.ultimine_addition.common.data.item.MiningSkillCardData;
import net.ixdarklord.ultimine_addition.common.data.item.SkillsRecordData;
import net.ixdarklord.ultimine_addition.common.item.MiningSkillCardItem;
import net.ixdarklord.ultimine_addition.common.item.SkillsRecordItem;
import net.ixdarklord.ultimine_addition.core.ServicePlatform;
import net.ixdarklord.ultimine_addition.core.FTBUltimineAddition;
import net.ixdarklord.ultimine_addition.util.ItemUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.SlotArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Objects;
import java.util.Optional;

public class CardsCommand {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher, CommandBuildContext ignored, Commands.CommandSelection ignored2) {
        var challengeArgument = Commands.argument("challenge_id", ChallengesArgument.data())
                .then(Commands.literal("in_inventory").then(Commands.argument("slot_index", SlotArgument.slot()).then(Commands.argument("card_holder", CardHolderArgument.slot(true))
                        .then(Commands.literal("set_point").then(Commands.argument("amount", IntegerArgumentType.integer(0)).executes(context -> updateChallengeValue(context.getSource(), EntityArgument.getPlayers(context, "targets"), ChallengesArgument.getData(context, "challenge_id"), new CardLocation(context), IntegerArgumentType.getInteger(context, "amount"), true))))
                        .then(Commands.literal("add_point").then(Commands.argument("amount", IntegerArgumentType.integer(1)).executes(context -> updateChallengeValue(context.getSource(), EntityArgument.getPlayers(context, "targets"), ChallengesArgument.getData(context, "challenge_id"), new CardLocation(context), IntegerArgumentType.getInteger(context, "amount"), false))))
                        .then(Commands.literal("accomplish").executes(context -> updateChallengeValue(context.getSource(), EntityArgument.getPlayers(context, "targets"), ChallengesArgument.getData(context, "challenge_id"), new CardLocation(context), null, false)))
                )));

        var tierArgument = Commands.argument("new_tier", CardTierArgument.tier())
                .then(Commands.literal("in_inventory").then(Commands.argument("slot_index", SlotArgument.slot()).then(Commands.argument("card_holder", CardHolderArgument.slot(true))
                        .executes(context -> updateTierValue(context.getSource(), EntityArgument.getPlayers(context, "targets"), CardTierArgument.getTier(context, "new_tier"), new CardLocation(context))))));

        if (ServicePlatform.get().slotAPI().isModLoaded()) {
            challengeArgument.then(Commands.literal("in_%s".formatted(ServicePlatform.get().slotAPI().getAPIName())).then(Commands.argument("card_holder", CardHolderArgument.slot(false))
                    .then(Commands.literal("set_point").then(Commands.argument("amount", IntegerArgumentType.integer(0)).executes(context -> updateChallengeValue(context.getSource(), EntityArgument.getPlayers(context, "targets"), ChallengesArgument.getData(context, "challenge_id"), new CardLocation(context), IntegerArgumentType.getInteger(context, "amount"), true))))
                    .then(Commands.literal("add_point").then(Commands.argument("amount", IntegerArgumentType.integer(1)).executes(context -> updateChallengeValue(context.getSource(), EntityArgument.getPlayers(context, "targets"), ChallengesArgument.getData(context, "challenge_id"), new CardLocation(context), IntegerArgumentType.getInteger(context, "amount"), false))))
                    .then(Commands.literal("accomplish").executes(context -> updateChallengeValue(context.getSource(), EntityArgument.getPlayers(context, "targets"), ChallengesArgument.getData(context, "challenge_id"), new CardLocation(context), null, false))))
            );

            tierArgument.then(Commands.literal("in_%s".formatted(ServicePlatform.get().slotAPI().getAPIName())).then(Commands.argument("card_holder", CardHolderArgument.slot(false))
                    .executes(context -> updateTierValue(context.getSource(), EntityArgument.getPlayers(context, "targets"), CardTierArgument.getTier(context, "new_tier"), new CardLocation(context)))));
        }

        FTBUltimineAddition.withCommandPrompt(dispatcher, Commands.LEVEL_GAMEMASTERS, builder ->
                builder.then(Commands.literal("mining_skill_card")
                        .then(Commands.literal("challenges")
                                .then(Commands.argument("targets", EntityArgument.players())
                                        .then(challengeArgument)))
                        .then(Commands.literal("change_tier")
                                .then(Commands.argument("targets", EntityArgument.players())
                                        .then(tierArgument)))));
    }

    private static int updateChallengeValue(CommandSourceStack source, @NotNull Collection<ServerPlayer> targets, Pair<ResourceLocation, ChallengesData> cData, CardLocation cardLocation, @Nullable Integer amount, Boolean replaceValue) {
        int i = 0;
        try {
            for (ServerPlayer player : targets) {
                Optional<SlotReference.Player> cardOrHolder = getCardOrRecord(player, cardLocation);
                if (cardOrHolder.isEmpty()) {
                    source.sendFailure(Component.translatable("command.ultimine_addition.cards.not_found").withStyle(ChatFormatting.RED));
                    continue;
                }

                ItemStack mainStack = cardOrHolder.get().getItem();
                ItemStack cardStack = mainStack.copy();
                SkillsRecordData recordData = null;

                if (!(mainStack.getItem() instanceof MiningSkillCardItem)) {
                    recordData = SkillsRecordData.loadData(mainStack);
                    cardStack = recordData.getCardSlots().get(cardLocation.cardHolder);
                }

                MiningSkillCardData cardData = MiningSkillCardData.loadData(cardStack);
                ResourceLocation challengeId = cData.getFirst();
                MiningSkillCardData.ChallengeHolder challengeHolder = cardData.getChallenge(challengeId).orElse(null);

                if (challengeHolder == null) {
                    source.sendFailure(Component.translatable("command.ultimine_addition.challenge.not_found", cData.getFirst().toString()));
                    return i;
                }

                int oldPoints = challengeHolder.getCurrentPoints();
                Integer newPoints = amount == null ? null : replaceValue ? amount : oldPoints + amount;

                if (!cardData.isChallengeAccomplished(challengeId) || replaceValue) {
                    if (newPoints != null) {
                        cardData.setAmount(challengeId, newPoints);
                    } else cardData.accomplishChallenge(challengeId);
                    cardData.saveData(cardStack);
                    if (recordData == null)
                        cardData.sendToClient(player, cardLocation.inventoryIndex).saveData(cardStack);
                    else
                        recordData.sendToClient(player, cardLocation.inventoryIndex).saveData(mainStack);
                    i++;
                } else {
                    source.sendFailure(Component.translatable("command.ultimine_addition.challenge.accomplished", cData.getFirst().toString()).withStyle(ChatFormatting.RED));
                    continue;
                }

                if (player == source.getPlayer()) {
                    source.sendSuccess(() -> Component.translatable("command.ultimine_addition.challenge.success", cData.getFirst().toString(), oldPoints, newPoints).withStyle(ChatFormatting.DARK_AQUA), true);
                }

                if (i > 1 && player != source.getPlayer() && !player.hasPermissions(2)) {
                    player.displayClientMessage(Component.translatable("command.ultimine_addition.challenge.receiver", cData.getFirst().toString(), oldPoints, newPoints, Objects.requireNonNull(source.getPlayer()).getName().getString()).withStyle(ChatFormatting.GRAY), false);
                }

                if (i > 1) {
                    source.sendSuccess(() -> Component.translatable("command.ultimine_addition.challenge.sender", cData.getFirst().toString(), oldPoints, newPoints).withStyle(ChatFormatting.GRAY), true);
                    int x = 1;
                    for (ServerPlayer p : targets) {
                        if (p != source.getPlayer()) {
                            int finalX = x;
                            source.sendSuccess(() -> Component.literal(finalX + ": " + p.getName().getString()).withStyle(ChatFormatting.YELLOW), true);
                            x++;
                        }
                    }
                }
            }
        } catch (Exception err) {
            if (Platform.isDevelopmentEnvironment())
                FTBUltimineAddition.LOGGER.error("Error occurred while executing the command!", err);
        }
        return i;
    }

    private static int updateTierValue(CommandSourceStack source, @NotNull Collection<ServerPlayer> targets, MiningSkillCardItem.Tier tier, CardLocation cardLocation) {
        int i = 0;
        try {
            for (ServerPlayer player : targets) {
                Optional<SlotReference.Player> cardOrHolder = getCardOrRecord(player, cardLocation);
                if (cardOrHolder.isEmpty()) {
                    source.sendFailure(Component.translatable("command.ultimine_addition.cards.not_found").withStyle(ChatFormatting.RED));
                    return i;
                }

                ItemStack mainStack = cardOrHolder.get().getItem();
                ItemStack cardStack = mainStack.copy();
                SkillsRecordData recordData = null;

                if (!(mainStack.getItem() instanceof MiningSkillCardItem)) {
                    recordData = SkillsRecordData.loadData(mainStack);
                    cardStack = recordData.getCardSlots().get(cardLocation.cardHolder);
                }

                MiningSkillCardData cardData = MiningSkillCardData.loadData(cardStack);
                if (cardData.getTier() != tier) {
                    cardData.setTier(tier).initChallenges();
                    if (recordData == null)
                        cardData.sendToClient(player, cardLocation.inventoryIndex).saveData(cardStack);
                    else
                        recordData.sendToClient(player, cardLocation.inventoryIndex).saveData(mainStack);
                    i++;
                } else {
                    source.sendFailure(Component.translatable("command.ultimine_addition.cards.tier.set.already_setted", cardData.get().getHoverName().getString().toLowerCase(), tier.name()).withStyle(ChatFormatting.RED));
                    continue;
                }

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
            }
        } catch (Exception err) {
            if (Platform.isDevelopmentEnvironment())
                FTBUltimineAddition.LOGGER.error("Error occurred!", err);
        }
        return i;
    }

    private static Optional<SlotReference.Player> getCardOrRecord(ServerPlayer player, CardLocation location) {
        return ItemUtils.getSlotReferences(player,
                        stack -> location.isCardInsideSkillsRecord()
                                ? stack.getItem() instanceof SkillsRecordItem
                                : stack.getItem() instanceof MiningSkillCardItem cardItem && cardItem.getType() != MiningSkillCardItem.Type.EMPTY, false).stream()
                .filter(slot -> slot.getIndex() == location.inventoryIndex)
                .findAny();
    }

    private static class CardLocation {
        public final int inventoryIndex;
        public final int cardHolder;

        private CardLocation(CommandContext<CommandSourceStack> context) {
            this.inventoryIndex = Util.make(() -> {
                try {
                    return SlotArgument.getSlot(context, "slot_index");
                } catch (Exception ignored) {
                    return -1;
                }
            });
            this.cardHolder = Util.make(() -> {
                try {
                    return CardHolderArgument.getSlot(context, "card_holder");
                } catch (Exception ignored) {
                    return -1;
                }
            });
        }

        public boolean isCardInsideSkillsRecord() {
            return this.cardHolder > -1;
        }
    }
}
