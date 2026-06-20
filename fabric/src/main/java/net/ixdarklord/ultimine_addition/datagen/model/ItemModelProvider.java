package net.ixdarklord.ultimine_addition.datagen.model;

import com.google.common.base.Preconditions;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricModelProvider;
import net.ixdarklord.coolcatlib.api.utils.JsonUtils;
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
import java.util.stream.Stream;

public abstract class ItemModelProvider extends FabricModelProvider {
   private final List<ModelBuilder> builders = new ArrayList<>();

   public ItemModelProvider(FabricDataOutput output) {
      super(output);
      this.registerModels();
   }

   protected abstract void registerModels();

   public void generateItemModels(ItemModelGenerators generator) {
      this.builders.forEach((builder) -> this.create(builder.getItemModelLocation(), TextureMapping.layer0(builder.getItemTextureLocation()), builder, generator.output));
   }

   protected void create(ResourceLocation location, TextureMapping textureMapping, ModelBuilder builder, BiConsumer<ResourceLocation, Supplier<JsonElement>> biConsumer) {
      Map<TextureSlot, ResourceLocation> map = builder.template.createMap(textureMapping);
      biConsumer.accept(location, () -> {
         JsonObject jsonObject = new JsonObject();
         builder.template.model.ifPresent((resourceLocation) -> jsonObject.addProperty("parent", resourceLocation.toString()));
         if (builder.guiLight != null) {
            jsonObject.addProperty("gui_light", builder.guiLight.toString().toLowerCase());
         }

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

   public void generateBlockStateModels(BlockModelGenerators blockStateModelGenerator) {
   }

   protected ModelBuilder simpleItem(Item item) {
      return this.simpleItem(Registration.ITEMS.getRegistrar().getId(item), null);
   }

   protected ModelBuilder simpleItem(Item item, String string) {
      return this.simpleItem(Registration.ITEMS.getRegistrar().getId(item), string);
   }

   protected ModelBuilder simpleItem(ResourceLocation location, String string) {
      ModelBuilder builder = new ModelBuilder(location, string, ModelTemplates.FLAT_ITEM);
      this.builders.add(builder);
      return builder;
   }

   protected ModelBuilder handheldItem(Item item) {
      return this.handheldItem(Registration.ITEMS.getRegistrar().getId(item), null);
   }

   protected ModelBuilder handheldItem(ResourceLocation location, String string) {
      ModelBuilder builder = new ModelBuilder(location, string, ModelTemplates.FLAT_HANDHELD_ITEM);
      this.builders.add(builder);
      return builder;
   }

   protected ModelBuilder specialRendererItem(Item item) {
      return this.specialRendererItem(Registration.ITEMS.getRegistrar().getId(item), null);
   }

   protected ModelBuilder specialRendererItem(ResourceLocation location) {
      return this.specialRendererItem(location, null);
   }

   protected ModelBuilder specialRendererItem(ResourceLocation location, String string) {
      ModelTemplate template = new ModelTemplate(Optional.of(new ResourceLocation("builtin/entity")), Optional.empty(), TextureSlot.LAYER0);
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
      protected final TransformsBuilder transforms = new TransformsBuilder(this);
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
         String string2 = this.id.getNamespace();
         String string3 = this.id.getPath();
         return new ResourceLocation(string2, "item/" + string3 + (this.string != null ? (this.isAddingStringToFileName ? this.string : "") : ""));
      }

      public ResourceLocation getItemTextureLocation() {
         String string2 = this.id.getNamespace();
         String string3 = this.id.getPath();
         return new ResourceLocation(string2, "item/" + string3 + (this.string != null ? this.string : ""));
      }

      private ModelBuilder self() {
         return this;
      }

      public TransformsBuilder transforms() {
         return this.transforms;
      }

      public ModelBuilder guiLight(BlockModel.GuiLight guiLight) {
         this.guiLight = guiLight;
         return this;
      }

      public ModelBuilder addStringToFileName() {
         this.isAddingStringToFileName = true;
         return this;
      }

      public OverrideBuilder override() {
         OverrideBuilder ret = new OverrideBuilder(this);
         this.overrides.add(ret);
         return ret;
      }

      public OverrideBuilder override(int index) {
         Preconditions.checkElementIndex(index, this.overrides.size(), "override");
         return this.overrides.get(index);
      }

      public JsonObject toJson() {
         JsonObject root = new JsonObject();
         if (!this.overrides.isEmpty()) {
            JsonArray overridesJson = new JsonArray();
            Stream<JsonObject> stream2 = this.overrides.stream().map(OverrideBuilder::toJson);
            Objects.requireNonNull(overridesJson);
            stream2.forEach(overridesJson::add);
            root.add("overrides", overridesJson);
         }

         Map<ItemDisplayContext, ItemTransform> transforms = this.transforms.build();
         if (!transforms.isEmpty()) {
            JsonObject display = new JsonObject();

            for (Map.Entry<ItemDisplayContext, ItemTransform> e : transforms.entrySet()) {
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
         return (float) ((int) f) == f ? (float) ((int) f) : f;
      }

      public class OverrideBuilder {
         protected final Map<ResourceLocation, Float> predicates = new LinkedHashMap<>();
         protected ResourceLocation model;

         public OverrideBuilder(ModelBuilder this$0) {
         }

         public OverrideBuilder predicate(ResourceLocation location, float value) {
            this.predicates.put(location, value);
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
            this.predicates.forEach((key, val) -> predicatesJson.addProperty(key.toString(), val));
            ret.add("predicate", predicatesJson);
            ret.addProperty("model", this.model.toString());
            return ret;
         }
      }

      public class TransformsBuilder {
         private final Map<ItemDisplayContext, TransformVecBuilder> transforms = new LinkedHashMap<>();

         public TransformsBuilder(ModelBuilder builder) {
         }

         public TransformVecBuilder transform(ItemDisplayContext type) {
            Preconditions.checkNotNull(type, "Perspective cannot be null");
            return this.transforms.computeIfAbsent(type, (x$0) -> new TransformVecBuilder(this, x$0));
         }

         Map<ItemDisplayContext, ItemTransform> build() {
            return this.transforms.entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, (e) -> e.getValue().build(), (k1, k2) -> {
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

            TransformVecBuilder(TransformsBuilder this$1, ItemDisplayContext type) {
               this.rotation = new Vector3f(ItemTransformDeserializer.DEFAULT_ROTATION);
               this.translation = new Vector3f(ItemTransformDeserializer.DEFAULT_TRANSLATION);
               this.scale = new Vector3f(ItemTransformDeserializer.DEFAULT_SCALE);
            }

            public TransformVecBuilder rotation(float x, float y, float z) {
               this.rotation = new Vector3f(x, y, z);
               return this;
            }

            public TransformVecBuilder leftRotation(float x, float y, float z) {
               return this.rotation(x, y, z);
            }

            public TransformVecBuilder translation(float x, float y, float z) {
               this.translation = new Vector3f(x, y, z);
               return this;
            }

            public TransformVecBuilder scale(float sc) {
               return this.scale(sc, sc, sc);
            }

            public TransformVecBuilder scale(float x, float y, float z) {
               this.scale = new Vector3f(x, y, z);
               return this;
            }

            ItemTransform build() {
               return new ItemTransform(this.rotation, this.translation, this.scale);
            }

            public TransformsBuilder end() {
               return TransformsBuilder.this;
            }
         }
      }
   }
}
