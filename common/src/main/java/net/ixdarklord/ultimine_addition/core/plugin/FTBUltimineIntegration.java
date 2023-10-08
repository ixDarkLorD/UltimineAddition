package net.ixdarklord.ultimine_addition.core.plugin;

import dev.ftb.mods.ftbultimine.FTBUltimine;
import dev.ftb.mods.ftbultimine.client.FTBUltimineClient;
import dev.ftb.mods.ftbultimine.integration.FTBRanksIntegration;
import dev.ftb.mods.ftbultimine.integration.FTBUltiminePlugin;
import net.ixdarklord.ultimine_addition.common.config.ConfigHandler;
import net.ixdarklord.ultimine_addition.common.effect.MineGoJuiceEffect;
import net.ixdarklord.ultimine_addition.common.effect.ModMobEffects;
import net.ixdarklord.ultimine_addition.common.item.MiningSkillCardItem;
import net.ixdarklord.ultimine_addition.core.Constants;
import net.ixdarklord.ultimine_addition.core.ServicePlatform;
import net.ixdarklord.ultimine_addition.util.ItemUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.player.Player;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import static dev.ftb.mods.ftbultimine.config.FTBUltimineServerConfig.MAX_BLOCKS;

public class FTBUltimineIntegration implements FTBUltiminePlugin {
    private static boolean isButtonPressed;

    @Override
    public void init() {
        Constants.LOGGER.info("Registering plugin to FTBUltimine!");
    }

    @Override
    public boolean canUltimine(Player player) {
        boolean result = ServicePlatform.Players.isPlayerUltimineCapable(player);
        if (result) return true;
        if (player.hasEffect(ModMobEffects.MINE_GO_JUICE_PICKAXE)) {
            if (ItemUtils.isItemInHandPickaxe(player)) result = true;
        }
        if (player.hasEffect(ModMobEffects.MINE_GO_JUICE_AXE)) {
            if (ItemUtils.isItemInHandAxe(player)) result = true;
        }
        if (player.hasEffect(ModMobEffects.MINE_GO_JUICE_SHOVEL)) {
            if (ItemUtils.isItemInHandShovel(player)) result = true;
        }
        if (player.hasEffect(ModMobEffects.MINE_GO_JUICE_HOE)) {
            if (ItemUtils.isItemInHandHoe(player)) result = true;
        }
        return result;
    }
    public static void keyEvent(Player player) {
        if (FTBUltimineClient.keyBinding.isDown()) {
            if (!isButtonPressed) {
                MutableComponent MSG = Component.translatable("info.ultimine_addition.incapable");
                if (!ServicePlatform.Players.isPlayerUltimineCapable(player)) {
                     if (ItemUtils.isItemInHandPickaxe(player)) {
                        if (!player.hasEffect(ModMobEffects.MINE_GO_JUICE_PICKAXE)) {
                            player.displayClientMessage(MSG.withStyle(ChatFormatting.RED), false);
                            player.displayClientMessage(Component.literal("\u2716 ").append(Component.translatable("info.ultimine_addition.required_skill.pickaxe")).withStyle(ChatFormatting.GRAY), false);
                        }
                    } else if (ItemUtils.isItemInHandAxe(player)) {
                        if (!player.hasEffect(ModMobEffects.MINE_GO_JUICE_AXE)) {
                            player.displayClientMessage(MSG.withStyle(ChatFormatting.RED), false);
                            player.displayClientMessage(Component.literal("\u2716 ").append(Component.translatable("info.ultimine_addition.required_skill.axe")).withStyle(ChatFormatting.GRAY), false);
                        }
                    } else if (ItemUtils.isItemInHandShovel(player)) {
                        if (!player.hasEffect(ModMobEffects.MINE_GO_JUICE_SHOVEL)) {
                            player.displayClientMessage(MSG.withStyle(ChatFormatting.RED), false);
                            player.displayClientMessage(Component.literal("\u2716 ").append(Component.translatable("info.ultimine_addition.required_skill.shovel")).withStyle(ChatFormatting.GRAY), false);
                        }
                    } else if (ItemUtils.isItemInHandHoe(player)) {
                        if (!player.hasEffect(ModMobEffects.MINE_GO_JUICE_HOE)) {
                            player.displayClientMessage(MSG.withStyle(ChatFormatting.RED), false);
                            player.displayClientMessage(Component.literal("\u2716 ").append(Component.translatable("info.ultimine_addition.required_skill.hoe")).withStyle(ChatFormatting.GRAY), false);
                        }
                    } else if (ItemUtils.isItemInHandNotTools(player)) {
                         player.displayClientMessage(MSG.withStyle(ChatFormatting.RED), false);
                         player.displayClientMessage(Component.literal("\u2716 ").append(Component.translatable("info.ultimine_addition.required_skill.all")).withStyle(ChatFormatting.GRAY), false);
                     }
                }
            }
            isButtonPressed = true;
        } else {
            isButtonPressed = false;
        }
    }

    public static int getMaxBlocks(ServerPlayer player) {
        if (!ConfigHandler.COMMON.TIER_BASED_MAX_BLOCKS.get())
            return FTBUltimine.ranksMod ? FTBRanksIntegration.getMaxBlocks(player) : MAX_BLOCKS.get();

        List<MobEffectInstance> instances = new ArrayList<>(player.getActiveEffects().stream().filter(mobEffectInstance -> mobEffectInstance.getEffect() instanceof MineGoJuiceEffect).toList());
        if (!ServicePlatform.Players.isPlayerUltimineCapable(player) && !instances.isEmpty()) {
            instances.sort(Comparator.comparingInt(MobEffectInstance::getAmplifier).reversed());

            if (ItemUtils.isItemInHandPickaxe(player)) {
                instances.removeIf(instance -> ((MineGoJuiceEffect)instance.getEffect()).getType() != MiningSkillCardItem.Type.PICKAXE);
            } else if (ItemUtils.isItemInHandAxe(player)) {
                instances.removeIf(instance -> ((MineGoJuiceEffect)instance.getEffect()).getType() != MiningSkillCardItem.Type.AXE);
            } else if (ItemUtils.isItemInHandShovel(player)) {
                instances.removeIf(instance -> ((MineGoJuiceEffect)instance.getEffect()).getType() != MiningSkillCardItem.Type.SHOVEL);
            } else if (ItemUtils.isItemInHandHoe(player)) {
                instances.removeIf(instance -> ((MineGoJuiceEffect)instance.getEffect()).getType() != MiningSkillCardItem.Type.HOE);
            }

            if (!instances.isEmpty()) {
                return switch (instances.get(0).getAmplifier()) {
                    case 0 -> ConfigHandler.COMMON.TIER_1_MAX_BLOCKS.get();
                    case 1 -> ConfigHandler.COMMON.TIER_2_MAX_BLOCKS.get();
                    default -> ConfigHandler.COMMON.TIER_3_MAX_BLOCKS.get();
                };
            }
        }
        return FTBUltimine.ranksMod ? FTBRanksIntegration.getMaxBlocks(player) : MAX_BLOCKS.get();
    }
}
