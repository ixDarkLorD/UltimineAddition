package net.ixdarklord.ultimine_addition.core;

import net.ixdarklord.ultimine_addition.config.UAServerConfig;
import net.ixdarklord.ultimine_addition.config.PlaystyleModes;
import net.ixdarklord.coolcatcore.api.platform.Platform;
import dev.ftb.mods.ftbultimine.api.restriction.RestrictionHandler;
import dev.ftb.mods.ftbultimine.api.shape.Shape;
import dev.ftb.mods.ftbultimine.api.util.CanUltimineResult;
import dev.ftb.mods.ftbultimine.client.FTBUltimineClient;
import dev.ftb.mods.ftbultimine.config.FTBUltimineServerConfig;
import dev.ftb.mods.ftbultimine.shape.ShapeRegistry;
import net.ixdarklord.ultimine_addition.client.gui.hud.UltimineNoticeHud;
import net.ixdarklord.ultimine_addition.common.effect.MineGoJuiceEffect;
import net.ixdarklord.ultimine_addition.common.item.ModItems;
import net.ixdarklord.ultimine_addition.common.progression.UltimineNotice;
import net.ixdarklord.ultimine_addition.common.item.MiningSkillCardItem;
import net.ixdarklord.ultimine_addition.util.ItemUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Supplier;

import static net.ixdarklord.ultimine_addition.config.UAServerConfig.CARD_TIER_MAX_BLOCKS;

public class FTBUltimineIntegration implements RestrictionHandler {
    public static FTBUltimineIntegration INSTANCE = new FTBUltimineIntegration();
    // Set by the client setup; the client shape registry has no player of its own.
    public static Supplier<@Nullable Player> clientPlayer = () -> null;
    private static boolean isButtonPressed;

    public CanUltimineResult ultimineBlockReason(Player player) {
        return this.canUltimine(player) ? CanUltimineResult.ALLOWED : CanUltimineResult.prevent("info.ultimine_addition.ability_locked");
    }

    @Override
    public boolean canUltimine(Player player) {
        if (ServicePlatform.get().players().isPlayerUltimineCapable(player)) return true;
        if (PlaystyleModes.isLegacy()) return false;
        if (hasJuiceAccess(player)) return true;
        return hasCertificateShapes(player);
    }

    private static boolean hasJuiceAccess(Player player) {
        return hasJuiceForHeldTool(player) && ItemUtils.checkTargetedBlock(player);
    }

    // Mine-Go Juice (or a tier-up taste) for the tool in hand.
    private static boolean hasJuiceForHeldTool(Player player) {
        boolean result = false;
        if (isPlayerHasCustomCardValidEffect(player)) {
            if (ItemUtils.isItemInHandCustomCardValid(player)) result = true;
        }

        if (player.hasEffect(BuiltInRegistries.MOB_EFFECT.get(Registration.MINE_GO_JUICE_PICKAXE.getId()).orElseThrow())) {
            if (ItemUtils.isItemInHandPickaxe(player)) result = true;
        }
        if (player.hasEffect(BuiltInRegistries.MOB_EFFECT.get(Registration.MINE_GO_JUICE_AXE.getId()).orElseThrow())) {
            if (ItemUtils.isItemInHandAxe(player)) result = true;
        }
        if (player.hasEffect(BuiltInRegistries.MOB_EFFECT.get(Registration.MINE_GO_JUICE_SHOVEL.getId()).orElseThrow())) {
            if (ItemUtils.isItemInHandShovel(player)) result = true;
        }
        if (player.hasEffect(BuiltInRegistries.MOB_EFFECT.get(Registration.MINE_GO_JUICE_HOE.getId()).orElseThrow())) {
            if (ItemUtils.isItemInHandHoe(player)) result = true;
        }
        return result;
    }

    public static boolean isShapeCertificatesActive() {
        return UAServerConfig.SHAPE_CERTIFICATES.get() && !PlaystyleModes.isLegacy();
    }

    // Shapes learned from Shape Certificates for this tool, across every card type it belongs to (a paxel has all four).
    public static Set<Identifier> getLearnedShapes(Player player, ItemStack tool) {
        Set<Identifier> learned = new HashSet<>();
        for (MiningSkillCardItem.Type type : ItemUtils.getToolTypes(tool)) {
            learned.addAll(ServicePlatform.get().players().getUnlockedShapes(player, type.getId()));
        }
        return learned;
    }

