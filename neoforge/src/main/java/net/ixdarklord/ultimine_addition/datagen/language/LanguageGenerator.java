package net.ixdarklord.ultimine_addition.datagen.language;

import net.ixdarklord.ultimine_addition.core.FTBUltimineAddition;
import net.ixdarklord.ultimine_addition.datagen.language.builder.LanguageBuilder;
import net.minecraft.data.PackOutput;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.common.data.LanguageProvider;
import org.jetbrains.annotations.NotNull;

public class LanguageGenerator extends LanguageProvider {

    public LanguageGenerator(PackOutput output, String local) {
        super(output, FTBUltimineAddition.MOD_ID, local);
    }

    @Override
    protected void addTranslations() {
        LanguageBuilder.INSTANCE.getTranslations().forEach((key, value) -> {
            if (key instanceof String string) {
                add(string, value);
            } else if (key instanceof Item item) {
                add(item, value);
            } else if (key instanceof MobEffect mobEffect) {
                add(mobEffect, value);
            }
        });
    }

    @Override
    public @NotNull String getName() {
        return String.format("%s %s", FTBUltimineAddition.MOD_NAME, super.getName());
    }
}
