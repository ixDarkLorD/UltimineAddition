package net.ixdarklord.ultimine_addition.core;

import dev.ftb.mods.ftbultimine.FTBUltimine;
import dev.ftb.mods.ftbultimine.client.FTBUltimineClient;
import dev.ftb.mods.ftbultimine.integration.FTBRanksIntegration;
import dev.ftb.mods.ftbultimine.integration.FTBUltiminePlugin;
import dev.ftb.mods.ftbultimine.shape.Shape;
import net.ixdarklord.ultimine_addition.common.effect.MineGoJuiceEffect;
import net.ixdarklord.ultimine_addition.common.item.MiningSkillCardItem;
import net.ixdarklord.ultimine_addition.config.ConfigHandler;
import net.ixdarklord.ultimine_addition.config.PlaystyleMode;
import net.ixdarklord.ultimine_addition.mixin.ShapeRegistryAccessor;
import net.ixdarklord.ultimine_addition.util.ItemUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import java.util.*;

import static dev.ftb.mods.ftbultimine.config.FTBUltimineServerConfig.MAX_BLOCKS;

public class FTBUltimineIntegration implements FTBUltiminePlugin {
    private static boolean isButtonPressed;

    @Override
    public boolean canUltimine(Player player) {
        boolean result = ServicePlatform.get().players().isPlayerUltimineCapable(player);
        if (result) return true;
        if (ConfigHandler.COMMON.PLAYSTYLE_MODE.get() == PlaystyleMode.LEGACY) return result;

        if (isPlayerHasCustomCardValidEffect(player)) {
            if (ItemUtils.isItemInHandCustomCardValid(player)) result = true;
        }

        if (!ItemUtils.checkTargetedBlock(player)) return false;

        if (player.hasEffect(BuiltInRegistries.MOB_EFFECT.getHolder(Registration.MINE_GO_JUICE_PICKAXE.getId()).orElseThrow())) {
            if (ItemUtils.isItemInHandPickaxe(player)) result = true;
        }
        if (player.hasEffect(BuiltInRegistries.MOB_EFFECT.getHolder(Registration.MINE_GO_JUICE_AXE.getId()).orElseThrow())) {
            if (ItemUtils.isItemInHandAxe(player)) result = true;
        }
        if (player.hasEffect(BuiltInRegistries.MOB_EFFECT.getHolder(Registration.MINE_GO_JUICE_SHOVEL.getId()).orElseThrow())) {
            if (ItemUtils.isItemInHandShovel(player)) result = true;
        }
        if (player.hasEffect(BuiltInRegistries.MOB_EFFECT.getHolder(Registration.MINE_GO_JUICE_HOE.getId()).orElseThrow())) {
            if (ItemUtils.isItemInHandHoe(player)) result = true;
        }
        return result;
    }

    public static void keyEvent(Player player) {
        if (FTBUltimineClient.keyBinding.isDown()) {
            if (!isButtonPressed) {
                MutableComponent MSG = Component.translatable("info.ultimine_addition.incapable");
                Component requiredTool = null;

                if (!ServicePlatform.get().players().isPlayerUltimineCapable(player)) {
                    if (ItemUtils.isItemInHandCustomCardValid(player)) {
                        if (!isPlayerHasCustomCardValidEffect(player)) {
                            String[] toolNames = getCustomCardTypes(player).stream()
                                    .map(MiningSkillCardItem.Type::getId)
                                    .toArray(String[]::new);
                            String toolPrefix = toolNames.length > 1 ? "many_tools" : toolNames[0];

                            MutableComponent toolsList = Component.empty();
                            for (int i = 0; i < toolNames.length; i++) {
                                toolsList.append("[").append(Component.translatable("info.ultimine_addition.required_skill.%s".formatted(toolNames[i]))).append("]");
                                if (i != toolNames.length-1)
                                    toolsList.append(", ");
                            }
                            HoverEvent event = new HoverEvent(HoverEvent.Action.SHOW_TEXT, toolsList);
                            Style style = toolNames.length > 1 ? Style.EMPTY.withUnderlined(true).withHoverEvent(event) : Style.EMPTY;
                            requiredTool = Component.translatable("info.ultimine_addition.required_skill.%s".formatted(toolPrefix)).withStyle(style);
                        }
                    } else if (ItemUtils.isItemInHandPickaxe(player)) {
                        if (!player.hasEffect(BuiltInRegistries.MOB_EFFECT.getHolder(Registration.MINE_GO_JUICE_PICKAXE.getId()).orElseThrow())) {
                            requiredTool = Component.translatable("info.ultimine_addition.required_skill.pickaxe");
                        }
                    } else if (ItemUtils.isItemInHandAxe(player)) {
                        if (!player.hasEffect(BuiltInRegistries.MOB_EFFECT.getHolder(Registration.MINE_GO_JUICE_AXE.getId()).orElseThrow())) {
                            requiredTool = Component.translatable("info.ultimine_addition.required_skill.axe");
                        }
                    } else if (ItemUtils.isItemInHandShovel(player)) {
                        if (!player.hasEffect(BuiltInRegistries.MOB_EFFECT.getHolder(Registration.MINE_GO_JUICE_SHOVEL.getId()).orElseThrow())) {
                            requiredTool = Component.translatable("info.ultimine_addition.required_skill.shovel");
                        }
                    } else if (ItemUtils.isItemInHandHoe(player)) {
                        if (!player.hasEffect(BuiltInRegistries.MOB_EFFECT.getHolder(Registration.MINE_GO_JUICE_HOE.getId()).orElseThrow())) {
                            requiredTool = Component.translatable("info.ultimine_addition.required_skill.hoe");
                        }
                    } else if (!ItemUtils.isItemInHandTool(player)) {
                        requiredTool = Component.translatable("info.ultimine_addition.required_skill.all");
                    }

                    if (ConfigHandler.COMMON.PLAYSTYLE_MODE.get() != PlaystyleMode.LEGACY) {
                        if (requiredTool != null) {
                            player.displayClientMessage(MSG.withStyle(ChatFormatting.RED), false);
                            player.displayClientMessage(Component.literal("✖ ")
                                    .append(Component.translatable("info.ultimine_addition.required_skill", requiredTool))
                                    .withStyle(ChatFormatting.GRAY), false);
                        }
                    } else player.displayClientMessage(MSG.withStyle(ChatFormatting.RED), false);
                }
                isButtonPressed = true;
            }
        } else {
            isButtonPressed = false;
        }
    }