    private static int getCertificateTier(Player player, ItemStack tool) {
        int tier = 0;
        for (MiningSkillCardItem.Type type : ItemUtils.getToolTypes(tool)) {
            tier = Math.max(tier, ServicePlatform.get().players().getAbilityData(player).getCertificateTier(type.getId()));
        }
        return tier;
    }

    private static boolean hasCertificateShapes(Player player) {
        if (!isShapeCertificatesActive()) return false;
        Set<Identifier> learned = getLearnedShapes(player, ItemUtils.getItemInHand(player, true));
        return getEnabledShapes().stream().anyMatch(shape -> learned.contains(shape.getName()));
    }

    // Every shape: the Miner Certificate for good, or a Mine-Go Juice for the tool in hand while it lasts.
    private static boolean hasAllShapes(Player player) {
        return ServicePlatform.get().players().isPlayerUltimineCapable(player) || hasJuiceForHeldTool(player);
    }

    // Ultimine only through the held tool's Shape Certificates; its block limit follows their tier.
    public static boolean isLimitedToCertificateShapes(Player player) {
        return isShapeCertificatesActive() && !hasAllShapes(player) && hasCertificateShapes(player);
    }

    // Not learned for this tool from a Shape Certificate (only usable with the Miner Certificate or a juice).
    public static boolean isShapeNotLearned(Player player, ItemStack tool, Shape shape) {
        return isShapeCertificatesActive()
                && !ServicePlatform.get().players().isPlayerUltimineCapable(player)
                && !getLearnedShapes(player, tool).contains(shape.getName());
    }

    // Without the Miner Certificate or a juice, only the shapes learned for the tool in hand; with none, a placeholder
    // so FTB Ultimine's shape HUD doesn't offer a shape the player can't use.
    public static List<Shape> getAvailableShapes(@Nullable Player player, ShapeRegistry registry) {
        List<Shape> enabled = getEnabledShapes(registry);
        if (player == null || !isShapeCertificatesActive() || hasAllShapes(player)) return enabled;
        Set<Identifier> learned = getLearnedShapes(player, ItemUtils.getItemInHand(player, true));
        List<Shape> available = enabled.stream().filter(shape -> learned.contains(shape.getName())).toList();
        return available.isEmpty() ? List.of(NoShape.INSTANCE) : available;
    }

    public static Shape getAvailableShape(@Nullable Player player, ShapeRegistry registry, int idx) {
        List<Shape> shapes = getAvailableShapes(player, registry);
        return shapes.isEmpty() ? getDefaultShape(registry) : shapes.get(Math.floorMod(idx, shapes.size()));
    }

    // The client registry serves the local player; the server one is only asked through FTBUltiminePlayerData.
    public static @Nullable Player getRegistryPlayer(ShapeRegistry registry) {
        return registry == ShapeRegistry.getInstance(true) ? clientPlayer.get() : null;
    }

    public static void keyEvent(Player player) {
        if (FTBUltimineClient.keyBindUltimine.isDown()) {
            if (!isButtonPressed) {
                if (ServicePlatform.get().players().isPlayerUltimineCapable(player) || hasCertificateShapes(player) || hasJuiceForHeldTool(player)) {
                    // Ultimine works, so whatever the notice says is no longer in the way.
                    UltimineNoticeHud.INSTANCE.dismiss();
                } else {
                    showLockedNotice(player);
                }
                isButtonPressed = true;
            }
        } else {
            isButtonPressed = false;
        }
    }

