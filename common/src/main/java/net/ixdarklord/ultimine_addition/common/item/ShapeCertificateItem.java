package net.ixdarklord.ultimine_addition.common.item;

import dev.ftb.mods.ftbultimine.api.shape.Shape;
import net.ixdarklord.coolcatcore.api.utils.ComponentHelper;
import net.ixdarklord.ultimine_addition.client.handler.ClientHandler;
import net.ixdarklord.ultimine_addition.common.data.item.ShapeCertificateData;
import net.ixdarklord.ultimine_addition.config.UAServerConfig;
import net.ixdarklord.ultimine_addition.core.FTBUltimineIntegration;
import net.ixdarklord.ultimine_addition.core.ServicePlatform;
import net.minecraft.ChatFormatting;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

// Unlocks its tier's Ultimine shapes for one tool: the Mining Skill Card type it was earned with.
public class ShapeCertificateItem extends ModernItem implements net.ixdarklord.coolcatcore.api.item.DecoratedItem {
    // CoolCatLib takes it on the client, the first time a certificate is drawn: a data pack tool's icon on the page.
    @Override
    public void registerDecorators(java.util.function.Consumer<net.ixdarklord.coolcatcore.api.client.gui.ItemDecorator> registrar) {
        registrar.accept(net.ixdarklord.ultimine_addition.client.renderer.item.CertificateToolIcon.INSTANCE);
    }

    private final MiningSkillCardItem.Tier tier;

    public ShapeCertificateItem(MiningSkillCardItem.Tier tier, Properties properties) {
        super(properties, ComponentType.ABILITY);
        this.tier = tier;
    }

    public MiningSkillCardItem.Tier getTier() {
        return this.tier;
    }

    public static List<ShapeCertificateItem> all() {
        return List.of(ModItems.SHAPE_CERTIFICATE_NOVICE, ModItems.SHAPE_CERTIFICATE_APPRENTICE, ModItems.SHAPE_CERTIFICATE_ADEPT);
    }

    // The certificate a card hands out when it reaches this tier; Mastered hands out an Adept one.
    public static @Nullable ShapeCertificateItem forTier(MiningSkillCardItem.Tier tier) {
        return switch (tier) {
            case Novice -> ModItems.SHAPE_CERTIFICATE_NOVICE;
            case Apprentice -> ModItems.SHAPE_CERTIFICATE_APPRENTICE;
            case Adept, Mastered -> ModItems.SHAPE_CERTIFICATE_ADEPT;
            default -> null;
        };
    }

    public ItemStack create(MiningSkillCardItem.Type type) {
        ItemStack stack = this.getDefaultInstance();
        stack.set(ShapeCertificateData.DATA_COMPONENT, new ShapeCertificateData(type.getId()));
        return stack;
    }

    public ItemStack create(MiningSkillCardItem.Type type, Identifier shape) {
        ItemStack stack = this.create(type);
        stack.set(ShapeCertificateData.SHAPE_COMPONENT, shape);
        return stack;
    }

    private static final MiningSkillCardItem.Tier[] LIST_TIERS = {MiningSkillCardItem.Tier.Novice, MiningSkillCardItem.Tier.Apprentice, MiningSkillCardItem.Tier.Adept};

    // The shapes a card can pick from when it claims the certificate of claimTier: that tier's list plus every earlier
    // tier's shapes not picked yet, so a shape passed over stays available later (Mastered: anything left). Shapes
    // already learned for the tool, and blacklisted ones, are left out.
    public static List<Identifier> claimPool(Player player, String tool, MiningSkillCardItem.Tier claimTier) {
        Set<Identifier> learned = ServicePlatform.get().players().getUnlockedShapes(player, tool);
        List<Identifier> pool = new ArrayList<>();
        for (MiningSkillCardItem.Tier tier : LIST_TIERS) {
            if (tier.getValue() > claimTier.getValue()) break;
            for (Identifier shape : tierList(tier)) {
                if (!pool.contains(shape) && !learned.contains(shape) && FTBUltimineIntegration.isShapeEnabled(shape)) pool.add(shape);
            }
        }
        return pool;
    }

    // The tier whose list offers this shape, or null when no list does.
    public static MiningSkillCardItem.@Nullable Tier shapeTier(Identifier shape) {
        for (MiningSkillCardItem.Tier tier : LIST_TIERS) {
            if (tierList(tier).contains(shape)) return tier;
        }
        return null;
    }

