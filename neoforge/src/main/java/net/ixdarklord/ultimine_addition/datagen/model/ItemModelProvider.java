package net.ixdarklord.ultimine_addition.datagen.model;

import net.ixdarklord.ultimine_addition.core.Registration;
import net.minecraft.client.renderer.block.model.BlockModel;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.client.model.generators.ItemModelBuilder;
import net.neoforged.neoforge.client.model.generators.ModelFile;
import net.neoforged.neoforge.client.model.generators.ModelProvider;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public abstract class ItemModelProvider extends ModelProvider<ItemModelBuilder> {
    public ItemModelProvider(PackOutput output, String modid, ExistingFileHelper existingFileHelper) {
        super(output, modid, ITEM_FOLDER, ItemModelBuilder::new, existingFileHelper);
        this.registerModels();
    }

    protected abstract void registerModels();
    
    protected ItemModelBuilder simpleItem(Item item) {
        return this.simpleItem(item, null, true);
    }

    protected ItemModelBuilder simpleItem(Item item, String string) {
        return this.simpleItem(item, string, true);
    }

    protected ItemModelBuilder simpleItem(Item item, String string, boolean addSuffixToName) {
        return this.simpleItem(Objects.requireNonNull(Registration.ITEMS.getRegistrar().getId(item)), string, addSuffixToName);
    }

    protected ItemModelBuilder simpleItem(ResourceLocation location, String string) {
        return this.simpleItem(location, string, true);
    }

    protected ItemModelBuilder simpleItem(ResourceLocation location, String string, boolean addSuffixToName) {
        String path = this.addSuffixToResourceLocation(location, string).getPath().contains("/") ? "" : "item/";
        path = path + this.addSuffixToResourceLocation(location, string).getPath();

        return getBuilder(this.addSuffixToResourceLocation(location, addSuffixToName ? string : null).toString())
                .parent(new ModelFile.UncheckedModelFile("item/generated"))
                .texture("layer0", ResourceLocation.fromNamespaceAndPath(location.getNamespace(), path));
    }

    protected ItemModelBuilder handheldItem(Item item) {
        return this.handheldItem(Registration.ITEMS.getRegistrar().getId(item), null);
    }

    protected ItemModelBuilder handheldItem(ResourceLocation location, String string) {
        return getBuilder(this.addSuffixToResourceLocation(location, string).toString())
                .parent(new ModelFile.UncheckedModelFile("item/handheld"))
                .texture("layer0", ResourceLocation.fromNamespaceAndPath(location.getNamespace(), "item/" + this.addSuffixToResourceLocation(location, string).getPath()));
    }

    protected ItemModelBuilder specialRendererItem(Item item) {
        return this.specialRendererItem(Registration.ITEMS.getRegistrar().getId(item), null);
    }

    protected ItemModelBuilder specialRendererItem(ResourceLocation location) {
        return this.specialRendererItem(location, null);
    }

    protected ItemModelBuilder specialRendererItem(ResourceLocation location, String string) {
        return getBuilder(this.addSuffixToResourceLocation(location, string).toString())
                .parent(new ModelFile.UncheckedModelFile("builtin/entity"))
                .guiLight(BlockModel.GuiLight.FRONT);
    }

    private ResourceLocation addSuffixToResourceLocation(ResourceLocation location, String string) {
        ResourceLocation rl = location;
        if (string != null) {
            rl = ResourceLocation.parse(location.toString() + string);
        }
        return rl;
    }

    public final @NotNull String getName() {
        return "Item Models";
    }
}
