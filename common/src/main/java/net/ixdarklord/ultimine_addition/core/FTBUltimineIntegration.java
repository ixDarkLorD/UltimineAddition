package net.ixdarklord.ultimine_addition.core;

import net.ixdarklord.ultimine_addition.config.UAServerConfig;
import net.ixdarklord.ultimine_addition.config.PlaystyleModes;
import dev.ftb.mods.ftbultimine.integration.FTBUltiminePlugin;
import dev.ftb.mods.ftbultimine.shape.Shape;
import dev.ftb.mods.ftbultimine.client.FTBUltimineClient;
import dev.ftb.mods.ftbultimine.config.FTBUltimineServerConfig;
import net.ixdarklord.ultimine_addition.client.gui.hud.UltimineNoticeHud;
import net.ixdarklord.ultimine_addition.common.effect.MineGoJuiceEffect;
import net.ixdarklord.ultimine_addition.common.item.ModItems;
import net.ixdarklord.ultimine_addition.common.progression.UltimineNotice;
import net.ixdarklord.ultimine_addition.common.item.MiningSkillCardItem;
import net.ixdarklord.ultimine_addition.mixin.ShapeRegistryAccessor;
import net.ixdarklord.ultimine_addition.util.ItemUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
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

// FTB Ultimine 2001 has no restriction handlers: the same check is an FTBUltiminePlugin (registered in
// UltimineAdditionConstructor).
public class FTBUltimineIntegration implements FTBUltiminePlugin {
    public static FTBUltimineIntegration INSTANCE = new FTBUltimineIntegration();
    // Set by the client setup: the local player while on the client thread (FTB Ultimine 2001's one static shape
    // registry serves both the client and the integrated server).
    public static Supplier<@Nullable Player> clientPlayer = () -> null;
    private static boolean isButtonPressed;

    public @Nullable Component ultimineBlockReason(Player player) {
        return this.canUltimine(player) ? null : Component.translatable("info.ultimine_addition.ability_locked");
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

        if (player.hasEffect(Registration.MINE_GO_JUICE_PICKAXE.get())) {
            if (ItemUtils.isItemInHandPickaxe(player)) result = true;
        }
        if (player.hasEffect(Registration.MINE_GO_JUICE_AXE.get())) {
            if (ItemUtils.isItemInHandAxe(player)) result = true;
        }
        if (player.hasEffect(Registration.MINE_GO_JUICE_SHOVEL.get())) {
            if (ItemUtils.isItemInHandShovel(player)) result = true;
        }
        if (player.hasEffect(Registration.MINE_GO_JUICE_HOE.get())) {
            if (ItemUtils.isItemInHandHoe(player)) result = true;
        }
        return result;
    }

    public static boolean isShapeCertificatesActive() {
        return UAServerConfig.SHAPE_CERTIFICATES.get() && !PlaystyleModes.isLegacy();
    }

    // Shapes learned from Shape Certificates for this tool, across every card type it belongs to (a paxel has all four).
    public static Set<ResourceLocation> getLearnedShapes(Player player, ItemStack tool) {
        Set<ResourceLocation> learned = new HashSet<>();
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
        Set<ResourceLocation> learned = getLearnedShapes(player, ItemUtils.getItemInHand(player, true));
        return getEnabledShapes().stream().anyMatch(shape -> learned.contains(shapeId(shape)));
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
                && !getLearnedShapes(player, tool).contains(shapeId(shape));
    }

    // Without the Miner Certificate or a juice, only the shapes learned for the tool in hand; with none, a placeholder
    // so FTB Ultimine's shape HUD doesn't offer a shape the player can't use.
    public static List<Shape> getAvailableShapes(@Nullable Player player) {
        List<Shape> enabled = getEnabledShapes();
        if (player == null || !isShapeCertificatesActive() || hasAllShapes(player)) return enabled;
        Set<ResourceLocation> learned = getLearnedShapes(player, ItemUtils.getItemInHand(player, true));
        List<Shape> available = enabled.stream().filter(shape -> learned.contains(shapeId(shape))).toList();
        return available.isEmpty() ? List.of(NoShape.INSTANCE) : available;
    }

    public static Shape getAvailableShape(@Nullable Player player, int idx) {
        List<Shape> shapes = getAvailableShapes(player);
        return shapes.isEmpty() ? getDefaultShape() : shapes.get(Math.floorMod(idx, shapes.size()));
    }

    // On the client thread the registry serves the local player; the server only asks it through FTBUltiminePlayerData.
    public static @Nullable Player getRegistryPlayer() {
        return clientPlayer.get();
    }

