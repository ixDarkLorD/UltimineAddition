//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package net.ixdarklord.ultimine_addition.common.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.datafixers.util.Pair;
import dev.architectury.platform.Platform;
import java.util.Collection;
import java.util.Optional;
import java.util.function.Consumer;
import net.ixdarklord.ultimine_addition.common.commands.arguments.CardHolderArgument;
import net.ixdarklord.ultimine_addition.common.commands.arguments.CardTierArgument;
import net.ixdarklord.ultimine_addition.common.commands.arguments.ChallengesArgument;
import net.ixdarklord.ultimine_addition.common.data.challenge.ChallengeData;
import net.ixdarklord.ultimine_addition.common.data.item.MiningSkillCardData;
import net.ixdarklord.ultimine_addition.common.data.item.SkillsRecordData;
import net.ixdarklord.ultimine_addition.common.item.MiningSkillCardItem;
import net.ixdarklord.ultimine_addition.common.item.SkillsRecordItem;
import net.ixdarklord.ultimine_addition.core.FTBUltimineAddition;
import net.ixdarklord.ultimine_addition.core.ServicePlatform;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.SlotArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class CardsCommand {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher, CommandBuildContext ignored, Commands.CommandSelection ignored2) {
        LiteralArgumentBuilder<CommandSourceStack> challengeInInventory =
                Commands.literal("in_inventory")
                        .then(Commands.argument("slot_index", SlotArgument.slot())
                                .then(attachChallengeActions(Commands.argument("card_holder", CardHolderArgument.allSlots()))));

        RequiredArgumentBuilder<CommandSourceStack, Pair<ResourceLocation, ChallengeData>> challengeArg =
                Commands.argument("challenge_id", ChallengesArgument.data())
                        .then(challengeInInventory);

        LiteralArgumentBuilder<CommandSourceStack> tierInInventory =
                Commands.literal("in_inventory")
                        .then(Commands.argument("slot_index", SlotArgument.slot())
                                .then(Commands.argument("card_holder", CardHolderArgument.allSlots())
                                        .executes((ctx) -> updateTierValue(ctx.getSource(), EntityArgument.getPlayers(ctx, "targets"), CardTierArgument.getTier(ctx, "new_tier"), new CardLocation(ctx)))));

        RequiredArgumentBuilder<CommandSourceStack, MiningSkillCardItem.Tier> tierArg =
                Commands.argument("new_tier", CardTierArgument.tier())
                        .then(tierInInventory);

        if (ServicePlatform.get().slotAPI().isModLoaded()) {
            String apiName = ServicePlatform.get().slotAPI().getAPIName();
            LiteralArgumentBuilder<CommandSourceStack> challengeInApi = Commands.literal("in_%s".formatted(apiName)).then(attachChallengeActions(Commands.argument("card_holder", CardHolderArgument.recordSlots())));
            challengeArg.then(challengeInApi);
            LiteralArgumentBuilder<CommandSourceStack> tierInApi = Commands.literal("in_%s".formatted(apiName)).then(Commands.argument("card_holder", CardHolderArgument.recordSlots()).executes((ctx) -> updateTierValue(ctx.getSource(), EntityArgument.getPlayers(ctx, "targets"), CardTierArgument.getTier(ctx, "new_tier"), new CardLocation(ctx))));
            tierArg.then(tierInApi);
        }

        FTBUltimineAddition.withCommandPrompt(dispatcher, 2, (builder) -> builder.then(
                Commands.literal("mining_skill_card")
                        .then(Commands.argument("targets", EntityArgument.players())
                                .then(Commands.literal("challenge")
                                        .then(challengeArg))
                                .then(Commands.literal("tier")
                                        .then(tierArg)))));
    }

    private static ArgumentBuilder<CommandSourceStack, ?> attachChallengeActions(ArgumentBuilder<CommandSourceStack, ?> parent) {
        parent.then(Commands.literal("set_point").then(Commands.argument("amount", IntegerArgumentType.integer(0)).executes((ctx) -> executeChallenge(ctx, CardsCommand.ChallengeModification.SET))));
        parent.then(Commands.literal("add_point").then(Commands.argument("amount", IntegerArgumentType.integer(1)).executes((ctx) -> executeChallenge(ctx, CardsCommand.ChallengeModification.ADD))));
        parent.then(Commands.literal("accomplish").executes((ctx) -> executeChallenge(ctx, CardsCommand.ChallengeModification.ACCOMPLISH)));
        return parent;
    }

    private static int executeChallenge(CommandContext<CommandSourceStack> ctx, ChallengeModification modification) throws CommandSyntaxException {
        CommandSourceStack source = ctx.getSource();
        Collection<ServerPlayer> targets = EntityArgument.getPlayers(ctx, "targets");
        Pair<ResourceLocation, ChallengeData> cData = ChallengesArgument.getData(ctx, "challenge_id");
        CardLocation location = new CardLocation(ctx);
        int amount = 0;
        if (modification != CardsCommand.ChallengeModification.ACCOMPLISH) {
            amount = IntegerArgumentType.getInteger(ctx, "amount");
        }

        return updateChallengeValue(source, targets, cData, location, amount, modification);
    }

    private static int updateChallengeValue(CommandSourceStack source, @NotNull Collection<ServerPlayer> targets, Pair<ResourceLocation, ChallengeData> cData, CardLocation location, int amount, ChallengeModification modification) {
        return executeForEach(source, targets, location, (ctx) -> {
            ResourceLocation challengeId = cData.getFirst();
            MiningSkillCardData.Challenge challenge = ctx.cardData.getChallenge(challengeId).orElse(null);
            if (challenge == null) {
                fail(source, "command.ultimine_addition.challenge.not_found", challengeId.toString());
            } else if (modification == CardsCommand.ChallengeModification.ACCOMPLISH && ctx.cardData.isChallengeAccomplished(challengeId)) {
                fail(source, "command.ultimine_addition.challenge.accomplished", challengeId.toString());
            } else {
                int oldPoints = challenge.getCurrentPoints();
                switch (modification.ordinal()) {
                    case 0 -> ctx.cardData.setAmount(challengeId, amount);
                    case 1 -> ctx.cardData.addAmount(challengeId, amount);
                    case 2 -> ctx.cardData.accomplishChallenge(challengeId);
                }

                int newPoints = ctx.cardData.getChallenge(challengeId).orElse(challenge).getCurrentPoints();
                saveData(ctx.player, ctx.recordData, ctx.cardData, location);
                success(source, ctx.player, targets, "command.ultimine_addition.challenge", cData.getFirst().toString(), oldPoints, newPoints);
            }
        });
    }

    private static int updateTierValue(CommandSourceStack source, @NotNull Collection<ServerPlayer> targets, MiningSkillCardItem.Tier tier, CardLocation location) {
        return executeForEach(source, targets, location, (ctx) -> {
            if (ctx.cardData.getTier() == tier) {
                fail(source, "command.ultimine_addition.cards.tier.set.already_setted", ctx.cardData.getStack().getHoverName().getString().toLowerCase(), tier.name());
            } else {
                ctx.cardData.setTier(tier).initChallenges().save();
                saveData(ctx.player, ctx.recordData, ctx.cardData, location);
                success(source, ctx.player, targets, "command.ultimine_addition.cards.tier.set", ctx.cardData.getStack().getHoverName(), tier.name());
            }
        });
    }

    private static int executeForEach(CommandSourceStack source, Collection<ServerPlayer> targets, CardLocation location, Consumer<CommandContextData> executor) {
        int count = 0;

        try {
            for(ServerPlayer player : targets) {
                ItemStack main = location.getItem(player);
                if (!main.isEmpty() && (main.getItem() instanceof SkillsRecordItem || main.getItem() instanceof MiningSkillCardItem)) {
                    SkillsRecordData recordData = main.getItem() instanceof SkillsRecordItem ? SkillsRecordData.load(main) : null;
                    Optional<MiningSkillCardData> dataOptional = recordData == null ? Optional.of(MiningSkillCardData.load(main)) : (location.isCardInsideSkillsRecord() ? recordData.getCardData(location.cardHolder) : Optional.empty());
                    if (dataOptional.isEmpty()) {
                        fail(source, "command.ultimine_addition.cards.not_found");
                    } else {
                        executor.accept(new CommandContextData(player, dataOptional.get(), recordData, main));
                        ++count;
                    }
                } else {
                    fail(source, "command.ultimine_addition.cards.not_found");
                }
            }
        } catch (Exception e) {
            if (Platform.isDevelopmentEnvironment()) {
                FTBUltimineAddition.LOGGER.error("Error executing CardsCommand!", e);
            }
        }

        return count;
    }

    private static void saveData(ServerPlayer player, @Nullable SkillsRecordData recordData, MiningSkillCardData cardData, CardLocation location) {
        if (recordData != null) {
            cardData.save();
            recordData.sendToClient(player, location.slotIndex).save();
        } else {
            cardData.sendToClient(player, location.slotIndex).save();
        }

    }

    private static void success(CommandSourceStack source, ServerPlayer player, Collection<ServerPlayer> targets, String baseKey, Object... args) {
        ServerPlayer self = source.getPlayer();
        if (player == self) {
            source.sendSuccess(() -> Component.translatable(baseKey + ".success", args).withStyle(ChatFormatting.DARK_AQUA), true);
        } else if (!player.hasPermissions(2)) {
            player.displayClientMessage(Component.translatable(baseKey + ".receiver", args).withStyle(ChatFormatting.GRAY), false);
        }

        if (targets.size() > 1 && player == self) {
            source.sendSuccess(() -> Component.translatable(baseKey + ".sender", args).withStyle(ChatFormatting.GRAY), true);
            int x = 1;

            for(ServerPlayer p : targets) {
                if (p != self) {
                    int finalX = x++;
                    source.sendSuccess(() -> Component.literal(finalX + ": " + p.getName().getString()).withStyle(ChatFormatting.YELLOW), true);
                }
            }
        }

    }

    private static void fail(CommandSourceStack source, String key, Object... args) {
        source.sendFailure(Component.translatable(key, args).withStyle(ChatFormatting.RED));
    }

    private record CommandContextData(ServerPlayer player, MiningSkillCardData cardData, @Nullable SkillsRecordData recordData, ItemStack mainStack) {
    }

    private enum ChallengeModification {
        SET,
        ADD,
        ACCOMPLISH
    }

    private static class CardLocation {
        public final int slotIndex;
        public final int cardHolder;

        private CardLocation(CommandContext<CommandSourceStack> ctx) {
            this.slotIndex = safeArg(() -> SlotArgument.getSlot(ctx, "slot_index"));
            this.cardHolder = safeArg(() -> CardHolderArgument.getSlot(ctx, "card_holder"));
        }

        public boolean isCardInsideSkillsRecord() {
            return this.cardHolder > -1;
        }

        public ItemStack getItem(Player player) {
            if (this.slotIndex == -1) {
                return ServicePlatform.get().slotAPI().isModLoaded() ? ServicePlatform.get().slotAPI().getSkillsRecordItem(player) : ItemStack.EMPTY;
            } else {
                return player.getSlot(this.slotIndex).get();
            }
        }

        private static int safeArg(SupplierWithException<Integer> supplier) {
            try {
                return supplier.get();
            } catch (Exception var2) {
                return -1;
            }
        }

        @FunctionalInterface
        private interface SupplierWithException<T> {
            T get() throws Exception;
        }
    }
}
