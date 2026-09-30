package net.ixdarklord.ultimine_addition.integration.jei;

import mezz.jei.api.ingredients.subtypes.ISubtypeInterpreter;
import mezz.jei.api.ingredients.subtypes.UidContext;
import mezz.jei.api.registration.ISubtypeRegistration;
import net.ixdarklord.ultimine_addition.common.data.item.ShapeCertificateData;
import net.ixdarklord.ultimine_addition.common.item.ModItems;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

// Items whose variants live in components: without these JEI counts every variant as one item and lists only one.
public final class ItemVariantInterpreters {
    private ItemVariantInterpreters() {
    }

    public static void init(ISubtypeRegistration registration) {
        registration.registerSubtypeInterpreter(ModItems.SKILLS_RECORD, new SkillsRecordInterpreter());
        ShapeCertificateInterpreter certificates = new ShapeCertificateInterpreter();
        registration.registerSubtypeInterpreter(ModItems.SHAPE_CERTIFICATE_NOVICE, certificates);
        registration.registerSubtypeInterpreter(ModItems.SHAPE_CERTIFICATE_APPRENTICE, certificates);
        registration.registerSubtypeInterpreter(ModItems.SHAPE_CERTIFICATE_ADEPT, certificates);
    }

    // One per dye edition; an undyed record is the white one.
    private static final class SkillsRecordInterpreter implements ISubtypeInterpreter<ItemStack> {
        @Override
        public @Nullable Object getSubtypeData(ItemStack stack, UidContext context) {
            DyeColor color = stack.get(DataComponents.BASE_COLOR);
            return color == null || color == DyeColor.WHITE ? null : color;
        }
    }

    // One per tool and shape taught.
    private static final class ShapeCertificateInterpreter implements ISubtypeInterpreter<ItemStack> {
        @Override
        public @Nullable Object getSubtypeData(ItemStack stack, UidContext context) {
            String tool = ShapeCertificateData.getTool(stack);
            var shape = ShapeCertificateData.getShape(stack);
            return tool == null && shape == null ? null : tool + "|" + shape;
        }
    }
}