    // Whether the stack fits the player's main inventory without anything dropping.
    public static boolean hasRoomFor(Player player, ItemStack stack) {
        return player.getInventory().getSlotWithRemainingSpace(stack) >= 0 || player.getInventory().getFreeSlot() >= 0;
    }

    public static Component toolName(String tool) {
        // A data pack card type names its own tool.
        MiningSkillCardItem.Type type = MiningSkillCardItem.Type.byId(tool);
        // A tool no card type has (its data pack is gone) is "Unknown", like its generic card.
        return type != null ? type.displayName() : Component.translatable("item.ultimine_addition.mining_skill_card_generic.unknown");
    }

    private static List<? extends String> listedIds(MiningSkillCardItem.Tier tier) {
        return switch (tier) {
            case Novice -> UAServerConfig.NOVICE_CERTIFICATE_SHAPES.get();
            case Apprentice -> UAServerConfig.APPRENTICE_CERTIFICATE_SHAPES.get();
            case Adept -> UAServerConfig.ADEPT_CERTIFICATE_SHAPES.get();
            default -> List.of();
        };
    }

    // The shapes a tier's list offers (from the config, plus other mods' unlisted shapes for the chosen tier).
    public static List<Identifier> tierList(MiningSkillCardItem.Tier tier) {
        List<Identifier> shapes = new ArrayList<>();
        for (String id : listedIds(tier)) {
            Identifier shape = Identifier.tryParse(id);
            if (shape != null && FTBUltimineIntegration.getShape(shape) != null) shapes.add(shape);
        }
        if (UAServerConfig.EXTRA_SHAPES_CERTIFICATE.get().tier() == tier) {
            for (Identifier extra : getUnlistedShapes()) {
                if (!shapes.contains(extra)) shapes.add(extra);
            }
        }
        return shapes;
    }

    // What this certificate teaches: its one shape (older certificates without one: their tier's whole list).
    public List<Identifier> getShapeIds(ItemStack stack) {
        Identifier shape = ShapeCertificateData.getShape(stack);
        if (shape != null) return FTBUltimineIntegration.getShape(shape) != null ? List.of(shape) : List.of();
        return tierList(this.tier);
    }

    // Shapes from other mods (FTB Ultimine plugins) that no certificate lists; FTB Ultimine's own unlisted
    // shapes (Shapeless) stay with the Miner Certificate.
    public static List<Identifier> getUnlistedShapes() {
        List<String> listed = new ArrayList<>();
        for (ShapeCertificateItem certificate : all()) listed.addAll(listedIds(certificate.tier));
        return FTBUltimineIntegration.getShapesList().stream()
                .map(Shape::getName)
                .filter(id -> !id.getNamespace().equals("ftbultimine"))
                .filter(id -> !listed.contains(id.toString()))
                .toList();
    }

    @Override
    public @NotNull Component getName(@NotNull ItemStack stack) {
        String tool = ShapeCertificateData.getTool(stack);
        Identifier shape = ShapeCertificateData.getShape(stack);
        if (tool != null && shape != null) return Component.translatable("item.ultimine_addition.shape_certificate.named", shapeName(shape), toolName(tool));
        Component name = super.getName(stack);
        return tool == null ? name : Component.translatable("item.ultimine_addition.shape_certificate.for_tool", name, toolName(tool));
    }

