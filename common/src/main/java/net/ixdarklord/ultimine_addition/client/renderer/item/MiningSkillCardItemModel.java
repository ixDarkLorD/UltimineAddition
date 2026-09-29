package net.ixdarklord.ultimine_addition.client.renderer.item;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.ixdarklord.ultimine_addition.common.data.item.MiningSkillCardData;
import net.ixdarklord.ultimine_addition.common.item.MiningSkillCardItem;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.item.ItemModel;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.item.ItemModels;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.resources.model.ResolvableModel;
import net.minecraft.world.entity.ItemOwner;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4fc;

import java.util.Arrays;
import java.util.EnumMap;
import java.util.Locale;
import java.util.Map;

// The Mining Skill Card's model: one per tier, picked from the card's tier (a data component vanilla's select
// properties can't read). Item definitions list them under "classic"; any other key, like the removed "custom"
// renderer's, is ignored, so older resource packs still load.
public class MiningSkillCardItemModel implements ItemModel {
    private final Map<MiningSkillCardItem.Tier, ItemModel> tiers;

    private MiningSkillCardItemModel(Map<MiningSkillCardItem.Tier, ItemModel> tiers) {
        this.tiers = tiers;
    }

    @Override
    public void update(ItemStackRenderState output, ItemStack item, ItemModelResolver resolver, ItemDisplayContext displayContext,
                       @Nullable ClientLevel level, @Nullable ItemOwner owner, int seed) {
        output.appendModelIdentityElement(this);
        MiningSkillCardItem.Tier tier = MiningSkillCardData.load(item).getTier();
        ItemModel model = this.tiers.get(tier);
        if (model == null) model = this.tiers.get(MiningSkillCardItem.Tier.Unlearned);
        if (model != null) model.update(output, item, resolver, displayContext, level, owner, seed);
    }

    public record Unbaked(Map<MiningSkillCardItem.Tier, ItemModel.Unbaked> tiers) implements ItemModel.Unbaked {
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
        }

        @Override
        public ItemModel bake(ItemModel.BakingContext context, Matrix4fc transformation) {
            Map<MiningSkillCardItem.Tier, ItemModel> tiers = new EnumMap<>(MiningSkillCardItem.Tier.class);
            this.tiers.forEach((tier, model) -> tiers.put(tier, model.bake(context, transformation)));
            return new MiningSkillCardItemModel(tiers);
        }
    }
}
