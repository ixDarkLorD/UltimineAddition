package net.ixdarklord.ultimine_addition.client.renderer.item;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.ixdarklord.ultimine_addition.common.data.item.MiningSkillCardData;
import net.ixdarklord.ultimine_addition.common.item.MiningSkillCardItem;
import net.minecraft.client.multiplayer.ClientLevel;
import net.ixdarklord.ultimine_addition.core.FTBUltimineAddition;
import net.minecraft.client.renderer.item.CuboidItemModelWrapper;
import net.minecraft.client.renderer.item.ItemModel;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.item.ItemModels;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.resources.model.ResolvableModel;
import net.minecraft.client.resources.model.cuboid.ItemTransform;
import net.minecraft.client.resources.model.cuboid.ItemTransforms;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.ItemOwner;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;
import org.joml.Matrix4fc;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

// The Mining Skill Card's model: one per tier, picked from the card's tier (a data component vanilla's select
// properties can't read). Item definitions list them under "classic"; any other key, like the removed "custom"
// renderer's, is ignored, so older resource packs still load.
// Over the tier's model comes the card's tool: the textures leave the card's plate empty, and the tool's own item model
// is drawn there, small, on both faces of the card (the netherite tool for the built-in cards, a data pack card type's
// "icon"). So the tool shows wherever the card does: in a slot, in the hand, on the ground.
// The tier's stars and the potion points' pips are layers too (small flat models, models/item/card_overlay), standing
// out of the card's faces like the tool does.
public class MiningSkillCardItemModel implements ItemModel {
    // The plate on the card texture (32x32): its middle, and the icon's width, as fractions of the model.
    private static final float PLATE_X = 16.0F / 32.0F;
    private static final float PLATE_Y = 1.0F - 16.5F / 32.0F;
    private static final float ICON_SCALE = 14.0F / 32.0F;
    // A flat item is a 1/16 thick slab around the model's middle: the icon sits on the card's front face.
    private static final float CARD_FRONT = 8.5F / 16.0F;
    private static final float SLAB_BACK = 7.5F / 16.0F;

    private final Map<MiningSkillCardItem.Tier, ItemModel> tiers;
    // The icon's display transforms: the card's (those of a flat item) with the plate's place and size worked in, so
    // the icon moves with the card. The size has to be part of the item transform: a layer's local transform that
    // scales makes the game work the normals out again from the whole pose (a GUI's flipped Y included), and the icon
    // would be lit from the wrong side.
    private final Map<ItemDisplayContext, ItemTransform> transforms = new EnumMap<>(ItemDisplayContext.class);
    private final Matrix4fc front;
    private final Matrix4fc back;
    private final Map<String, ItemStack> icons = new HashMap<>();
    private final Overlays overlays;
    private final Matrix4fc transformation;
    private final Matrix4f scratch = new Matrix4f();
    // How far a layer stands out of the card's face: the stars and the filled pips like the tool, the empty pips
    // (sockets) barely.
    private static final float RAISED = 0.4F / 16.0F, FLUSH = 0.02F / 16.0F;
    private static final float TEXEL = 1.0F / 32.0F;
    // The potion points' strip under the card's plate, and the stars' pitch, in the texture's pixels. The overlay
    // textures hold the first star and the first pip in their places on the card.
    private static final int STRIP_LEFT = 8, STRIP_WIDTH = 16, STAR_PITCH = 5;

    // stars: by tier (none for an unlearned card). pips: by width (4, 2 and 1 pixels: indices 0, 1, 2).
    private record Overlays(Map<MiningSkillCardItem.Tier, ItemModel> stars, ItemModel[] pips, ItemModel[] emptyPips, ItemModel question) {}

