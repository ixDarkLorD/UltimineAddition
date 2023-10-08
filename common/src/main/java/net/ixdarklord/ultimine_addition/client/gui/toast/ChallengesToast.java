package net.ixdarklord.ultimine_addition.client.gui.toast;

import dev.ftb.mods.ftblibrary.icon.Icon;
import dev.ftb.mods.ftblibrary.icon.ItemIcon;
import dev.ftb.mods.ftblibrary.ui.misc.SimpleToast;
import net.ixdarklord.ultimine_addition.common.data.item.MiningSkillCardData;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.ItemStack;

public class ChallengesToast extends SimpleToast {
    private final Component title;
    private final Component desc;
    private final Icon icon;
    private final SoundInstance sound;

    public ChallengesToast(MiningSkillCardData.Identifier identifier, ItemStack stack) {
        this.icon = ItemIcon.getItemIcon(stack);
        if (identifier.id().equals(new ResourceLocation("completed"))) {
            title = Component.translatable("toast.ultimine_addition.challenge.all_completed");
            desc = Component.translatable("toast.ultimine_addition.challenge.all_completed.info", stack.getHoverName());
            sound = SimpleSoundInstance.forUI(SoundEvents.UI_TOAST_CHALLENGE_COMPLETE, 1.0F, 1.0F);
        } else {
            title = Component.translatable("toast.ultimine_addition.challenge.completed");
            desc = Component.translatable("toast.ultimine_addition.challenge.completed.info", identifier.order(), stack.getHoverName());
            sound = SimpleSoundInstance.forUI(SoundEvents.PLAYER_LEVELUP, 1.0F, 1.0F);
        }
    }

    @Override
    public Component getTitle() {
        return title;
    }

    @Override
    public Component getSubtitle() {
        return desc;
    }

    @Override
    public Icon getIcon() {
        return icon;
    }

    @Override
    public void playSound(SoundManager handler) {
        handler.play(sound);
    }
}
