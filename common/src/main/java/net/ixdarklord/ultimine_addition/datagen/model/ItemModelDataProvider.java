package net.ixdarklord.ultimine_addition.datagen.model;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import net.ixdarklord.ultimine_addition.client.renderer.item.UAItemModels;
import net.ixdarklord.ultimine_addition.common.item.MiningSkillCardItem;
import net.ixdarklord.ultimine_addition.core.FTBUltimineAddition;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.DyeColor;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * Generates the item models ({@code models/item}) for both loaders. A Mining Skill Card's model switches to its tier's
 * through overrides on the {@code ultimine_addition:tier} item property, the Miner Certificate's on
 * {@code ultimine_addition:certificate_opened}, a Shape Certificate's on {@code ultimine_addition:tool} and a Skills
 * Record's on {@code ultimine_addition:dye} (registered by the client in UAItemModels).
 */
public class ItemModelDataProvider implements DataProvider {
    private static final String[] CARD_TYPES = {"pickaxe", "axe", "shovel", "hoe"};
    // Each tier's model suffix, Unlearned first.
    private static final String[] TIER_SUFFIXES = {"", "_1", "_2", "_3", "_mastered"};

    private final PackOutput.PathProvider modelPaths;
    private final List<CompletableFuture<?>> futures = new ArrayList<>();

    public ItemModelDataProvider(PackOutput output) {
        this.modelPaths = output.createPathProvider(PackOutput.Target.RESOURCE_PACK, "models/item");
    }

    @Override
    public @NotNull CompletableFuture<?> run(@NotNull CachedOutput cache) {
        this.futures.clear();

        this.simpleItem(cache, "ink_chamber", "minecraft:item/handheld", null);
        this.simpleItem(cache, "pen", "minecraft:item/handheld", null);
        this.simpleItem(cache, "mining_skill_card_empty", "minecraft:item/generated", null);

        // Shape Certificates: the tool's plaque (as on its Mining Skill Card) picked from the certificate's tool;
        // custom card tools fall back to the plain certificate.
        for (String tier : new String[]{"novice", "apprentice", "adept"}) {
            String base = "shape_certificate_" + tier;
            JsonArray overrides = new JsonArray();
            for (int i = 0; i < UAItemModels.CERTIFICATE_TOOLS.size(); i++) {
                String name = base + "_" + UAItemModels.CERTIFICATE_TOOLS.get(i).getId();
                this.model(cache, name, "minecraft:item/generated", "item/" + name, null, null);
                overrides.add(override(UAItemModels.CERTIFICATE_TOOL, (i + 1) / 4.0F, name));
            }
            this.model(cache, base, "minecraft:item/generated", "item/" + base, null, overrides);
        }
        // Skills Record: a model per dye, picked by the ultimine_addition:dye item property (the record's Color tag; 26.1.2
        // selects on vanilla's base_color component).
        JsonArray recordOverrides = new JsonArray();
        for (DyeColor dye : DyeColor.values()) {
            String name = "skills_record_" + dye.getSerializedName();
            this.model(cache, name, "minecraft:item/handheld", "item/" + name, heldToolDisplay(), null);
            recordOverrides.add(override(UAItemModels.SKILLS_RECORD_DYE, UAItemModels.dyeValue(dye), name));
        }
        this.model(cache, "skills_record", "minecraft:item/handheld", "item/skills_record", heldToolDisplay(), recordOverrides);
        this.simpleItem(cache, "shape_selector", "minecraft:item/handheld", heldToolDisplay());

        // Miner Certificate: switches to the "opened" model once accomplished.
        this.model(cache, "miner_certificate_opened", "minecraft:item/generated", "item/miner_certificate_opened", null, null);
        JsonArray certificate = new JsonArray();
        certificate.add(override(UAItemModels.CERTIFICATE_OPENED, 1.0F, "miner_certificate_opened"));
        this.model(cache, "miner_certificate", "minecraft:item/generated", "item/miner_certificate", null, certificate);

        // Mining Skill Cards: a model per tier; the Unlearned one is the item's own and picks the others by tier.
        for (String type : CARD_TYPES) {
            String base = "mining_skill_card_" + type;
            JsonArray tiers = new JsonArray();
            for (int i = 1; i < TIER_SUFFIXES.length; i++) {
                String modelName = base + TIER_SUFFIXES[i];
                this.model(cache, modelName, "minecraft:item/generated", "item/" + modelName, null, null);
                tiers.add(override(UAItemModels.MINING_SKILL_CARD_TIER, UAItemModels.tierValue(MiningSkillCardItem.Tier.values()[i]), modelName));
            }
            this.model(cache, base, "minecraft:item/generated", "item/" + base + "_unlearned", null, tiers);
        }

        return CompletableFuture.allOf(this.futures.toArray(CompletableFuture[]::new));
    }

    private void simpleItem(CachedOutput cache, String name, String parent, JsonObject display) {
        this.model(cache, name, parent, "item/" + name, display, null);
    }

    private void model(CachedOutput cache, String name, String parent, String texture, JsonObject display, JsonArray overrides) {
        JsonObject json = new JsonObject();
        json.addProperty("parent", parent);
        if (display != null) json.add("display", display);
        JsonObject textures = new JsonObject();
        textures.addProperty("layer0", FTBUltimineAddition.id(texture).toString());
        json.add("textures", textures);
        if (overrides != null) json.add("overrides", overrides);
        this.futures.add(DataProvider.saveStable(cache, json, this.modelPaths.json(FTBUltimineAddition.id(name))));
    }

    // An override picking model once property reaches value (the last matching one wins).
    private static JsonObject override(ResourceLocation property, float value, String model) {
        JsonObject predicate = new JsonObject();
        predicate.addProperty(property.toString(), value);
        JsonObject json = new JsonObject();
        json.add("predicate", predicate);
        json.addProperty("model", FTBUltimineAddition.id("item/" + model).toString());
        return json;
    }

    /** Display transforms shared by the Skills Record and the Shape Selector. */
    private static JsonObject heldToolDisplay() {
        JsonObject display = new JsonObject();
        JsonObject firstPerson = transform(new float[]{-10, -45, 0}, new float[]{1, 4, 0}, 0.65F);
        JsonObject thirdPerson = transform(new float[]{45, -10, 0}, new float[]{0, 4, 5}, 0.75F);
        display.add("firstperson_lefthand", firstPerson);
        display.add("firstperson_righthand", firstPerson.deepCopy());
        display.add("thirdperson_lefthand", thirdPerson);
        display.add("thirdperson_righthand", thirdPerson.deepCopy());
        return display;
    }

    private static JsonObject transform(float[] rotation, float[] translation, float scale) {
        JsonObject json = new JsonObject();
        json.add("rotation", vector(rotation));
        json.add("scale", vector(new float[]{scale, scale, scale}));
        json.add("translation", vector(translation));
        return json;
    }

    private static JsonArray vector(float[] values) {
        JsonArray array = new JsonArray();
        for (float value : values) array.add(value);
        return array;
    }

    @Override
    public @NotNull String getName() {
        return FTBUltimineAddition.MOD_NAME + " Item Models";
    }
}