    private MiningSkillCardItemModel(Map<MiningSkillCardItem.Tier, ItemModel> tiers, ItemTransforms transforms, Matrix4fc transformation, Overlays overlays) {
        this.tiers = tiers;
        this.overlays = overlays;
        this.transformation = new Matrix4f(transformation);
        // Where the icon's model starts on the card, as a flat item's model space (0..1) has it.
        float plateX = PLATE_X - ICON_SCALE / 2.0F, plateY = PLATE_Y - ICON_SCALE / 2.0F, plateZ = CARD_FRONT + 0.0005F - SLAB_BACK * ICON_SCALE;
        for (ItemDisplayContext context : ItemDisplayContext.values()) {
            ItemTransform card = transforms.getTransform(context);
            // An item transform ends by centering the model: the plate's offset from the model's middle, as it is
            // before the card's own scale and rotation.
            Vector3f offset = new Vector3f(plateX, plateY, plateZ).add(ICON_SCALE / 2.0F - 0.5F, ICON_SCALE / 2.0F - 0.5F, ICON_SCALE / 2.0F - 0.5F)
                    .mul(card.scale());
            new Quaternionf().rotationXYZ(card.rotation().x() * Mth.DEG_TO_RAD, card.rotation().y() * Mth.DEG_TO_RAD, card.rotation().z() * Mth.DEG_TO_RAD)
                    .transform(offset);
            this.transforms.put(context, new ItemTransform(card.rotation(), offset.add(card.translation()), new Vector3f(card.scale()).mul(ICON_SCALE)));
        }
        this.front = new Matrix4f(transformation);
        // The same on the card's other face: turned around the card's vertical middle (in the icon's own space).
        this.back = new Matrix4f(transformation).translate(-2.0F * (plateX - 0.5F) / ICON_SCALE, 0.0F, -2.0F * (plateZ - 0.5F) / ICON_SCALE).rotateY((float) Math.PI);
    }

    @Override
    public void update(ItemStackRenderState output, ItemStack item, ItemModelResolver resolver, ItemDisplayContext displayContext,
                       @Nullable ClientLevel level, @Nullable ItemOwner owner, int seed) {
        output.appendModelIdentityElement(this);
        MiningSkillCardItem.Tier tier = MiningSkillCardData.load(item).getTier();
        ItemModel model = this.tiers.get(tier);
        if (model == null) model = this.tiers.get(MiningSkillCardItem.Tier.Unlearned);
        if (model != null) model.update(output, item, resolver, displayContext, level, owner, seed);

        if (!(item.getItem() instanceof MiningSkillCardItem card)) return;
        MiningSkillCardItem.Type type = card.getType(item);
        if (type == MiningSkillCardItem.Type.EMPTY) {
            // A generic card whose type no data pack has (anymore): a question mark in the slot, as on the empty card.
            this.appendOverlay(output, this.overlays.question(), item, resolver, displayContext, level, owner, seed, 0, RAISED, displayContext == ItemDisplayContext.GUI);
            return;
        }
        boolean gui = displayContext == ItemDisplayContext.GUI;
        this.appendStars(output, item, resolver, displayContext, level, owner, seed, tier, gui);
        if (item.has(MiningSkillCardData.DATA_COMPONENT)) this.appendPips(output, item, resolver, displayContext, level, owner, seed, card.getData(item), gui);
        // One stack per type and icon: a data pack reload can give a type another icon.
        ItemStack icon = this.icons.computeIfAbsent(type.getId() + "|" + type.getIcon(), key -> type.iconStack());
        if (icon.isEmpty()) return;
        output.appendModelIdentityElement(type.getId());
        output.appendModelIdentityElement(type.getIcon());
        ItemTransform transform = this.transforms.get(displayContext);
        this.appendIcon(output, icon, resolver, level, owner, seed, transform, this.front);
        // A GUI only ever shows the front.
        if (displayContext != ItemDisplayContext.GUI) this.appendIcon(output, icon, resolver, level, owner, seed, transform, this.back);
    }

    // As many stars as the tier (three, in gold, once mastered), over the header's sockets.
    private void appendStars(ItemStackRenderState output, ItemStack item, ItemModelResolver resolver, ItemDisplayContext displayContext,
                             @Nullable ClientLevel level, @Nullable ItemOwner owner, int seed, MiningSkillCardItem.Tier tier, boolean gui) {
        ItemModel star = this.overlays.stars().get(tier);
        if (star == null) return;
        int lit = Math.min(tier.getValue(), 3);
        for (int i = 0; i < lit; i++) this.appendOverlay(output, star, item, resolver, displayContext, level, owner, seed, i * STAR_PITCH, RAISED, gui);
    }

