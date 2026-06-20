package net.ixdarklord.ultimine_addition.client.gui.toasts;

import dev.ftb.mods.ftblibrary.icon.Icon;
import dev.ftb.mods.ftblibrary.icon.ItemIcon;
import dev.ftb.mods.ftblibrary.ui.misc.SimpleToast;
import net.ixdarklord.ultimine_addition.common.data.item.MiningSkillCardData;
import net.minecraft.client.Minecraft;
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

    public static void run(MiningSkillCardData.Challenge challenge, ItemStack stack) {
        Minecraft.getInstance().getToasts().addToast(new ChallengesToast(challenge, stack));
    }

    public ChallengesToast(MiningSkillCardData.Challenge challenge, ItemStack stack) {
        this.icon = ItemIcon.getItemIcon(stack);
        if (challenge.getId().equals(new ResourceLocation("completed"))) {
            this.title = Component.translatable("toast.ultimine_addition.challenge.all_completed");
            this.desc = Component.translatable("toast.ultimine_addition.challenge.all_completed.info", stack.getHoverName());
            this.sound = SimpleSoundInstance.forUI(SoundEvents.UI_TOAST_CHALLENGE_COMPLETE, 1.0F, 1.0F);
        } else {
            this.title = Component.translatable("toast.ultimine_addition.challenge.completed");
            this.desc = Component.translatable("toast.ultimine_addition.challenge.completed.info", challenge.getOrder(), stack.getHoverName());
            this.sound = SimpleSoundInstance.forUI(SoundEvents.PLAYER_LEVELUP, 1.0F, 1.0F);
        }

    }

    public Component getTitle() {
        return this.title;
    }

    public Component getSubtitle() {
        return this.desc;
    }

    public Icon getIcon() {
        return this.icon;
    }

    public void playSound(SoundManager handler) {
        handler.play(this.sound);
    }
}
