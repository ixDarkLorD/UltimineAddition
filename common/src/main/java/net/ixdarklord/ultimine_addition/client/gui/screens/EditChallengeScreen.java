package net.ixdarklord.ultimine_addition.client.gui.screens;

import net.ixdarklord.coolcatcanvas.api.client.gui.components.ColorableImageButton;
import net.ixdarklord.coolcatcanvas.api.client.utils.RenderUtils;
import net.ixdarklord.ultimine_addition.common.data.challenge.ChallengeData;
import net.ixdarklord.ultimine_addition.common.data.challenge.ChallengesManager;
import net.ixdarklord.ultimine_addition.common.data.item.MiningSkillCardData;
import net.ixdarklord.ultimine_addition.common.data.item.SkillsRecordData;
import net.ixdarklord.ultimine_addition.config.UAClientConfig;
import net.ixdarklord.ultimine_addition.core.FTBUltimineAddition;
import net.ixdarklord.ultimine_addition.network.PayloadHandler;
import net.ixdarklord.ultimine_addition.network.payloads.SkillsRecordPayload;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.ARGB;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.glfw.GLFW;

import java.util.List;
import java.util.Optional;

// Operator tool (Skills Record edit mode): set a challenge's progress. Framed like the Skills Record (the book's
// header and recessed screen, tinted with its color), over the dimmed book.
public class EditChallengeScreen extends Screen {
    private static final Identifier FRAME_SPRITE = FTBUltimineAddition.id("container/skills_record/card_viewer/frame");
    // The frame's border: its screen starts 9px in from the sides, 20px below the top (the header), 8px above the bottom.
    private static final int FRAME_SIDE = 9, FRAME_TOP = 20, FRAME_BOTTOM = 8;
    private static final int WIDTH = 236, HEIGHT = 196;
    private static final int PAD = FRAME_SIDE + 5;
    private static final int SLOT = 20;
    private static final int BAR_HEIGHT = 8;
    private static final int MAX_TARGETS = 9;
    private static final int BUTTON_HEIGHT = 16;

    private final SkillsRecordScreen parent;
    private final MiningSkillCardData.Challenge challenge;
    private final Component name;
    private final Component info;
    private final List<ItemStack> targets;
    private int leftPos, topPos;
    private EditBox valueBox;
    private ColorableImageButton doneButton;
    private boolean draggingBar;
    private int copiedTicks;

    public EditChallengeScreen(@NotNull SkillsRecordScreen parent, MiningSkillCardData.Challenge challenge) {
        super(Component.translatable("gui.ultimine_addition.skills_record.edit.title"));
        this.parent = parent.lock(true);
        this.challenge = challenge;

        Identifier id = challenge.getId();
        String key = "challenge.%s.%s.name".formatted(id.getNamespace(), id.getPath().replace('/', '.'));
        this.name = I18n.exists(key) ? Component.translatable(key) : Component.translatable("challenge.ultimine_addition.title", challenge.getOrder());
        MiningSkillCardData card = parent.getSelectedCard();
        Component tier = card == null ? Component.empty() : Component.literal(" · ").append(card.getTier().getDisplayName());
        this.info = Component.translatable("challenge.ultimine_addition.title", challenge.getOrder()).withStyle(ChatFormatting.GRAY).append(tier);
        Optional<ChallengeData> data = ChallengesManager.INSTANCE.getChallengeData(id);
        this.targets = data.map(d -> ChallengesManager.INSTANCE.utilizeTargetedBlocks(d).stream().map(Block::asItem)
                .map(item -> item.getDefaultInstance()).filter(stack -> !stack.isEmpty()).limit(MAX_TARGETS).toList()).orElse(List.of());
    }

    // --- Layout (rows inside the frame's screen) ---

    private int centerX() {
        return this.leftPos + WIDTH / 2;
    }

    private int targetsY() {
        return this.topPos + 46;
    }

    private int valueRowY() {
        return this.topPos + 72;
    }

    private int quickRowY() {
        return this.topPos + 93;
    }

    private int barLeft() {
        return this.leftPos + PAD;
    }

    private int barRight() {
        return this.leftPos + WIDTH - PAD;
    }

    private int barTop() {
        return this.topPos + 120;
    }

    private int footerY() {
        return this.topPos + HEIGHT - FRAME_BOTTOM - BUTTON_HEIGHT - 6;
    }

    // The value row: [-] [field] [+] "/ required", centred as a whole.
    private int valueRowWidth() {
        return 16 + 4 + 48 + 4 + 16 + 6 + this.font.width("/ " + this.required());
    }

    private int valueRowLeft() {
        return this.centerX() - this.valueRowWidth() / 2;
    }

    // --- Value ---

    private int required() {
        return this.challenge.getRequiredPoints();
    }

