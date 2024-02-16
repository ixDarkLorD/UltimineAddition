package net.ixdarklord.ultimine_addition.datagen.language;

import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricLanguageProvider;
import net.ixdarklord.ultimine_addition.core.UltimineAddition;
import net.ixdarklord.ultimine_addition.datagen.language.builder.LanguageBuilder;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.item.Item;
import org.jetbrains.annotations.NotNull;

public class LanguageGenerator extends FabricLanguageProvider {
    public LanguageGenerator(FabricDataGenerator generator, String locale) {
        super(generator, locale);
    }

    @Override
    public void generateTranslations(TranslationBuilder translationBuilder) {
        LanguageBuilder.INSTANCE.getTranslations().forEach((key, value) -> {
            if (key instanceof String string) {
                translationBuilder.add(string, value);
            } else if (key instanceof Item item) {
                translationBuilder.add(item, value);
            } else if (key instanceof MobEffect mobEffect) {
                translationBuilder.add(mobEffect, value);
            }
        });
    }

    @Override
    public @NotNull String getName() {
        return String.format("%s %s", UltimineAddition.MOD_NAME, super.getName());
    }
}
