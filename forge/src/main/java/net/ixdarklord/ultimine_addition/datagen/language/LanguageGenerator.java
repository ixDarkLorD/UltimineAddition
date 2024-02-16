package net.ixdarklord.ultimine_addition.datagen.language;

import net.ixdarklord.ultimine_addition.core.UltimineAddition;
import net.ixdarklord.ultimine_addition.datagen.language.builder.LanguageBuilder;
import net.minecraft.data.PackOutput;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.item.Item;
import net.minecraftforge.common.data.LanguageProvider;

public class LanguageGenerator extends LanguageProvider {
    public LanguageGenerator(PackOutput output, String locale) {
        super(output, UltimineAddition.MOD_ID, locale);
    }

    @Override
    protected void addTranslations() {
        LanguageBuilder.INSTANCE.getTranslations().forEach((key, value) -> {
            if (key instanceof String) {
                add((String) key, value);
            } else if (key instanceof Item) {
                add((Item) key, value);
            } else if (key instanceof MobEffect) {
                add((MobEffect) key, value);
            }
        });
    }
}