    // The potion points along the card's bottom strip: one pip per point the tier holds, filled while it's left. As
    // many as fit centered on the card: up to three at full size, more of them narrower, and past eight the strip as
    // one bar of single-pixel pips showing the share that's left.
    private void appendPips(ItemStackRenderState output, ItemStack item, ItemModelResolver resolver, ItemDisplayContext displayContext,
                            @Nullable ClientLevel level, @Nullable ItemOwner owner, int seed, MiningSkillCardData data, boolean gui) {
        MiningSkillCardItem.Tier tier = data.getTier();
        if (data.isCreativeItem() || tier == MiningSkillCardItem.Tier.Unlearned || tier == MiningSkillCardItem.Tier.Mastered) return;
        if (!data.hasProgress()) return;  // potion points not synced yet
        int max = data.getMaxPotionPoints();
        if (max <= 0) return;
        int left = Math.clamp(data.getPotionPoints(), 0, max);
        output.appendModelIdentityElement(max);
        output.appendModelIdentityElement(left);
        int count = max, size, width, gap;
        if (max <= 3) {
            size = 0;
            width = 4;
            gap = 2;
        } else if (max == 4) {
            size = 1;
            width = 2;
            gap = 2;
        } else if (max <= 8) {
            size = 1;
            width = 2;
            gap = 0;
        } else {
            count = STRIP_WIDTH;
            size = 2;
            width = 1;
            gap = 0;
            left = Math.round(left / (float) max * count);
        }
        int start = (STRIP_WIDTH - (count * width + (count - 1) * gap)) / 2;
        for (int i = 0; i < count; i++) {
            boolean filled = i < left;
            this.appendOverlay(output, (filled ? this.overlays.pips() : this.overlays.emptyPips())[size], item, resolver, displayContext, level, owner, seed,
                    start + i * (width + gap), filled ? RAISED : FLUSH, gui);
        }
    }

    // One of the card's small layers, moved right by this many of the texture's pixels and out of the card's front by
    // this depth; and again out of its back, where a flat model shows the same picture mirrored, as the card's does.
    // (Moving a layer leaves its lighting alone; see the icon's transforms.)
    private void appendOverlay(ItemStackRenderState output, ItemModel overlay, ItemStack item, ItemModelResolver resolver, ItemDisplayContext displayContext,
                               @Nullable ClientLevel level, @Nullable ItemOwner owner, int seed, int texels, float depth, boolean gui) {
        for (int face = 0; face < (gui ? 1 : 2); face++) {
            LayerCapture capture = new LayerCapture(output);
            overlay.update(capture, item, resolver, displayContext, level, owner, seed);
            this.scratch.set(this.transformation).translate(texels * TEXEL, 0.0F, face == 0 ? depth : -depth);
            for (ItemStackRenderState.LayerRenderState layer : capture.layers) layer.setLocalTransform(this.scratch);
        }
    }

    // The icon's own item model, its layers taken over: placed on the plate, and moved with the card instead of with the
    // display transform the icon's item would have on its own (a tool held in the hand is turned differently).
    private void appendIcon(ItemStackRenderState output, ItemStack icon, ItemModelResolver resolver, @Nullable ClientLevel level, @Nullable ItemOwner owner,
                            int seed, ItemTransform transform, Matrix4fc placement) {
        LayerCapture capture = new LayerCapture(output);
        resolver.appendItemLayers(capture, icon, ItemDisplayContext.NONE, level, owner, seed);
        for (ItemStackRenderState.LayerRenderState layer : capture.layers) {
            layer.setItemTransform(transform);
            layer.setLocalTransform(placement);
        }
    }

    // Stands in for the card's render state while the icon's model adds its layers, to learn which layers those are.
    private static final class LayerCapture extends ItemStackRenderState {
        private final ItemStackRenderState target;
        private final List<ItemStackRenderState.LayerRenderState> layers = new ArrayList<>();

        private LayerCapture(ItemStackRenderState target) {
            this.target = target;
        }

