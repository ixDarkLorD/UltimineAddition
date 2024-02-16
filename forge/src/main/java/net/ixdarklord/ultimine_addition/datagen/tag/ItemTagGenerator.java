package net.ixdarklord.ultimine_addition.datagen.tag;

import net.ixdarklord.ultimine_addition.common.item.ModItems;
import net.ixdarklord.ultimine_addition.common.tag.ModItemTags;
import net.ixdarklord.ultimine_addition.core.UltimineAddition;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.tags.ItemTagsProvider;
import net.minecraftforge.common.Tags;
import net.minecraftforge.common.data.ExistingFileHelper;
import org.jetbrains.annotations.Nullable;

public class ItemTagGenerator extends ItemTagsProvider {
    public ItemTagGenerator(DataGenerator generator, @Nullable ExistingFileHelper existingFileHelper) {
        super(generator, new BlockTagGenerator(generator, existingFileHelper), UltimineAddition.MOD_ID, existingFileHelper);
    }

    @SuppressWarnings("unchecked")
    @Override
    protected void addTags() {
        this.tag(ModItemTags.MINING_SKILL_CARD).add(ModItems.MINING_SKILL_CARD_EMPTY, ModItems.MINING_SKILL_CARD_PICKAXE, ModItems.MINING_SKILL_CARD_AXE, ModItems.MINING_SKILL_CARD_SHOVEL, ModItems.MINING_SKILL_CARD_HOE);
        this.tag(ModItemTags.MORE_VALUABLE_PIGMENT).addTags(Tags.Items.DYES_BLACK, Tags.Items.DYES_RED, Tags.Items.DYES_GREEN, Tags.Items.DYES_BLUE);
        this.tag(ModItemTags.LESS_VALUABLE_PIGMENT).addTags(Tags.Items.DYES_WHITE, Tags.Items.DYES_BROWN, Tags.Items.DYES_CYAN, Tags.Items.DYES_GRAY, Tags.Items.DYES_LIGHT_BLUE, Tags.Items.DYES_LIGHT_GRAY, Tags.Items.DYES_LIME, Tags.Items.DYES_MAGENTA, Tags.Items.DYES_ORANGE, Tags.Items.DYES_PINK, Tags.Items.DYES_PURPLE, Tags.Items.DYES_YELLOW);
    }
}