    @Override
    public @NotNull InteractionResult use(@NotNull Level level, @NotNull Player player, @NotNull InteractionHand usedHand) {
        if (level.isClientSide()) return InteractionResult.SUCCESS;
        if (!(player instanceof ServerPlayer serverPlayer)) return InteractionResult.PASS;
        ItemStack stack = player.getItemInHand(usedHand);
        ServicePlatform.Players players = ServicePlatform.get().players();

        if (!FTBUltimineIntegration.isShapeCertificatesActive()) {
            player.sendOverlayMessage(Component.translatable("info.ultimine_addition.shape_certificate.disabled").withStyle(ChatFormatting.RED));
            return InteractionResult.FAIL;
        }
        String tool = ShapeCertificateData.getTool(stack);
        if (tool == null) {
            player.sendOverlayMessage(Component.translatable("info.ultimine_addition.shape_certificate.unbound").withStyle(ChatFormatting.RED));
            return InteractionResult.FAIL;
        }
        if (players.isPlayerUltimineCapable(player)) {
            player.sendOverlayMessage(Component.translatable("info.ultimine_addition.shape_certificate.all_known").withStyle(ChatFormatting.RED));
            return InteractionResult.FAIL;
        }

        Set<Identifier> unlocked = players.getUnlockedShapes(player, tool);
        List<Identifier> newShapes = this.getShapeIds(stack).stream().filter(id -> !unlocked.contains(id)).toList();
        boolean raisesTier = this.tier.getValue() > players.getAbilityData(player).getCertificateTier(tool);
        if (newShapes.isEmpty() && !raisesTier) {
            player.sendOverlayMessage(Component.translatable("info.ultimine_addition.shape_certificate.known", toolName(tool)).withStyle(ChatFormatting.RED));
            return InteractionResult.FAIL;
        }

        players.unlockShapes(player, tool, newShapes, this.tier.getValue());
        if (!player.isCreative()) stack.shrink(1);

        MutableComponent names = Component.empty();
        for (int i = 0; i < newShapes.size(); i++) {
            if (i > 0) names.append(", ");
            names.append(shapeName(newShapes.get(i)).withStyle(ChatFormatting.YELLOW));
        }
        Component toolName = toolName(tool).copy().withStyle(ChatFormatting.AQUA);
        player.sendSystemMessage(Component.literal("✦ ").withStyle(ChatFormatting.GOLD)
                .append(newShapes.isEmpty()
                        ? Component.translatable("info.ultimine_addition.shape_certificate.tier_raised", toolName, this.tier.getDisplayName())
                        : Component.translatable("info.ultimine_addition.shape_certificate.learned", toolName, names))
                .withStyle(ChatFormatting.GRAY));
        serverPlayer.level().sendParticles(ParticleTypes.ENCHANT, player.getX(), player.getY() + 1.0, player.getZ(), 40, 0.5, 0.8, 0.5, 0.6);
        serverPlayer.level().sendParticles(ParticleTypes.HAPPY_VILLAGER, player.getX(), player.getY() + 1.0, player.getZ(), 12, 0.6, 0.6, 0.6, 0.02);
        level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.PLAYER_LEVELUP, player.getSoundSource(), 0.6F, 1.4F);
        level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.BOOK_PAGE_TURN, player.getSoundSource(), 1.0F, 1.0F);
        return InteractionResult.SUCCESS_SERVER;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay display, Consumer<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        super.appendHoverText(stack, context, display, tooltipComponents, tooltipFlag);
        String tool = ShapeCertificateData.getTool(stack);
        if (tool == null) {
            tooltipComponents.accept(Component.translatable("info.ultimine_addition.shape_certificate.unbound").withStyle(ChatFormatting.RED));
            return;
        }
        Player player = ClientHandler.getPlayer();
        Set<Identifier> unlocked = player == null ? Set.of() : ServicePlatform.get().players().getUnlockedShapes(player, tool);
        boolean master = player != null && ServicePlatform.get().players().isPlayerUltimineCapable(player);

        tooltipComponents.accept(Component.literal("• ").withStyle(ChatFormatting.DARK_GRAY)
                .append(Component.translatable("tooltip.ultimine_addition.skill_card.tier", this.tier.getDisplayName()).withStyle(ChatFormatting.GRAY)));
        tooltipComponents.accept(Component.translatable("tooltip.ultimine_addition.shape_certificate.unlocks",
                toolName(tool).copy().withStyle(ChatFormatting.AQUA)).withStyle(ChatFormatting.GRAY));
        for (Identifier id : this.getShapeIds(stack)) {
            boolean known = master || unlocked.contains(id);
            tooltipComponents.accept(Component.literal(known ? " ✔ " : " • ").withStyle(known ? ChatFormatting.GREEN : ChatFormatting.DARK_GRAY)
                    .append(shapeName(id).withStyle(known ? ChatFormatting.DARK_GREEN : ChatFormatting.YELLOW)));
        }
        if (!FTBUltimineIntegration.isShapeCertificatesActive()) {
            tooltipComponents.accept(Component.translatable("info.ultimine_addition.shape_certificate.disabled").withStyle(ChatFormatting.RED));
        }

        if (this.isShiftButtonNotPressed(tooltipComponents)) return;
        Component info = Component.translatable("tooltip.ultimine_addition.shape_certificate.info", toolName(tool), this.tier.getDisplayName()).withStyle(ChatFormatting.GRAY);
        ComponentHelper.splitComponent(info, getSplitterLength()).forEach(tooltipComponents);
    }

    private static MutableComponent shapeName(Identifier id) {
        Shape shape = FTBUltimineIntegration.getShape(id);
        return shape == null ? Component.literal(id.toString()) : shape.getDisplayName().copy();
    }
}