    public static List<MiningSkillCardItem.Type> getCustomCardTypes(Player player) {
        ItemStack stack = ItemUtils.getItemInHand(player, true);
        return MiningSkillCardItem.Type.TYPES.stream()
                .filter(MiningSkillCardItem.Type::isCustomType)
                .filter(type -> type.utilizeRequiredTools().contains(stack.getItem()))
                .toList();
    }

    private static boolean isPlayerHasCustomCardValidEffect(Player player) {
        List<MiningSkillCardItem.Type> types = getCustomCardTypes(player);
        for (MiningSkillCardItem.Type type : types) {
            Optional<Holder.Reference<MobEffect>> mobEffect = BuiltInRegistries.MOB_EFFECT.getHolder(MineGoJuiceEffect.getId(type));
            if (mobEffect.isEmpty()) continue;
            if (player.hasEffect(mobEffect.get()))
                return true;
        }
        return false;
    }

    public static int getMaxBlocks(ServerPlayer player) {
        if (!ConfigHandler.SERVER.CARD_TIER_BASED_MAX_BLOCKS.get()) {
            return FTBUltimine.ranksMod ? FTBRanksIntegration.getMaxBlocks(player) : MAX_BLOCKS.get();
        }

        List<MobEffectInstance> instances = new ArrayList<>(player.getActiveEffects().stream().filter(mobEffectInstance -> mobEffectInstance.getEffect().value() instanceof MineGoJuiceEffect).toList());
        if (!ServicePlatform.get().players().isPlayerUltimineCapable(player) && !instances.isEmpty()) {
            instances.sort(Comparator.comparingInt(MobEffectInstance::getAmplifier).reversed());

            if (ItemUtils.isItemInHandCustomCardValid(player)) {
                instances.removeIf(instance -> {
                    ItemStack stack = ItemUtils.getItemInHand(player, true);
                    if (stack.getItem() instanceof MiningSkillCardItem cardItem)
                        return ((MineGoJuiceEffect) instance.getEffect()).getType() != cardItem.getType();
                    return false;
                });
            } else if (ItemUtils.isItemInHandPickaxe(player)) {
                instances.removeIf(instance -> ((MineGoJuiceEffect) instance.getEffect().value()).getType() != MiningSkillCardItem.Type.PICKAXE);
            } else if (ItemUtils.isItemInHandAxe(player)) {
                instances.removeIf(instance -> ((MineGoJuiceEffect) instance.getEffect().value()).getType() != MiningSkillCardItem.Type.AXE);
            } else if (ItemUtils.isItemInHandShovel(player)) {
                instances.removeIf(instance -> ((MineGoJuiceEffect) instance.getEffect().value()).getType() != MiningSkillCardItem.Type.SHOVEL);
            } else if (ItemUtils.isItemInHandHoe(player)) {
                instances.removeIf(instance -> ((MineGoJuiceEffect) instance.getEffect().value()).getType() != MiningSkillCardItem.Type.HOE);
            }

            if (!instances.isEmpty()) {
                try {
                    return ConfigHandler.SERVER.CARD_TIER_MAX_BLOCKS.getValue(MiningSkillCardItem.Tier.fromInt(instances.getFirst().getAmplifier()+1));
                } catch (IllegalArgumentException ignored) {}
            }
        }
        return FTBUltimine.ranksMod ? FTBRanksIntegration.getMaxBlocks(player) : MAX_BLOCKS.get();
    }

    public static Shape getShape(String shapeId) {
        for (Shape shape : ShapeRegistryAccessor.getShapesList()) {
            if (shape.getName().equals(shapeId)) {
                return shape;
            }
        }
        return null;
    }

    public static List<Shape> getEnabledShapes() {
        return ShapeRegistryAccessor.getShapesList().stream()
                .filter(shape -> !ConfigHandler.SERVER.BLACKLISTED_SHAPES.get().contains(shape.getName()))
                .toList();
    }

    public static Shape getEnabledShapes(int idx) {
        if (idx < 0) {
            idx += getEnabledShapes().size();
        } else if (idx >= getEnabledShapes().size()) {
            idx -= getEnabledShapes().size();
        }
        return idx >= 0 && idx < getEnabledShapes().size() ? getEnabledShapes().get(idx) : ShapeRegistryAccessor.getDefaultShape();
    }

    public static boolean hasToolWithShape(Player player) {
        ItemStack stack = player.getMainHandItem();
        return stack.has(Registration.SELECTED_SHAPE_COMPONENT.get());
    }

    public static Shape getToolShape(Player player) {
        ItemStack stack = player.getMainHandItem();
        return Objects.requireNonNull(stack.get(Registration.SELECTED_SHAPE_COMPONENT.get())).shape();
    }
}
