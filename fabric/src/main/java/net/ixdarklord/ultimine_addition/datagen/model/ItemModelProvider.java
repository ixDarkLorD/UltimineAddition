package net.ixdarklord.ultimine_addition.datagen.model;

import com.google.common.base.Preconditions;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricModelProvider;
import net.ixdarklord.coolcat_lib.util.JsonUtils;
import net.ixdarklord.ultimine_addition.core.Registration;
import net.minecraft.client.renderer.block.model.BlockModel;
import net.minecraft.data.models.BlockModelGenerators;
import net.minecraft.data.models.ItemModelGenerators;
import net.minecraft.data.models.model.ModelTemplate;
import net.minecraft.data.models.model.ModelTemplates;
import net.minecraft.data.models.model.TextureMapping;
import net.minecraft.data.models.model.TextureSlot;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Supplier;

public abstract class ItemModelProvider extends FabricModelProvider {
    private final List<ModelBuilder> builders = new ArrayList<>();

    public ItemModelProvider(FabricDataOutput output) {
        super(output);
        registerModels();
    }

    protected abstract void registerModels();

    @Override
    public void generateItemModels(ItemModelGenerators generator) {
        builders.forEach(builder -> this.create(builder.getItemModelLocation(), TextureMapping.layer0(builder.getItemTextureLocation()), builder, generator.output));
    }

    protected void create(ResourceLocation location, TextureMapping textureMapping, ModelBuilder builder, BiConsumer<ResourceLocation, Supplier<JsonElement>> biConsumer) {
        Map<TextureSlot, ResourceLocation> map = builder.template.createMap(textureMapping);
        biConsumer.accept(location, () -> {
            JsonObject jsonObject = new JsonObject();
            builder.template.model.ifPresent((resourceLocation) -> jsonObject.addProperty("parent", resourceLocation.toString()));
            if (builder.guiLight != null) jsonObject.addProperty("gui_light", builder.guiLight.toString().toLowerCase());
            JsonUtils.deepMerge(builder.toJson(), jsonObject);

            boolean isCustomRenderer = builder.template.model.isPresent() && builder.template.model.get().toString().contains("builtin/entity");
            if (!map.isEmpty() && !isCustomRenderer) {
                JsonObject jsonObject2 = new JsonObject();
                map.forEach((textureSlot, resourceLocation) -> jsonObject2.addProperty(textureSlot.getId(), resourceLocation.toString()));
                jsonObject.add("textures", jsonObject2);
            }

            return jsonObject;
        });
    }
    @Override
    public void generateBlockStateModels(BlockModelGenerators blockStateModelGenerator) {}

    protected ModelBuilder simpleItem(Item item) {
        return this.simpleItem(Registration.ITEMS.getRegistrar().getId(item), null);
    }

    @SuppressWarnings({"UnusedReturnValue", "SameParameterValue"})
    protected ModelBuilder simpleItem(Item item, String string) {
        return this.simpleItem(Registration.ITEMS.getRegistrar().getId(item), string);
    }

    protected ModelBuilder simpleItem(ResourceLocation location, String string) {
        ModelBuilder builder = new ModelBuilder(location, string, ModelTemplates.FLAT_ITEM);
        this.builders.add(builder);
        return builder;
    }

    @SuppressWarnings("UnusedReturnValue")
    protected ModelBuilder handheldItem(Item item) {
        return this.handheldItem(Registration.ITEMS.getRegistrar().getId(item), null);
    }

    @SuppressWarnings("SameParameterValue")
    protected ModelBuilder handheldItem(ResourceLocation location, String string) {
        ModelBuilder builder = new ModelBuilder(location, string, ModelTemplates.FLAT_HANDHELD_ITEM);
        this.builders.add(builder);
        return builder;
    }

    @SuppressWarnings("unused")
    protected ModelBuilder specialRendererItem(Item item) {
        return this.specialRendererItem(Registration.ITEMS.getRegistrar().getId(item), null);
    }

    protected ModelBuilder specialRendererItem(ResourceLocation location) {
        return this.specialRendererItem(location, null);
    }

    @SuppressWarnings("SameParameterValue")
    protected ModelBuilder specialRendererItem(ResourceLocation location, String string) {
        ModelTemplate template = new ModelTemplate(Optional.of(new ResourceLocation("minecraft", "builtin/entity")), Optional.empty(), TextureSlot.LAYER0);
        ModelBuilder builder = new ModelBuilder(location, string, template);
        this.builders.add(builder);
        return builder;
    }

    public final @NotNull String getName() {
        return "Item Models";
    }

    protected static class ModelBuilder {
        private final ResourceLocation id;
        private final String string;
        private final ModelTemplate template;
        private BlockModel.GuiLight guiLight;
        private boolean isAddingStringToFileName;
        private final List<OverrideBuilder> overrides = new ArrayList<>();

        public ModelBuilder(ResourceLocation id, String string, ModelTemplate template) {
            this.id = id;
            this.string = string;
            this.template = template;
        }

        public ResourceLocation getItemModelLocation() {
            return new ResourceLocation(id.getNamespace(), "item/" + id.getPath() + (string != null ? isAddingStringToFileName ? string : "" : ""));
        }

        public ResourceLocation getItemTextureLocation() {
            return new ResourceLocation(id.getNamespace(), "item/" + id.getPath() + (string != null ? string : ""));
        }

        @SuppressWarnings("UnusedReturnValue")
        public ModelBuilder guiLight(BlockModel.GuiLight guiLight) {
            this.guiLight = guiLight;
            return this;
        }

        @SuppressWarnings("UnusedReturnValue")
        public ModelBuilder addStringToFileName() {
            this.isAddingStringToFileName = true;
            return this;
        }

        public OverrideBuilder override() {
            OverrideBuilder ret = new OverrideBuilder();
            overrides.add(ret);
            return ret;
        }

        @SuppressWarnings("unused")
        public OverrideBuilder override(int index) {
            Preconditions.checkElementIndex(index, overrides.size(), "override");
            return overrides.get(index);
        }

        public JsonObject toJson() {
            JsonObject root = new JsonObject();
            if (!overrides.isEmpty()) {
                JsonArray overridesJson = new JsonArray();
                overrides.stream().map(OverrideBuilder::toJson).forEach(overridesJson::add);
                root.add("overrides", overridesJson);
            }
            return root;
        }

        protected class OverrideBuilder {
            protected final Map<ResourceLocation, Float> predicates = new LinkedHashMap<>();
            protected ResourceLocation model;

            public OverrideBuilder predicate(ResourceLocation location, float value) {
                predicates.put(location, value);
                return this;
            }
            public OverrideBuilder model(ResourceLocation location) {
                this.model = location;
                return this;
            }
            public ModelBuilder end() {
                return ModelBuilder.this;
            }

            JsonObject toJson() {
                JsonObject ret = new JsonObject();
                JsonObject predicatesJson = new JsonObject();
                predicates.forEach((key, val) -> predicatesJson.addProperty(key.toString(), val));
                ret.add("predicate", predicatesJson);
                ret.addProperty("model", model.toString());
                return ret;
            }
        }
    }
}
