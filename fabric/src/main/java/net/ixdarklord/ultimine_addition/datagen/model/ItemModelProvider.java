package net.ixdarklord.ultimine_addition.datagen.model;

import com.google.common.base.Preconditions;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricModelProvider;
import net.ixdarklord.coolcatlib.api.util.JsonUtils;
import net.ixdarklord.ultimine_addition.core.Registration;
import net.minecraft.client.renderer.block.model.BlockModel;
import net.minecraft.client.renderer.block.model.ItemTransform;
import net.minecraft.data.models.BlockModelGenerators;
import net.minecraft.data.models.ItemModelGenerators;
import net.minecraft.data.models.model.ModelTemplate;
import net.minecraft.data.models.model.ModelTemplates;
import net.minecraft.data.models.model.TextureMapping;
import net.minecraft.data.models.model.TextureSlot;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemDisplayContext;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector3f;

import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Supplier;
import java.util.stream.Collectors;

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
        ModelTemplate template = new ModelTemplate(Optional.of(ResourceLocation.withDefaultNamespace("builtin/entity")), Optional.empty(), TextureSlot.LAYER0);
        ModelBuilder builder = new ModelBuilder(location, string, template);
        this.builders.add(builder);
        return builder;
    }

    public final @NotNull String getName() {
        return "Item Models";
    }

    public static class ModelBuilder {
        private final ResourceLocation id;
        private final String string;
        protected final ModelBuilder.TransformsBuilder transforms = new TransformsBuilder();
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
            return ResourceLocation.fromNamespaceAndPath(id.getNamespace(), "item/" + id.getPath() + (string != null ? isAddingStringToFileName ? string : "" : ""));
        }

        public ResourceLocation getItemTextureLocation() {
            return ResourceLocation.fromNamespaceAndPath(id.getNamespace(), "item/" + id.getPath() + (string != null ? string : ""));
        }

        private ModelBuilder self() {
            return this;
        }

        public ModelBuilder.TransformsBuilder transforms() {
            return this.transforms;
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

            Map<ItemDisplayContext, ItemTransform> transforms = this.transforms.build();
            if (!transforms.isEmpty()) {
                JsonObject display = new JsonObject();

                for(Map.Entry<ItemDisplayContext, ItemTransform> e : transforms.entrySet()) {
                    JsonObject transform = new JsonObject();
                    ItemTransform vec = e.getValue();
                    if (!vec.equals(ItemTransform.NO_TRANSFORM)) {
                        if (!vec.translation.equals(ItemTransformDeserializer.DEFAULT_TRANSLATION)) {
                            transform.add("translation", this.serializeVector3f(e.getValue().translation));
                        }

                        if (!vec.rotation.equals(ItemTransformDeserializer.DEFAULT_ROTATION)) {
                            transform.add("rotation", this.serializeVector3f(vec.rotation));
                        }

                        if (!vec.scale.equals(ItemTransformDeserializer.DEFAULT_SCALE)) {
                            transform.add("scale", this.serializeVector3f(e.getValue().scale));
                        }

                        display.add(e.getKey().getSerializedName(), transform);
                    }
                }

                root.add("display", display);
            }

            return root;
        }

        private JsonArray serializeVector3f(Vector3f vec) {
            JsonArray ret = new JsonArray();
            ret.add(this.serializeFloat(vec.x()));
            ret.add(this.serializeFloat(vec.y()));
            ret.add(this.serializeFloat(vec.z()));
            return ret;
        }

        private Number serializeFloat(float f) {
            return (float)((int)f) == f ? (int)f : f;
        }

        public class OverrideBuilder {
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

        public class TransformsBuilder {
            private final Map<ItemDisplayContext, ModelBuilder.TransformsBuilder.TransformVecBuilder> transforms = new LinkedHashMap<>();

            public TransformsBuilder() {
            }

            public ModelBuilder.TransformsBuilder.TransformVecBuilder transform(ItemDisplayContext type) {
                Preconditions.checkNotNull(type, "Perspective cannot be null");
                return this.transforms.computeIfAbsent(type, TransformVecBuilder::new);
            }

            Map<ItemDisplayContext, ItemTransform> build() {
                return this.transforms.entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, (e) -> ((TransformVecBuilder)e.getValue()).build(), (k1, k2) -> {
                    throw new IllegalArgumentException();
                }, LinkedHashMap::new));
            }

            public ModelBuilder end() {
                return ModelBuilder.this.self();
            }

            public class TransformVecBuilder {
                private Vector3f rotation;
                private Vector3f translation;
                private Vector3f scale;

                TransformVecBuilder(ItemDisplayContext type) {
                    this.rotation = new Vector3f(ItemTransformDeserializer.DEFAULT_ROTATION);
                    this.translation = new Vector3f(ItemTransformDeserializer.DEFAULT_TRANSLATION);
                    this.scale = new Vector3f(ItemTransformDeserializer.DEFAULT_SCALE);
                }

                public ModelBuilder.TransformsBuilder.TransformVecBuilder rotation(float x, float y, float z) {
                    this.rotation = new Vector3f(x, y, z);
                    return this;
                }

                public ModelBuilder.TransformsBuilder.TransformVecBuilder leftRotation(float x, float y, float z) {
                    return this.rotation(x, y, z);
                }

                public ModelBuilder.TransformsBuilder.TransformVecBuilder translation(float x, float y, float z) {
                    this.translation = new Vector3f(x, y, z);
                    return this;
                }

                public ModelBuilder.TransformsBuilder.TransformVecBuilder scale(float sc) {
                    return this.scale(sc, sc, sc);
                }

                public ModelBuilder.TransformsBuilder.TransformVecBuilder scale(float x, float y, float z) {
                    this.scale = new Vector3f(x, y, z);
                    return this;
                }

                ItemTransform build() {
                    return new ItemTransform(this.rotation, this.translation, this.scale);
                }

                public ModelBuilder.TransformsBuilder end() {
                    return TransformsBuilder.this;
                }
            }
        }
    }
}