    public static void keyEvent(Player player) {
        if (FTBUltimineClient.keyBinding.isDown()) {
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
                    tools.append(types.get(i).displayName());
                }
                requiredTool = tools;
                type = types.isEmpty() ? null : types.get(0);
            }
        } else if (ItemUtils.isItemInHandPickaxe(player)) {
            if (!player.hasEffect(Registration.MINE_GO_JUICE_PICKAXE.get())) type = MiningSkillCardItem.Type.PICKAXE;
        } else if (ItemUtils.isItemInHandAxe(player)) {
            if (!player.hasEffect(Registration.MINE_GO_JUICE_AXE.get())) type = MiningSkillCardItem.Type.AXE;
        } else if (ItemUtils.isItemInHandShovel(player)) {
            if (!player.hasEffect(Registration.MINE_GO_JUICE_SHOVEL.get())) type = MiningSkillCardItem.Type.SHOVEL;
        } else if (ItemUtils.isItemInHandHoe(player)) {
            if (!player.hasEffect(Registration.MINE_GO_JUICE_HOE.get())) type = MiningSkillCardItem.Type.HOE;
        } else if (!ItemUtils.isItemInHandTool(player)) {
            // Not a tool: only the Miner Certificate covers it.
            List<Component> lines = List.of(status,
                    Component.translatable("info.ultimine_addition.required_skill", Component.translatable("info.ultimine_addition.required_skill.all").withStyle(ChatFormatting.YELLOW)).withStyle(ChatFormatting.GRAY),
                    Component.translatable("info.ultimine_addition.notice.locked.hint_all"));
            UltimineNoticeHud.INSTANCE.show(new UltimineNotice(UltimineNotice.Kind.ACTION, title, lines, ModItems.MINER_CERTIFICATE.getDefaultInstance()));
            return;
        }
        if (type == null) return;
        if (requiredTool == null) requiredTool = type.displayName();

        // Names that tool's own Mine-Go Juice.
        Component juiceName = MineGoJuiceEffect.juiceName(type).withStyle(ChatFormatting.AQUA);
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
            if (MineGoJuiceEffect.has(player, type)) return true;
        }
        return false;
    }

    public static int getMaxBlocks(ServerPlayer player) {
        if (UAServerConfig.CARD_TIER_BASED_MAX_BLOCKS.get()) {
            List<MobEffectInstance> instances = new ArrayList<>(player.getActiveEffects().stream().filter((mobEffectInstance) -> mobEffectInstance.getEffect() instanceof MineGoJuiceEffect).toList());
            if (!ServicePlatform.get().players().isPlayerUltimineCapable(player) && !instances.isEmpty()) {
                instances.sort(Comparator.comparingInt(MobEffectInstance::getAmplifier).reversed());
                // Only the juices of the tool in hand count (a data pack type's generic juice is the player's).
                List<MiningSkillCardItem.Type> held = ItemUtils.getToolTypes(ItemUtils.getItemInHand(player, true));
                instances.removeIf(instance -> !held.contains(MineGoJuiceEffect.typeOf(instance.getEffect(), player)));

                if (!instances.isEmpty()) {
                    try {
                        return CARD_TIER_MAX_BLOCKS.getValue(MiningSkillCardItem.Tier.fromInt(Math.min(instances.get(0).getAmplifier() + 1, CARD_TIER_MAX_BLOCKS.getDefaultMapValue().size() - 1)));
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

    // FTB Ultimine 2001's shape registry is static, shared by the client and the (integrated) server.
    public static List<Shape> getShapesList() {
        return ShapeRegistryAccessor.getShapesList$UA();
    }

    public static Shape getDefaultShape() {
        return ShapeRegistryAccessor.getDefaultShape$UA();
    }

    // FTB Ultimine 2001 names its shapes with plain strings ("small_tunnel"); newer versions and this mod's configs use
    // ids ("ftbultimine:small_tunnel"). Names without a namespace are FTB Ultimine's.
    public static ResourceLocation shapeId(Shape shape) {
        if (shape instanceof NoShape) return NoShape.ID;
        String name = shape.getName();
        ResourceLocation id = name.contains(":") ? ResourceLocation.tryParse(name) : ResourceLocation.tryBuild("ftbultimine", name);
        return id != null ? id : new ResourceLocation("ftbultimine", "invalid");
    }

    // FTB Ultimine 2001's shapes have no display name of their own; its HUD translates "ftbultimine.shape.<name>".
    public static MutableComponent shapeName(Shape shape) {
        if (shape instanceof net.ixdarklord.ultimine_addition.common.data.shape.DataShape dataShape) return dataShape.getDisplayName();
        return Component.translatable("ftbultimine.shape." + shape.getName());
    }

    public static List<Shape> getEnabledShapes() {
        return getShapesList().stream()
                .filter(shape -> !UAServerConfig.BLACKLISTED_SHAPES.get().contains(shapeId(shape).toString()))
                .toList();
    }

    public static boolean isShapeEnabled(ResourceLocation shapeId) {
        return !UAServerConfig.BLACKLISTED_SHAPES.get().contains(shapeId.toString()) && getShape(shapeId) != null;
    }

    public static Shape getShape(ResourceLocation shapeId) {
        for (Shape shape : getShapesList()) {
            if (shapeId(shape).equals(shapeId)) {
                return shape;
            }
        }
        return null;
    }


    public static boolean hasToolWithShape(Player player) {
        ItemStack stack = player.getMainHandItem();
        return Registration.SELECTED_SHAPE_COMPONENT.has(stack);
    }

    public static Shape getToolShape(Player player) {
        ItemStack stack = player.getMainHandItem();
        return Objects.requireNonNull(Registration.SELECTED_SHAPE_COMPONENT.get(stack)).shape();
    }
}
