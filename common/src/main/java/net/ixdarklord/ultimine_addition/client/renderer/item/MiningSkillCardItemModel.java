package net.ixdarklord.ultimine_addition.client.renderer.item;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.ixdarklord.coolcatlib.api.utils.MathUtils;
import net.ixdarklord.ultimine_addition.common.data.item.MiningSkillCardData;
import net.ixdarklord.ultimine_addition.common.item.MiningSkillCardItem;
import net.ixdarklord.ultimine_addition.config.ConfigHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.item.*;
import net.minecraft.client.renderer.special.SpecialModelRenderer;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.client.resources.model.ResolvableModel;
import net.minecraft.client.resources.model.ResolvedModel;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.ItemOwner;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;
import org.joml.Matrix4fc;
import org.joml.Vector3fc;

import java.util.*;
import java.util.function.Consumer;

public class MiningSkillCardItemModel implements ItemModel {
    // Display item placement; still needs tuning in-game.
    private static final float DISPLAY_ITEM_SCALE = 0.5F;
    private static final float DISPLAY_ITEM_X = -0.2F;
    private static final float DISPLAY_ITEM_Y = -0.15F;
    private static final float DISPLAY_ITEM_Z = 0.05F;

    private final Map<MiningSkillCardItem.Tier, ItemModel> classic;
    private final Map<MiningSkillCardItem.Tier, CustomCard> custom;
    private final Matrix4fc transformation;

    private MiningSkillCardItemModel(Map<MiningSkillCardItem.Tier, ItemModel> classic, Map<MiningSkillCardItem.Tier, CustomCard> custom, Matrix4fc transformation) {
        this.classic = classic;
        this.custom = custom;
        this.transformation = transformation;
    }

    @Override
    public void update(ItemStackRenderState output, ItemStack item, ItemModelResolver resolver, ItemDisplayContext displayContext,
                       @Nullable ClientLevel level, @Nullable ItemOwner owner, int seed) {
        output.appendModelIdentityElement(this);
        MiningSkillCardData data = MiningSkillCardData.load(item);
        MiningSkillCardItem.Tier tier = data.getTier();

        CustomCard customCard = ConfigHandler.CLIENT.MSC_RENDERER.get() ? pick(this.custom, tier) : null;
        if (customCard == null) {
            ItemModel model = pick(this.classic, tier);
            if (model != null) model.update(output, item, resolver, displayContext, level, owner, seed);
            return;
        }

        customCard.card().update(output, item, resolver, displayContext, level, owner, seed);
        ItemStack displayItem = data.getDisplayItem();
        if (displayItem.isEmpty()) return;

        float scale = DISPLAY_ITEM_SCALE;
        if (displayContext != ItemDisplayContext.GUI) {
            output.setAnimated();
            int tickCount = Minecraft.getInstance().player != null ? Minecraft.getInstance().player.tickCount : 0;
            scale *= MathUtils.cycledBetweenValues(0.95F, 1.0F, 0.8F, tickCount / 20.0F, false);
        }

        output.appendModelIdentityElement(displayItem.getItem());
        ItemStackRenderState.LayerRenderState layer = output.newLayer();
        customCard.properties().applyToLayer(layer, displayContext);
        layer.setLocalTransform(new Matrix4f(this.transformation)
                .translate(DISPLAY_ITEM_X, DISPLAY_ITEM_Y, DISPLAY_ITEM_Z)
                .scale(scale));
        layer.setupSpecialModel(DisplayItemRenderer.INSTANCE, displayItem);
    }

    private static <T> @Nullable T pick(Map<MiningSkillCardItem.Tier, T> models, MiningSkillCardItem.Tier tier) {
        T model = models.get(tier);
        return model != null ? model : models.get(MiningSkillCardItem.Tier.Unlearned);
    }

    private record CustomCard(ItemModel card, ModelRenderProperties properties) {}

    private static final class DisplayItemRenderer implements SpecialModelRenderer<ItemStack> {
        private static final DisplayItemRenderer INSTANCE = new DisplayItemRenderer();
        private final ItemStackRenderState displayState = new ItemStackRenderState();

        @Override
        public void submit(@Nullable ItemStack displayItem, PoseStack poseStack, SubmitNodeCollector submitNodeCollector,
                           int lightCoords, int overlayCoords, boolean hasFoil, int outlineColor) {
            if (displayItem == null) return;
            Minecraft minecraft = Minecraft.getInstance();
            minecraft.getItemModelResolver().updateForTopItem(this.displayState, displayItem, ItemDisplayContext.GUI, minecraft.level, null, 0);
            this.displayState.submit(poseStack, submitNodeCollector, lightCoords, overlayCoords, outlineColor);
        }

        @Override
        public void getExtents(Consumer<Vector3fc> output) {
        }

        @Override
        public @Nullable ItemStack extractArgument(ItemStack stack) {
            return null;
        }
    }

    public record Unbaked(Map<MiningSkillCardItem.Tier, ItemModel.Unbaked> classic,
                          Map<MiningSkillCardItem.Tier, Identifier> custom) implements ItemModel.Unbaked {
        private static final Codec<MiningSkillCardItem.Tier> TIER_KEY_CODEC = Codec.STRING.comapFlatMap(
                name -> Arrays.stream(MiningSkillCardItem.Tier.values())
                        .filter(tier -> tier.name().equalsIgnoreCase(name))
                        .findFirst()
                        .map(DataResult::success)
                        .orElseGet(() -> DataResult.error(() -> "Unknown card tier: " + name)),
                tier -> tier.name().toLowerCase(Locale.ROOT));

        public static final MapCodec<Unbaked> MAP_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                Codec.unboundedMap(TIER_KEY_CODEC, ItemModels.CODEC).fieldOf("classic").forGetter(Unbaked::classic),
                Codec.unboundedMap(TIER_KEY_CODEC, Identifier.CODEC).optionalFieldOf("custom", Map.of()).forGetter(Unbaked::custom)
        ).apply(instance, Unbaked::new));

        @Override
        public MapCodec<Unbaked> type() {
            return MAP_CODEC;
        }

        @Override
        public void resolveDependencies(ResolvableModel.Resolver resolver) {
            this.classic.values().forEach(model -> model.resolveDependencies(resolver));
            this.custom.values().forEach(resolver::markDependency);
        }

        @Override
        public ItemModel bake(ItemModel.BakingContext context, Matrix4fc transformation) {
            Map<MiningSkillCardItem.Tier, ItemModel> classic = new EnumMap<>(MiningSkillCardItem.Tier.class);
            this.classic.forEach((tier, model) -> classic.put(tier, model.bake(context, transformation)));

            ModelBaker baker = context.blockModelBaker();
            Map<MiningSkillCardItem.Tier, CustomCard> custom = new EnumMap<>(MiningSkillCardItem.Tier.class);
            this.custom.forEach((tier, modelId) -> {
                ResolvedModel resolved = baker.getModel(modelId);
                ModelRenderProperties properties = ModelRenderProperties.fromResolvedModel(baker, resolved, resolved.getTopTextureSlots());
                ItemModel card = new CuboidItemModelWrapper.Unbaked(modelId, Optional.empty(), List.of()).bake(context, transformation);
                custom.put(tier, new CustomCard(card, properties));
            });
            return new MiningSkillCardItemModel(classic, custom, transformation);
        }
    }
}