    private int current() {
        return this.challenge.getCurrentPoints();
    }

    private int value() {
        try {
            return Mth.clamp(Integer.parseInt(this.valueBox.getValue()), 0, this.required());
        } catch (NumberFormatException e) {
            return this.current();
        }
    }

    private void setValue(int value) {
        this.valueBox.setValue(String.valueOf(Mth.clamp(value, 0, this.required())));
    }

    private int step() {
        return Minecraft.getInstance().hasShiftDown() ? 10 : 1;
    }

    @Override
    protected void init() {
        this.parent.width = this.width;
        this.parent.height = this.height;
        this.parent.init();
        this.leftPos = (this.width - WIDTH) / 2;
        this.topPos = (this.height - HEIGHT) / 2;

        int rowX = this.valueRowLeft(), rowY = this.valueRowY();
        this.addRenderableWidget(new Button(rowX, rowY, 16, Component.literal("−"), b -> this.setValue(this.value() - this.step()))
                .withTooltip(Component.translatable("gui.ultimine_addition.skills_record.edit.step")));
        this.valueBox = this.addRenderableWidget(new EditBox(this.font, rowX + 20 + 3, rowY + 4, 44, 10, Component.translatable("gui.ultimine_addition.skills_record.edit.new_value", this.required())));
        this.valueBox.setBordered(false);
        this.valueBox.setMaxLength(String.valueOf(this.required()).length());
        this.valueBox.setValue(String.valueOf(this.current()));
        this.valueBox.setTooltip(Tooltip.create(Component.translatable("gui.ultimine_addition.skills_record.edit.new_value", this.required())));
        this.valueBox.setResponder(s -> {
            String digits = s.replaceAll("[^0-9]", "");
            if (!digits.equals(s)) this.valueBox.setValue(digits);
        });
        this.addRenderableWidget(new Button(rowX + 20 + 48 + 4, rowY, 16, Component.literal("+"), b -> this.setValue(this.value() + this.step()))
                .withTooltip(Component.translatable("gui.ultimine_addition.skills_record.edit.step")));

        // Quick values, centred under the field.
        int quickWidth = 44, gap = 4;
        int quickX = this.centerX() - (3 * quickWidth + 2 * gap) / 2;
        this.addRenderableWidget(new Button(quickX, this.quickRowY(), quickWidth, Component.translatable("gui.ultimine_addition.skills_record.edit.reset"), b -> this.setValue(0)));
        this.addRenderableWidget(new Button(quickX + quickWidth + gap, this.quickRowY(), quickWidth, Component.translatable("gui.ultimine_addition.skills_record.edit.half"), b -> this.setValue(this.required() / 2)));
        this.addRenderableWidget(new Button(quickX + 2 * (quickWidth + gap), this.quickRowY(), quickWidth, Component.translatable("gui.ultimine_addition.skills_record.edit.complete"), b -> this.setValue(this.required())));

        int footerWidth = 56;
        int footerX = this.centerX() - (2 * footerWidth + gap) / 2;
        this.doneButton = this.addRenderableWidget(new Button(footerX, this.footerY(), footerWidth, CommonComponents.GUI_DONE, b -> this.apply()));
        this.addRenderableWidget(new Button(footerX + footerWidth + gap, this.footerY(), footerWidth, CommonComponents.GUI_CANCEL, b -> this.onClose()));
        this.setInitialFocus(this.valueBox);
    }

    private void apply() {
        if (this.value() == this.current()) return;
        SkillsRecordData data = this.parent.getMenu().getData();
        data.getCardData(this.parent.selectedSlot).ifPresent(card -> {
            card.setAmount(this.challenge.getId(), this.value()).save();
            data.save();
            PayloadHandler.sendToServer(new SkillsRecordPayload.EditChallenge(this.parent.selectedSlot, this.challenge.getId(), this.value()));
            if (this.minecraft != null && this.minecraft.player != null) this.minecraft.player.playSound(SoundEvents.PLAYER_LEVELUP, 0.7F, 1.5F);
            this.onClose();
        });
    }