    private static void showLockedNotice(Player player) {
        Component title = UltimineNotice.actionsTitle();
        Component status = Component.translatable("info.ultimine_addition.notice.locked").withStyle(ChatFormatting.RED);
        ItemStack held = ItemUtils.getItemInHand(player, true);
        if (PlaystyleModes.isLegacy()) {
            UltimineNoticeHud.INSTANCE.show(new UltimineNotice(UltimineNotice.Kind.ACTION, title,
                    List.of(status, Component.translatable("info.ultimine_addition.notice.locked.legacy")), ModItems.MINER_CERTIFICATE.getDefaultInstance()));
            return;
        }

        // The card type (tool) whose juice would help, or null when the item in hand isn't a tool at all.
        MiningSkillCardItem.Type type = null;
        Component requiredTool = null;
        if (ItemUtils.isItemInHandCustomCardValid(player)) {
            if (!isPlayerHasCustomCardValidEffect(player)) {
                MutableComponent tools = Component.empty();
                List<MiningSkillCardItem.Type> types = getCustomCardTypes(player);
                for (int i = 0; i < types.size(); i++) {
                    if (i > 0) tools.append(", ");
                    tools.append(Component.translatable("info.ultimine_addition.required_skill.%s".formatted(types.get(i).getId())));
                }
                requiredTool = tools;
                type = types.isEmpty() ? null : types.getFirst();
            }
        } else if (ItemUtils.isItemInHandPickaxe(player)) {
            if (!player.hasEffect(BuiltInRegistries.MOB_EFFECT.get(Registration.MINE_GO_JUICE_PICKAXE.getId()).orElseThrow())) type = MiningSkillCardItem.Type.PICKAXE;
        } else if (ItemUtils.isItemInHandAxe(player)) {
            if (!player.hasEffect(BuiltInRegistries.MOB_EFFECT.get(Registration.MINE_GO_JUICE_AXE.getId()).orElseThrow())) type = MiningSkillCardItem.Type.AXE;
        } else if (ItemUtils.isItemInHandShovel(player)) {
            if (!player.hasEffect(BuiltInRegistries.MOB_EFFECT.get(Registration.MINE_GO_JUICE_SHOVEL.getId()).orElseThrow())) type = MiningSkillCardItem.Type.SHOVEL;
        } else if (ItemUtils.isItemInHandHoe(player)) {
            if (!player.hasEffect(BuiltInRegistries.MOB_EFFECT.get(Registration.MINE_GO_JUICE_HOE.getId()).orElseThrow())) type = MiningSkillCardItem.Type.HOE;
        } else if (!ItemUtils.isItemInHandTool(player)) {
            // Not a tool: only the Miner Certificate covers it.
            List<Component> lines = List.of(status,
                    Component.translatable("info.ultimine_addition.required_skill", Component.translatable("info.ultimine_addition.required_skill.all").withStyle(ChatFormatting.YELLOW)).withStyle(ChatFormatting.GRAY),
                    Component.translatable("info.ultimine_addition.notice.locked.hint_all"));
            UltimineNoticeHud.INSTANCE.show(new UltimineNotice(UltimineNotice.Kind.ACTION, title, lines, ModItems.MINER_CERTIFICATE.getDefaultInstance()));
            return;
        }
        if (type == null) return;
        if (requiredTool == null) requiredTool = Component.translatable("info.ultimine_addition.required_skill." + type.getId());

        // Names that tool's own Mine-Go Juice.
        MobEffect juice = BuiltInRegistries.MOB_EFFECT.getValue(MineGoJuiceEffect.getId(type));
        Component juiceName = (juice == null ? Component.translatable("info.ultimine_addition.notice.locked.juice") : juice.getDisplayName()).copy().withStyle(ChatFormatting.AQUA);
        List<Component> lines = List.of(status,
                Component.translatable("info.ultimine_addition.required_skill", requiredTool.copy().withStyle(ChatFormatting.YELLOW)).withStyle(ChatFormatting.GRAY),
                Component.translatable(isShapeCertificatesActive() ? "info.ultimine_addition.notice.locked.hint" : "info.ultimine_addition.notice.locked.hint_no_shapes", juiceName));
        UltimineNoticeHud.INSTANCE.show(new UltimineNotice(UltimineNotice.Kind.ACTION, title, lines, held.isEmpty() ? ModItems.MINER_CERTIFICATE.getDefaultInstance() : held.copyWithCount(1)));
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
            Optional<Holder.Reference<MobEffect>> mobEffect = BuiltInRegistries.MOB_EFFECT.get(MineGoJuiceEffect.getId(type));
            if (mobEffect.isEmpty()) continue;
            if (player.hasEffect(mobEffect.get()))
                return true;
        }
        return false;
    }

    public static int getMaxBlocks(ServerPlayer player) {
        if (UAServerConfig.CARD_TIER_BASED_MAX_BLOCKS.get()) {
            List<MobEffectInstance> instances = new ArrayList<>(player.getActiveEffects().stream().filter((mobEffectInstance) -> mobEffectInstance.getEffect() instanceof MineGoJuiceEffect).toList());
            if (!ServicePlatform.get().players().isPlayerUltimineCapable(player) && !instances.isEmpty()) {
                instances.sort(Comparator.comparingInt(MobEffectInstance::getAmplifier).reversed());
                if (ItemUtils.isItemInHandCustomCardValid(player)) {
                    instances.removeIf((instance) -> {
                        ItemStack stack = ItemUtils.getItemInHand(player, true);
                        Item item = stack.getItem();
                        if (item instanceof MiningSkillCardItem cardItem) {
                            return ((MineGoJuiceEffect) instance.getEffect()).getType() != cardItem.getType();
                        } else {
                            return false;
                        }
                    });
                } else if (ItemUtils.isItemInHandPickaxe(player)) {
                    instances.removeIf((instance) -> ((MineGoJuiceEffect) instance.getEffect()).getType() != MiningSkillCardItem.Type.PICKAXE);
                } else if (ItemUtils.isItemInHandAxe(player)) {
                    instances.removeIf((instance) -> ((MineGoJuiceEffect) instance.getEffect()).getType() != MiningSkillCardItem.Type.AXE);
                } else if (ItemUtils.isItemInHandShovel(player)) {
                    instances.removeIf((instance) -> ((MineGoJuiceEffect) instance.getEffect()).getType() != MiningSkillCardItem.Type.SHOVEL);
                } else if (ItemUtils.isItemInHandHoe(player)) {
                    instances.removeIf((instance) -> ((MineGoJuiceEffect) instance.getEffect()).getType() != MiningSkillCardItem.Type.HOE);
                }

                if (!instances.isEmpty()) {
                    try {
                        return CARD_TIER_MAX_BLOCKS.getValue(MiningSkillCardItem.Tier.fromInt(Math.min(instances.getFirst().getAmplifier() + 1, CARD_TIER_MAX_BLOCKS.getDefaultMapValue().size() - 1)));
                    } catch (IllegalArgumentException ignored) {
                    }
                }
            }

            if (isLimitedToCertificateShapes(player)) {
                int tier = getCertificateTier(player, ItemUtils.getItemInHand(player, true));
                try {
                    return CARD_TIER_MAX_BLOCKS.getValue(MiningSkillCardItem.Tier.fromInt(Mth.clamp(tier, 1, CARD_TIER_MAX_BLOCKS.getDefaultMapValue().size())));
                } catch (IllegalArgumentException ignored) {
                }
            }
        }

        return FTBUltimineServerConfig.getMaxBlocks(player);
    }

    // FTB Ultimine keeps separate client/server shape registries (with distinct Shape instances). The no-arg
    // helpers use the server registry, which only a server fills: a client connected to a dedicated server (tooltips,
    // JEI) reads the client registry instead.
    private static ShapeRegistry defaultRegistry() {
        ShapeRegistry server = ShapeRegistry.getInstance(false);
        if (!Platform.isClient() || !getShapesList(server).isEmpty()) return server;
        return ShapeRegistry.getInstance(true);
    }

    public static List<Shape> getShapesList() {
        return getShapesList(defaultRegistry());
    }

    public static List<Shape> getShapesList(ShapeRegistry registry) {
        Object instance = registry;
        return ((ShapeRegistryAccessor) instance).getShapesList();
    }

    public static Shape getDefaultShape() {
        return getDefaultShape(defaultRegistry());
    }

    public static Shape getDefaultShape(ShapeRegistry registry) {
        Object instance = registry;
        return ((ShapeRegistryAccessor) instance).getDefaultShape();
    }

    public static List<Shape> getEnabledShapes() {
        return getEnabledShapes(defaultRegistry());
    }

    public static List<Shape> getEnabledShapes(ShapeRegistry registry) {
        return getShapesList(registry).stream()
                .filter(shape -> !UAServerConfig.BLACKLISTED_SHAPES.get().contains(shape.getName().toString()))
                .toList();
    }

    public static boolean isShapeEnabled(Identifier shapeId) {
        return !UAServerConfig.BLACKLISTED_SHAPES.get().contains(shapeId.toString()) && getShape(shapeId) != null;
    }

    public static Shape getShape(Identifier shapeId) {
        for (Shape shape : getShapesList()) {
            if (shape.getName().equals(shapeId)) {
                return shape;
            }
        }
        return null;
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
