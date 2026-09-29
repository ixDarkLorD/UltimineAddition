package net.ixdarklord.ultimine_addition.client.gui.theme;

import net.ixdarklord.ultimine_addition.client.gui.screens.SkillsRecordScreen;
import net.ixdarklord.ultimine_addition.common.menu.SkillsRecordMenu;
import net.minecraft.client.Minecraft;
import net.ixdarklord.ultimine_addition.core.FTBUltimineAddition;
import net.ixdarklord.ultimine_addition.core.Registration;
import net.ixdarklord.ultimine_addition.core.ServicePlatform;
import net.ixdarklord.ultimine_addition.util.ItemUtils;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ARGB;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

/**
 * The look of a Skills Record, picked by its dye ({@code base_color}): the book's tint, a label color that stays
 * readable on it, an emblem (the motif on its item texture) and the card viewer's background effect. An undyed record
 * is {@link #WHITE}.
 */
public enum RecordTheme {
    WHITE(DyeColor.WHITE, 0xF2F6FF, RecordEffect.SNOW, RecordEmblem.SPIN),
    ORANGE(DyeColor.ORANGE, 0xFFB463, RecordEffect.LEAVES, RecordEmblem.GLOW),
    MAGENTA(DyeColor.MAGENTA, 0xEB93E3, RecordEffect.ALLIUM, RecordEmblem.SWAY),
    LIGHT_BLUE(DyeColor.LIGHT_BLUE, 0x96D6FF, RecordEffect.CLOUDS, RecordEmblem.DRIFT),
    YELLOW(DyeColor.YELLOW, 0xFFE272, RecordEffect.POLLEN, RecordEmblem.FLY),
    LIME(DyeColor.LIME, 0xAEEB63, RecordEffect.SLIME, RecordEmblem.BOUNCE),
    PINK(DyeColor.PINK, 0xFFB6CE, RecordEffect.CHERRY, RecordEmblem.FLUTTER),
    GRAY(DyeColor.GRAY, 0x8E959C, RecordEffect.DUST, RecordEmblem.MINE),
    LIGHT_GRAY(DyeColor.LIGHT_GRAY, 0xCBCFD3, RecordEffect.RAIN, RecordEmblem.DROP),
    CYAN(DyeColor.CYAN, 0x55D2CE, RecordEffect.BUBBLES, RecordEmblem.SWIM),
    PURPLE(DyeColor.PURPLE, 0xB68CF0, RecordEffect.AMETHYST, RecordEmblem.PULSE),
    BLUE(DyeColor.BLUE, 0x7189E8, RecordEffect.CAUSTICS, RecordEmblem.COIN),
    BROWN(DyeColor.BROWN, 0xBB8D63, RecordEffect.SPORES, RecordEmblem.BREATHE),
    GREEN(DyeColor.GREEN, 0x76B352, RecordEffect.FIREFLIES, RecordEmblem.SWAY),
    RED(DyeColor.RED, 0xE36565, RecordEffect.EMBERS, RecordEmblem.FLICKER),
    BLACK(DyeColor.BLACK, 0x4C485C, RecordEffect.STARS, RecordEmblem.TWINKLE);

    // The book's face, where the labels are drawn, before the tint.
    private static final int BOOK_FACE = 0xFFC6C6C6;

    private final DyeColor dye;
    private final int tint;
    private final int label;
    private final RecordEffect effect;
    private final RecordEmblem motion;
    private final Identifier emblem;

    RecordTheme(DyeColor dye, int tint, RecordEffect effect, RecordEmblem motion) {
        this.dye = dye;
        this.tint = tint;
        this.effect = effect;
        this.motion = motion;
        this.label = readableLabel(tint);
        this.emblem = FTBUltimineAddition.id("container/skills_record/emblem/" + dye.getSerializedName());
    }

    public static RecordTheme of(@Nullable DyeColor dye) {
        if (dye == null) return WHITE;
        for (RecordTheme theme : values()) {
            if (theme.dye == dye) return theme;
        }
        return WHITE;
    }

    public static RecordTheme of(ItemStack stack) {
        return stack.is(Registration.SKILLS_RECORD.get()) ? of(stack.get(DataComponents.BASE_COLOR)) : WHITE;
    }

    /**
     * The theme to show right now: the open Skills Record's (also under its settings popup or the challenge editor,
     * while its menu stays open), otherwise {@link #current the one the player uses}.
     */
    public static RecordTheme active() {
        Player player = Minecraft.getInstance().player;
        if (player != null && player.containerMenu instanceof SkillsRecordMenu menu) return of(menu.getRecordColor());
        return current(player);
    }

    /** The record the player uses: the one in hand, else the equipped one, else the first in the inventory. */
    public static RecordTheme current(@Nullable Player player) {
        if (player == null) return WHITE;
        ItemStack held = ItemUtils.findItemInHand(player, Registration.SKILLS_RECORD.get());
        if (!held.isEmpty()) return of(held);
        if (ServicePlatform.get().slotAPI().isModLoaded()) {
            ItemStack equipped = ServicePlatform.get().slotAPI().getSkillsRecordItem(player);
            if (!equipped.isEmpty()) return of(equipped);
        }
        for (ItemStack stack : player.getInventory().getNonEquipmentItems()) {
            if (stack.is(Registration.SKILLS_RECORD.get())) return of(stack);
        }
        return WHITE;
    }

    public DyeColor dye() {
        return this.dye;
    }

    /** The book's tint, opaque ARGB. */
    public int tint() {
        return ARGB.opaque(this.tint);
    }

    /** The tint as the overlay color the Skills Record's panels are drawn with. */
    public SkillsRecordScreen.OverlayColor overlay() {
        return SkillsRecordScreen.OverlayColor.of(this.tint & 0xFFFFFF);
    }

    /** The title and inventory labels' color, opaque ARGB, readable on the tinted book. */
    public int labelColor() {
        return ARGB.opaque(this.label);
    }

    public RecordEffect effect() {
        return this.effect;
    }

    public Identifier emblem() {
        return this.emblem;
    }

    /** How the emblem moves in the card viewer's background. */
    public RecordEmblem motion() {
        return this.motion;
    }

    // A dark shade of the tint, or a light one when that reads better on the tinted book; plain dark or light text
    // when neither is readable enough.
    private static int readableLabel(int tint) {
        int face = ARGB.multiply(BOOK_FACE, ARGB.opaque(tint));
        int dark = ARGB.scaleRGB(ARGB.opaque(tint), 0.22F);
        int light = ARGB.srgbLerp(0.85F, ARGB.opaque(tint), 0xFFFFFFFF);
        double darkContrast = contrast(face, dark), lightContrast = contrast(face, light);
        int best = darkContrast >= lightContrast ? dark : light;
        if (Math.max(darkContrast, lightContrast) >= 4.5) return best & 0xFFFFFF;
        return contrast(face, 0xFF202020) >= contrast(face, 0xFFFFFFFF) ? 0x202020 : 0xFFFFFF;
    }

    private static double contrast(int a, int b) {
        double la = luminance(a), lb = luminance(b);
        return (Math.max(la, lb) + 0.05) / (Math.min(la, lb) + 0.05);
    }

    private static double luminance(int argb) {
        return 0.2126 * linear(ARGB.red(argb)) + 0.7152 * linear(ARGB.green(argb)) + 0.0722 * linear(ARGB.blue(argb));
    }

    private static double linear(int channel) {
        double c = channel / 255.0;
        return c <= 0.04045 ? c / 12.92 : Math.pow((c + 0.055) / 1.055, 2.4);
    }
}
