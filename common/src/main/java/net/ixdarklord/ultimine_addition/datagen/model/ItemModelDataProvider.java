package net.ixdarklord.ultimine_addition.datagen.model;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import net.ixdarklord.ultimine_addition.core.FTBUltimineAddition;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * Generates the item models ({@code models/item}) and 26.1 item definitions ({@code items}) for both loaders.
 * Mining Skill Cards use the custom {@code ultimine_addition:mining_skill_card} item model type and the
 * Miner Certificate the {@code ultimine_addition:certificate_opened} condition (registered by the client in UAItemModels).
 */
public class ItemModelDataProvider implements DataProvider {
    private static final String[] CARD_TYPES = {"pickaxe", "axe", "shovel", "hoe"};
    private static final String[] DYES = {"white", "orange", "magenta", "light_blue", "yellow", "lime", "pink", "gray",
            "light_gray", "cyan", "purple", "blue", "brown", "green", "red", "black"};
    private static final String[][] TIERS = {
            {"unlearned", ""}, {"novice", "_1"}, {"apprentice", "_2"}, {"adept", "_3"}, {"mastered", "_mastered"}
    };

    private final PackOutput.PathProvider modelPaths;
    private final PackOutput.PathProvider itemPaths;
    private final List<CompletableFuture<?>> futures = new ArrayList<>();

    public ItemModelDataProvider(PackOutput output) {
        this.modelPaths = output.createPathProvider(PackOutput.Target.RESOURCE_PACK, "models/item");
        this.itemPaths = output.createPathProvider(PackOutput.Target.RESOURCE_PACK, "items");
    }

    @Override
    public @NotNull CompletableFuture<?> run(@NotNull CachedOutput cache) {
        this.futures.clear();

        this.simpleItem(cache, "ink_chamber", "minecraft:item/handheld", null);
        this.simpleItem(cache, "pen", "minecraft:item/handheld", null);
        this.simpleItem(cache, "card_blueprint", "minecraft:item/generated", null);
        this.simpleItem(cache, "mining_skill_card_empty", "minecraft:item/generated", null);

        // Shape Certificates: the tool's plaque (as on its Mining Skill Card) picked from the certificate's tool;
        // custom card tools fall back to the plain certificate.
        for (String tier : new String[]{"novice", "apprentice", "adept"}) {
            String base = "shape_certificate_" + tier;
            this.model(cache, base, "minecraft:item/generated", "item/" + base, null);
            JsonArray cases = new JsonArray();
            for (String type : CARD_TYPES) {
                String name = base + "_" + type;
                this.model(cache, name, "minecraft:item/generated", "item/" + name, null);
                JsonObject when = new JsonObject();
                when.addProperty("Tool", type);
                JsonObject entry = new JsonObject();
                entry.add("when", when);
                entry.add("model", modelReference(name));
                cases.add(entry);
            }
            JsonObject select = new JsonObject();
            select.addProperty("type", "minecraft:select");
            select.addProperty("property", "minecraft:component");
            select.addProperty("component", FTBUltimineAddition.id("shape_certificate_data").toString());
            select.add("cases", cases);
            select.add("fallback", modelReference(base));
            this.itemDefinition(cache, base, select);
        }
        // Skills Record: a model per dye (vanilla's base_color component, set by crafting it with a dye).
        this.model(cache, "skills_record", "minecraft:item/handheld", "item/skills_record", heldToolDisplay());
        JsonArray recordCases = new JsonArray();
        for (String dye : DYES) {
            String name = "skills_record_" + dye;
            this.model(cache, name, "minecraft:item/handheld", "item/" + name, heldToolDisplay());
            JsonObject entry = new JsonObject();
            entry.addProperty("when", dye);
            entry.add("model", modelReference(name));
            recordCases.add(entry);
        }
        JsonObject record = new JsonObject();
        record.addProperty("type", "minecraft:select");
        record.addProperty("property", "minecraft:component");
        record.addProperty("component", "minecraft:base_color");
        record.add("cases", recordCases);
        record.add("fallback", modelReference("skills_record"));
        this.itemDefinition(cache, "skills_record", record);
        this.simpleItem(cache, "shape_selector", "minecraft:item/handheld", heldToolDisplay());

        // Miner Certificate: switches to the "opened" model once accomplished.
        this.model(cache, "miner_certificate", "minecraft:item/generated", "item/miner_certificate", null);
        this.model(cache, "miner_certificate_opened", "minecraft:item/generated", "item/miner_certificate_opened", null);
        JsonObject certificate = new JsonObject();
        certificate.addProperty("type", "minecraft:condition");
        certificate.addProperty("property", FTBUltimineAddition.id("certificate_opened").toString());
        certificate.add("on_true", modelReference("miner_certificate_opened"));
        certificate.add("on_false", modelReference("miner_certificate"));
        this.itemDefinition(cache, "miner_certificate", certificate);

        // Mining Skill Cards: a model per tier.
        for (String type : CARD_TYPES) {
            String base = "mining_skill_card_" + type;
            JsonObject classic = new JsonObject();
            for (int i = 0; i < TIERS.length; i++) {
                String modelName = base + TIERS[i][1];
                String texture = "item/" + base + (i == 0 ? "_unlearned" : TIERS[i][1]);
                this.model(cache, modelName, "minecraft:item/generated", texture, null);
                classic.add(TIERS[i][0], modelReference(modelName));
            }
            JsonObject card = new JsonObject();
            card.addProperty("type", FTBUltimineAddition.id("mining_skill_card").toString());
            card.add("classic", classic);
            this.itemDefinition(cache, base, card);
        }

        return CompletableFuture.allOf(this.futures.toArray(CompletableFuture[]::new));
    }

    private void simpleItem(CachedOutput cache, String name, String parent, JsonObject display) {
        this.model(cache, name, parent, "item/" + name, display);
        this.itemDefinition(cache, name, modelReference(name));
    }

    private void model(CachedOutput cache, String name, String parent, String texture, JsonObject display) {
        JsonObject json = new JsonObject();
        json.addProperty("parent", parent);
        if (display != null) json.add("display", display);
        JsonObject textures = new JsonObject();
        textures.addProperty("layer0", FTBUltimineAddition.id(texture).toString());
        json.add("textures", textures);
        this.futures.add(DataProvider.saveStable(cache, json, this.modelPaths.json(FTBUltimineAddition.id(name))));
    }

    private void itemDefinition(CachedOutput cache, String item, JsonObject model) {
        JsonObject json = new JsonObject();
        json.add("model", model);
        this.futures.add(DataProvider.saveStable(cache, json, this.itemPaths.json(FTBUltimineAddition.id(item))));
    }

    private static JsonObject modelReference(String name) {
        JsonObject json = new JsonObject();
        json.addProperty("type", "minecraft:model");
        json.addProperty("model", FTBUltimineAddition.id("item/" + name).toString());
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