    @Override
    protected void clearWidgets() {
        super.clearWidgets();
        this.parent.clearWidgets();
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        // The Skills Record stays visible behind the dialog, dimmed; the dialog starts a new layer over it.
        this.parent.extractRenderStateWithTooltipAndSubtitles(graphics, -1, -1, partialTick);
        graphics.nextStratum();
        graphics.fill(0, 0, this.width, this.height, 0x90000000);
        graphics.fill(this.leftPos + 3, this.topPos + 3, this.leftPos + WIDTH + 3, this.topPos + HEIGHT + 3, 0x50000000);
        graphics.blitSprite(RenderPipelines.GUI_TEXTURED, FRAME_SPRITE, this.leftPos, this.topPos, WIDTH, HEIGHT, UAClientConfig.backgroundColor().argb());
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        super.extractRenderState(graphics, mouseX, mouseY, partialTick);
        int value = this.value();
        int current = this.current();
        this.doneButton.active = value != current;
        this.valueBox.setTextColor(value > current ? 0xFF6BCB6B : value < current ? 0xFFE0584F : 0xFFFFFFFF);

        int x = this.leftPos + PAD;
        int right = this.leftPos + WIDTH - PAD;
        // Header: the dialog's title in the book's label color, like the Skills Record's own title.
        graphics.text(this.font, Component.literal("✎ ").append(this.title), this.leftPos + FRAME_SIDE + 1, this.topPos + 7,
                ARGB.opaque(UAClientConfig.labelColor()), false);

        // The challenge's name, then its number/tier and ID.
        Component nameLine = Component.literal("📝 ").append(this.name);
        graphics.text(this.font, this.font.plainSubstrByWidth(nameLine.getString(), right - x), x, this.topPos + FRAME_TOP + 5, RenderUtils.textColor(0xFBF1C1), true);
        graphics.text(this.font, this.info, x, this.topPos + FRAME_TOP + 16, 0xFFFFFFFF, true);
        String id = this.challenge.getId().toString();
        int idWidth = this.idWidth();
        boolean overId = this.isOverId(mouseX, mouseY);
        graphics.text(this.font, this.font.plainSubstrByWidth(id, idWidth), right - idWidth, this.topPos + FRAME_TOP + 16, overId ? 0xFFFFFFFF : 0xFF8A8A8A, false);
        if (overId) {
            graphics.setTooltipForNextFrame(this.font, this.copiedTicks > 0
                    ? Component.translatable("gui.ultimine_addition.skills_record.edit.copy_success").withStyle(ChatFormatting.GREEN)
                    : Component.translatable("gui.ultimine_addition.skills_record.edit.copy_id").withStyle(ChatFormatting.GRAY), mouseX, mouseY);
        }
        if (this.copiedTicks > 0) this.copiedTicks--;

        // Target blocks, centred.
        ItemStack hovered = null;
        int targetsWidth = this.targets.size() * (SLOT + 2) - 2;
        int tx = this.centerX() - targetsWidth / 2;
        for (int i = 0; i < this.targets.size(); i++) {
            int sx = tx + i * (SLOT + 2), sy = this.targetsY();
            boolean over = mouseX >= sx && mouseX < sx + SLOT && mouseY >= sy && mouseY < sy + SLOT;
            graphics.fill(sx, sy, sx + SLOT, sy + SLOT, over ? 0xFFFFFFFF : 0xFF5A5A5A);
            graphics.fill(sx + 1, sy + 1, sx + SLOT - 1, sy + SLOT - 1, 0xFF2A2A2A);
            graphics.item(this.targets.get(i), sx + 2, sy + 2);
            if (over) hovered = this.targets.get(i);
        }

        // The value field's frame and "/ required", in the centred row.
        int fieldX = this.valueRowLeft() + 20, rowY = this.valueRowY();
        graphics.fill(fieldX, rowY, fieldX + 48, rowY + BUTTON_HEIGHT, this.valueBox.isFocused() ? 0xFFFFFFFF : 0xFF7A7A7A);
        graphics.fill(fieldX + 1, rowY + 1, fieldX + 47, rowY + BUTTON_HEIGHT - 1, 0xFF0C0C0C);
        graphics.text(this.font, "/ " + this.required(), fieldX + 48 + 4 + 16 + 6, rowY + 4, 0xFFAAAAAA, true);

        // The bar previews the change; click or drag it to set the value.
        int bl = this.barLeft(), br = this.barRight(), bt = this.barTop();
        int width = br - bl;
        int currentX = bl + Math.round(width * current / (float) Math.max(1, this.required()));
        int valueX = bl + Math.round(width * value / (float) Math.max(1, this.required()));
        graphics.fill(bl - 1, bt - 1, br + 1, bt + BAR_HEIGHT + 1, 0xFF5A5A5A);
        graphics.fill(bl, bt, br, bt + BAR_HEIGHT, 0xFF0C0C0C);
        graphics.fill(bl, bt, Math.min(currentX, valueX), bt + BAR_HEIGHT, 0xFF4F7FA0);
        if (valueX > currentX) graphics.fill(currentX, bt, valueX, bt + BAR_HEIGHT, 0xFF6BCB6B);
        if (valueX < currentX) graphics.fill(valueX, bt, currentX, bt + BAR_HEIGHT, 0xFF8A2E2E);
        graphics.fill(valueX - 1, bt - 2, valueX + 1, bt + BAR_HEIGHT + 2, 0xFFFFFFFF);
        graphics.text(this.font, Component.translatable("gui.ultimine_addition.skills_record.edit.progress",
                Math.round(100.0F * value / Math.max(1, this.required()))), bl, bt + BAR_HEIGHT + 4, 0xFF8A8A8A, false);

        // What Done would change, under the bar on the right.
        int delta = value - current;
        Component change = delta == 0 ? Component.translatable("gui.ultimine_addition.skills_record.edit.no_change").withStyle(ChatFormatting.GRAY)
                : Component.literal((delta > 0 ? "+" : "") + delta).withStyle(delta > 0 ? ChatFormatting.GREEN : ChatFormatting.RED)
                .append(Component.literal("  (" + current + " → " + value + ")").withStyle(ChatFormatting.GRAY));
        graphics.text(this.font, change, br - this.font.width(change), bt + BAR_HEIGHT + 16, 0xFFFFFFFF, true);

        if (hovered != null) graphics.setTooltipForNextFrame(this.font, hovered.getHoverName(), mouseX, mouseY);
    }