        @Override
        public ItemStackRenderState.LayerRenderState newLayer() {
            ItemStackRenderState.LayerRenderState layer = this.target.newLayer();
            this.layers.add(layer);
            return layer;
        }

        @Override
        public void ensureCapacity(int requestedCount) {
            this.target.ensureCapacity(requestedCount);
        }

        @Override
        public void appendModelIdentityElement(Object element) {
            this.target.appendModelIdentityElement(element);
        }

        @Override
        public void setAnimated() {
            this.target.setAnimated();
        }

        // The icon being oversized in a GUI says nothing about the card.
        @Override
        public void setOversizedInGui(boolean oversizedInGui) {
        }
    }

    public record Unbaked(Map<MiningSkillCardItem.Tier, ItemModel.Unbaked> tiers) implements ItemModel.Unbaked {
        private static final Identifier FLAT_ITEM = Identifier.withDefaultNamespace("item/generated");
        private static final List<String> OVERLAYS = List.of("star_1", "star_2", "star_3", "star_mastered",
                "pip_4", "pip_2", "pip_1", "pip_4_empty", "pip_2_empty", "pip_1_empty", "question");

        private static Identifier overlayId(String name) {
            return FTBUltimineAddition.id("item/card_overlay/" + name);
        }

        // Placed by the card's model (appendOverlay), so baked without a transformation of its own.
        private static ItemModel overlay(ItemModel.BakingContext context, String name) {
            return new CuboidItemModelWrapper.Unbaked(overlayId(name), Optional.empty(), List.of()).bake(context, new Matrix4f());
        }
        private static final Codec<MiningSkillCardItem.Tier> TIER_KEY_CODEC = Codec.STRING.comapFlatMap(
                name -> Arrays.stream(MiningSkillCardItem.Tier.values())
                        .filter(tier -> tier.name().equalsIgnoreCase(name))
                        .findFirst()
                        .map(DataResult::success)
                        .orElseGet(() -> DataResult.error(() -> "Unknown card tier: " + name)),
                tier -> tier.name().toLowerCase(Locale.ROOT));

        public static final MapCodec<Unbaked> MAP_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                Codec.unboundedMap(TIER_KEY_CODEC, ItemModels.CODEC).fieldOf("classic").forGetter(Unbaked::tiers)
        ).apply(instance, Unbaked::new));

        @Override
        public MapCodec<Unbaked> type() {
            return MAP_CODEC;
        }

        @Override
        public void resolveDependencies(ResolvableModel.Resolver resolver) {
            this.tiers.values().forEach(model -> model.resolveDependencies(resolver));
            OVERLAYS.forEach(name -> resolver.markDependency(overlayId(name)));
            resolver.markDependency(FLAT_ITEM);
        }

        @Override
        public ItemModel bake(ItemModel.BakingContext context, Matrix4fc transformation) {
            Map<MiningSkillCardItem.Tier, ItemModel> tiers = new EnumMap<>(MiningSkillCardItem.Tier.class);
            this.tiers.forEach((tier, model) -> tiers.put(tier, model.bake(context, transformation)));
            // Every card is a flat item: its display transforms are that model's.
            ItemTransforms transforms = context.blockModelBaker().getModel(FLAT_ITEM).getTopTransforms();
            Map<MiningSkillCardItem.Tier, ItemModel> stars = new EnumMap<>(MiningSkillCardItem.Tier.class);
            stars.put(MiningSkillCardItem.Tier.Novice, overlay(context, "star_1"));
            stars.put(MiningSkillCardItem.Tier.Apprentice, overlay(context, "star_2"));
            stars.put(MiningSkillCardItem.Tier.Adept, overlay(context, "star_3"));
            stars.put(MiningSkillCardItem.Tier.Mastered, overlay(context, "star_mastered"));
            Overlays overlays = new Overlays(stars,
                    new ItemModel[]{overlay(context, "pip_4"), overlay(context, "pip_2"), overlay(context, "pip_1")},
                    new ItemModel[]{overlay(context, "pip_4_empty"), overlay(context, "pip_2_empty"), overlay(context, "pip_1_empty")},
                    overlay(context, "question"));
            return new MiningSkillCardItemModel(tiers, transforms, transformation, overlays);
        }
    }
}
