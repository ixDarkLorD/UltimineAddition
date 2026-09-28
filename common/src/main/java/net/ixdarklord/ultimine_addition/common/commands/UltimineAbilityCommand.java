package net.ixdarklord.ultimine_addition.common.commands;

import java.util.List;
import net.ixdarklord.ultimine_addition.common.data.player.PlayerAbilityData;
import org.jetbrains.annotations.Nullable;
import net.minecraft.commands.SharedSuggestionProvider;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.ixdarklord.ultimine_addition.common.item.MiningSkillCardItem;
import net.ixdarklord.ultimine_addition.common.item.ShapeCertificateItem;
import net.minecraft.server.permissions.Permissions;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;
import net.ixdarklord.ultimine_addition.core.FTBUltimineAddition;
import net.ixdarklord.ultimine_addition.core.ServicePlatform;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

import java.util.Collection;
import java.util.Objects;

public final class UltimineAbilityCommand {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher, CommandBuildContext ignored1, Commands.CommandSelection ignored2) {
        FTBUltimineAddition.withCommandPrompt(dispatcher, builder ->
                builder.then(Commands.literal("ultimine_ability")
                        .requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS))
                        .then(Commands.argument("targets", EntityArgument.players())
                                .then(Commands.literal("set")
                                        .then(Commands.argument("state", BoolArgumentType.bool()).executes(context ->
                                                setAbility(context.getSource(), EntityArgument.getPlayers(context, "targets"), BoolArgumentType.getBool(context, "state"))
                                        )))
                                .then(Commands.literal("shapes")
                                        .then(Commands.literal("grant")
                                                .then(grantTier(MiningSkillCardItem.Tier.Novice))
                                                .then(grantTier(MiningSkillCardItem.Tier.Apprentice))
                                                .then(grantTier(MiningSkillCardItem.Tier.Adept)))
                                        .then(Commands.literal("reset").executes(context -> resetShapes(context.getSource(), EntityArgument.getPlayers(context, "targets"))))))));
    }

    // "grant <tier> [tool]": without a tool, every card type gets the tier's shapes.
    private static LiteralArgumentBuilder<CommandSourceStack> grantTier(MiningSkillCardItem.Tier tier) {
        return Commands.literal(tier.name().toLowerCase())
                .executes(context -> grantShapes(context.getSource(), EntityArgument.getPlayers(context, "targets"), tier, null))
                .then(Commands.argument("tool", StringArgumentType.word())
                        .suggests((context, builder) -> SharedSuggestionProvider.suggest(MiningSkillCardItem.Type.TYPES.stream()
                                .filter(type -> type != MiningSkillCardItem.Type.EMPTY).map(MiningSkillCardItem.Type::getId), builder))
                        .executes(context -> grantShapes(context.getSource(), EntityArgument.getPlayers(context, "targets"), tier, StringArgumentType.getString(context, "tool"))));
    }

    private static int grantShapes(CommandSourceStack source, Collection<ServerPlayer> targets, MiningSkillCardItem.Tier tier, @Nullable String tool) {
        List<String> tools = MiningSkillCardItem.Type.TYPES.stream()
                .filter(type -> type != MiningSkillCardItem.Type.EMPTY)
                .map(MiningSkillCardItem.Type::getId)
                .filter(id -> tool == null || id.equals(tool))
                .toList();
        if (tools.isEmpty()) {
            source.sendFailure(Component.translatable("command.ultimine_addition.shapes.unknown_tool", String.valueOf(tool)));
            return 0;
        }
        for (ServerPlayer player : targets) {
            for (String id : tools) ServicePlatform.get().players().unlockShapes(player, id, ShapeCertificateItem.tierList(tier), tier.getValue());
        }
        Component toolName = tool == null ? Component.translatable("info.ultimine_addition.required_skill.all") : ShapeCertificateItem.toolName(tool);
        source.sendSuccess(() -> Component.translatable("command.ultimine_addition.shapes.granted", tier.getDisplayName(), toolName, targets.size()).withStyle(ChatFormatting.DARK_AQUA), true);
        return targets.size();
    }

    private static int resetShapes(CommandSourceStack source, Collection<ServerPlayer> targets) {
        for (ServerPlayer player : targets) {
            ServicePlatform.get().players().modifyAbilityData(player, PlayerAbilityData::resetShapes);
        }
        source.sendSuccess(() -> Component.translatable("command.ultimine_addition.shapes.reset", targets.size()).withStyle(ChatFormatting.DARK_AQUA), true);
        return targets.size();
    }

    private static int setAbility(CommandSourceStack source, Collection<ServerPlayer> targets, boolean state) {
        int i = 0;
        String State = String.valueOf(state);
        for (ServerPlayer player : targets) {
            if (ServicePlatform.get().players().isPlayerUltimineCapable(player) != state) {
                ServicePlatform.get().players().setPlayerUltimineCapability(player, state);
                i++;

                if (player == source.getPlayer()) {
                    source.sendSuccess(() -> Component.translatable("command.ultimine_addition.set_ability.success", State).withStyle(ChatFormatting.DARK_AQUA), true);
                }
                if (i > 1 && player != source.getPlayer() && !player.permissions().hasPermission(Permissions.COMMANDS_GAMEMASTER)) {
                    player.sendSystemMessage(Component.translatable("command.ultimine_addition.set_ability.receiver", State, Objects.requireNonNull(source.getPlayer()).getName().getString()).withStyle(ChatFormatting.GRAY));
                }
                if (i > 1) {
                    source.sendSuccess(() -> Component.translatable("command.ultimine_addition.set_ability.sender", State).withStyle(ChatFormatting.GRAY), true);
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
                source.sendFailure(Component.translatable("command.ultimine_addition.set_ability.already_setted", State).withStyle(ChatFormatting.RED));
                i++;
            }
        }
        return i;
    }
}