    private int idWidth() {
        int right = this.leftPos + WIDTH - PAD;
        return Math.max(0, Math.min(this.font.width(this.challenge.getId().toString()), right - this.leftPos - PAD - this.font.width(this.info) - 8));
    }

    private boolean isOverId(double mouseX, double mouseY) {
        int right = this.leftPos + WIDTH - PAD;
        int y = this.topPos + FRAME_TOP + 15;
        return mouseX >= right - this.idWidth() && mouseX < right && mouseY >= y && mouseY < y + 10;
    }

    private boolean isOverBar(double mouseX, double mouseY) {
        return mouseX >= this.barLeft() - 2 && mouseX <= this.barRight() + 2 && mouseY >= this.barTop() - 3 && mouseY <= this.barTop() + BAR_HEIGHT + 3;
    }

    private void setFromBar(double mouseX) {
        float t = (float) Mth.clamp((mouseX - this.barLeft()) / (double) (this.barRight() - this.barLeft()), 0.0, 1.0);
        this.setValue(Math.round(t * this.required()));
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        if (super.mouseClicked(event, doubleClick)) return true;
        if (event.button() == GLFW.GLFW_MOUSE_BUTTON_1 && this.isOverId(event.x(), event.y())) {
            if (this.minecraft != null) this.minecraft.keyboardHandler.setClipboard(this.challenge.getId().toString());
            this.copiedTicks = 80;
            return true;
        }
        if (event.button() == GLFW.GLFW_MOUSE_BUTTON_1 && this.isOverBar(event.x(), event.y())) {
            this.draggingBar = true;
            this.setFromBar(event.x());
            return true;
        }
        if (event.button() == GLFW.GLFW_MOUSE_BUTTON_2 && this.valueBox.isHovered()) {
            this.valueBox.setValue("");
            return true;
        }
        return false;
    }

    @Override
    public boolean mouseDragged(MouseButtonEvent event, double dragX, double dragY) {
        if (this.draggingBar) {
            this.setFromBar(event.x());
            return true;
        }
        return super.mouseDragged(event, dragX, dragY);
    }

    @Override
    public boolean mouseReleased(MouseButtonEvent event) {
        this.draggingBar = false;
        return super.mouseReleased(event);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        if (super.mouseScrolled(mouseX, mouseY, scrollX, scrollY)) return true;
        if (this.valueBox.isHoveredOrFocused() || this.isOverBar(mouseX, mouseY)) {
            this.setValue(this.value() + (int) Math.signum(scrollY) * this.step());
            return true;
        }
        return false;
    }

    @Override
    public boolean keyPressed(KeyEvent event) {
        if (super.keyPressed(event)) return true;
        if (event.key() == GLFW.GLFW_KEY_ENTER || event.key() == GLFW.GLFW_KEY_KP_ENTER) {
            this.apply();
            return true;
        }
        return false;
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public void onClose() {
        if (this.minecraft != null) this.minecraft.setScreen(this.parent.lock(false));
    }

    // The Skills Record's tinted buttons, with their label drawn.
    private static final class Button extends ColorableImageButton {
        private Button(int x, int y, int width, Component label, OnPress onPress) {
            super(x, y, width, BUTTON_HEIGHT, SkillsRecordScreen.BUTTON_SPRITES, onPress, label);
            this.setColor(UAClientConfig.backgroundColor().convert());
        }

        private Button withTooltip(@Nullable Component tooltip) {
            if (tooltip != null) this.setTooltip(Tooltip.create(tooltip));
            return this;
        }

        @Override
        public void extractContents(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
            super.extractContents(graphics, mouseX, mouseY, partialTick);
            this.extractDefaultLabel(graphics.textRendererForWidget(this, GuiGraphicsExtractor.HoveredTextEffects.NONE));
        }
    }
}
