package net.ixdarklord.ultimine_addition.client.gui.screens;

import net.ixdarklord.ultimine_addition.client.gui.theme.RecordTheme;
import net.ixdarklord.ultimine_addition.client.gui.GuiDraw;
import net.ixdarklord.coolcatcanvas.api.client.gui.components.ColorableImageButton;
import net.ixdarklord.coolcatcanvas.api.client.utils.RenderUtils;
import net.ixdarklord.ultimine_addition.common.data.challenge.ChallengeData;
import net.ixdarklord.ultimine_addition.common.data.challenge.ChallengesManager;
import net.ixdarklord.ultimine_addition.common.data.item.MiningSkillCardData;
import net.ixdarklord.ultimine_addition.common.data.item.SkillsRecordData;
import net.ixdarklord.ultimine_addition.core.FTBUltimineAddition;
import net.ixdarklord.ultimine_addition.network.PayloadHandler;
import net.ixdarklord.ultimine_addition.network.payloads.SkillsRecordPayload;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
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
    private static final ResourceLocation FRAME_SPRITE = FTBUltimineAddition.id("container/skills_record/card_viewer/frame");
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

        ResourceLocation id = challenge.getId();
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

    // Rows, top to bottom: name, number and tier, target blocks, the value row, quick values, the bar and its
    // percentage, then what Done would change and the footer buttons.
    private int nameY() {
        return this.topPos + FRAME_TOP + 6;
    }

    private int infoY() {
        return this.nameY() + 11;
    }

    private int targetsY() {
        return this.infoY() + 13;
    }

    private int valueRowY() {
        return this.targetsY() + (this.targets.isEmpty() ? 0 : SLOT + 8);
    }

    private int quickRowY() {
        return this.valueRowY() + BUTTON_HEIGHT + 5;
    }

    private int barLeft() {
        return this.leftPos + PAD;
    }

    private int barRight() {
        return this.leftPos + WIDTH - PAD;
    }

    private int barTop() {
        return this.quickRowY() + BUTTON_HEIGHT + 9;
    }

    private int changeY() {
        return this.footerY() - 12;
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
        return Screen.hasShiftDown() ? 10 : 1;
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
        this.valueBox = this.addRenderableWidget(new CenteredEditBox(this.font, rowX + 20 + 3, rowY + 4, 44, 10, Component.translatable("gui.ultimine_addition.skills_record.edit.new_value", this.required())));
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

        // Quick values, centred under the field, all as wide as the longest label.
        Component reset = Component.translatable("gui.ultimine_addition.skills_record.edit.reset");
        Component half = Component.translatable("gui.ultimine_addition.skills_record.edit.half");
        Component complete = Component.translatable("gui.ultimine_addition.skills_record.edit.complete");
        int gap = 4;
        int quickWidth = Math.min((WIDTH - PAD * 2 - 2 * gap) / 3,
                Math.max(44, Math.max(this.font.width(reset), Math.max(this.font.width(half), this.font.width(complete))) + 10));
        int quickX = this.centerX() - (3 * quickWidth + 2 * gap) / 2;
        this.addRenderableWidget(new Button(quickX, this.quickRowY(), quickWidth, reset, b -> this.setValue(0)));
        this.addRenderableWidget(new Button(quickX + quickWidth + gap, this.quickRowY(), quickWidth, half, b -> this.setValue(this.required() / 2)));
        this.addRenderableWidget(new Button(quickX + 2 * (quickWidth + gap), this.quickRowY(), quickWidth, complete, b -> this.setValue(this.required())));

        int footerWidth = Math.max(56, Math.max(this.font.width(CommonComponents.GUI_DONE), this.font.width(CommonComponents.GUI_CANCEL)) + 12);
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

    // 1.20.1 screens draw their background themselves (render calls this first).
    private void renderDialogBackground(GuiGraphics graphics, float partialTick) {
        // The Skills Record stays visible behind the dialog, dimmed; the dialog starts a new layer over it.
        this.parent.render(graphics, -1, -1, partialTick);
        GuiDraw.nextStratum(graphics);
        graphics.fill(0, 0, this.width, this.height, 0x90000000);
        graphics.fill(this.leftPos + 3, this.topPos + 3, this.leftPos + WIDTH + 3, this.topPos + HEIGHT + 3, 0x50000000);
        GuiDraw.blitSprite(graphics, FRAME_SPRITE, this.leftPos, this.topPos, WIDTH, HEIGHT, RecordTheme.active().overlay().argb());
        // The value field's frame, under the field's text.
        int fieldX = this.valueRowLeft() + 20, rowY = this.valueRowY();
        graphics.fill(fieldX, rowY, fieldX + 48, rowY + BUTTON_HEIGHT, this.valueBox != null && this.valueBox.isFocused() ? 0xFFFFFFFF : 0xFF7A7A7A);
        graphics.fill(fieldX + 1, rowY + 1, fieldX + 47, rowY + BUTTON_HEIGHT - 1, 0xFF0C0C0C);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        this.renderDialogBackground(graphics, partialTick);
        super.render(graphics, mouseX, mouseY, partialTick);
        int value = this.value();
        int current = this.current();
        this.doneButton.active = value != current;
        this.valueBox.setTextColor(value > current ? 0xFF6BCB6B : value < current ? 0xFFE0584F : 0xFFFFFFFF);

        int x = this.leftPos + PAD;
        int right = this.leftPos + WIDTH - PAD;
        // Header: the dialog's title in the book's label color, like the Skills Record's own title.
        GuiDraw.text(graphics, this.font, Component.literal("✎ ").append(this.title), this.leftPos + FRAME_SIDE + 1, this.topPos + 7,
                RecordTheme.active().labelColor(), false);

        // The challenge's name (hover it for its ID, click to copy it), then its number and tier, centred.
        Component nameLine = Component.literal("📝 ").append(this.name);
        String name = this.font.plainSubstrByWidth(nameLine.getString(), right - x);
        boolean overName = this.isOverName(mouseX, mouseY);
        GuiDraw.centeredText(graphics, this.font, Component.literal(name).withStyle(overName ? ChatFormatting.UNDERLINE : ChatFormatting.RESET),
                this.centerX(), this.nameY(), RenderUtils.textColor(0xFBF1C1));
        GuiDraw.centeredText(graphics, this.font, this.info, this.centerX(), this.infoY(), 0xFFFFFFFF);
        if (overName) {
            GuiDraw.tooltip(graphics, this.font, List.of(
                    Component.literal(this.challenge.getId().toString()).withStyle(ChatFormatting.DARK_GRAY),
                    this.copiedTicks > 0 ? Component.translatable("gui.ultimine_addition.skills_record.edit.copy_success").withStyle(ChatFormatting.GREEN)
                            : Component.translatable("gui.ultimine_addition.skills_record.edit.copy_id").withStyle(ChatFormatting.GRAY)), mouseX, mouseY);
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
            graphics.renderItem(this.targets.get(i), sx + 2, sy + 2);
            if (over) hovered = this.targets.get(i);
        }

        // "/ required", after the field and its + button.
        int fieldX = this.valueRowLeft() + 20, rowY = this.valueRowY();
        GuiDraw.text(graphics, this.font, "/ " + this.required(), fieldX + 48 + 4 + 16 + 6, rowY + 4, 0xFFAAAAAA, true);

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
        GuiDraw.centeredText(graphics, this.font, Component.translatable("gui.ultimine_addition.skills_record.edit.progress",
                Math.round(100.0F * value / Math.max(1, this.required()))), this.centerX(), bt + BAR_HEIGHT + 4, 0xFF8A8A8A);

        // What Done would change, centred above the footer buttons.
        int delta = value - current;
        Component change = delta == 0 ? Component.translatable("gui.ultimine_addition.skills_record.edit.no_change").withStyle(ChatFormatting.GRAY)
                : Component.literal((delta > 0 ? "+" : "") + delta).withStyle(delta > 0 ? ChatFormatting.GREEN : ChatFormatting.RED)
                .append(Component.literal("  (" + current + " → " + value + ")").withStyle(ChatFormatting.GRAY));
        GuiDraw.centeredText(graphics, this.font, change, this.centerX(), this.changeY(), 0xFFFFFFFF);

        if (hovered != null) GuiDraw.tooltip(graphics, this.font, hovered.getHoverName(), mouseX, mouseY);
        GuiDraw.renderTooltip(graphics);
    }

    private boolean isOverName(double mouseX, double mouseY) {
        int width = Math.min(this.font.width("📝 " + this.name.getString()), WIDTH - PAD * 2);
        return Math.abs(mouseX - this.centerX()) <= width / 2.0 && mouseY >= this.nameY() - 1 && mouseY < this.nameY() + 9;
    }

    private boolean isOverBar(double mouseX, double mouseY) {
        return mouseX >= this.barLeft() - 2 && mouseX <= this.barRight() + 2 && mouseY >= this.barTop() - 3 && mouseY <= this.barTop() + BAR_HEIGHT + 3;
    }

    private void setFromBar(double mouseX) {
        float t = (float) Mth.clamp((mouseX - this.barLeft()) / (double) (this.barRight() - this.barLeft()), 0.0, 1.0);
        this.setValue(Math.round(t * this.required()));
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (super.mouseClicked(mouseX, mouseY, button)) return true;
        if (button == GLFW.GLFW_MOUSE_BUTTON_1 && this.isOverName(mouseX, mouseY)) {
            if (this.minecraft != null) this.minecraft.keyboardHandler.setClipboard(this.challenge.getId().toString());
            this.copiedTicks = 80;
            return true;
        }
        if (button == GLFW.GLFW_MOUSE_BUTTON_1 && this.isOverBar(mouseX, mouseY)) {
            this.draggingBar = true;
            this.setFromBar(mouseX);
            return true;
        }
        if (button == GLFW.GLFW_MOUSE_BUTTON_2 && this.valueBox.isHovered()) {
            this.valueBox.setValue("");
            return true;
        }
        return false;
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        if (this.draggingBar) {
            this.setFromBar(mouseX);
            return true;
        }
        return super.mouseDragged(mouseX, mouseY, button, dragX, dragY);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        this.draggingBar = false;
        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
        if (super.mouseScrolled(mouseX, mouseY, delta)) return true;
        if (this.valueBox.isHoveredOrFocused() || this.isOverBar(mouseX, mouseY)) {
            this.setValue(this.value() + (int) Math.signum(delta) * this.step());
            return true;
        }
        return false;
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (super.keyPressed(keyCode, scanCode, modifiers)) return true;
        if (keyCode == GLFW.GLFW_KEY_ENTER || keyCode == GLFW.GLFW_KEY_KP_ENTER) {
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

    // 1.20.1's EditBox has no centred text (26.1's setCentered): its text is drawn shifted to the middle of the box.
    private static final class CenteredEditBox extends EditBox {
        private final Font font;

        private CenteredEditBox(Font font, int x, int y, int width, int height, Component message) {
            super(font, x, y, width, height, message);
            this.font = font;
        }

        @Override
        public void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
            int x = this.getX();
            int shift = Math.max(0, (this.getWidth() - this.font.width(this.getValue())) / 2);
            this.setX(x + shift);
            try {
                super.renderWidget(graphics, mouseX, mouseY, partialTick);
            } finally {
                this.setX(x);
            }
        }
    }

    // The Skills Record's tinted buttons, with their label drawn.
    private static final class Button extends ColorableImageButton {
        private Button(int x, int y, int width, Component label, OnPress onPress) {
            super(x, y, width, BUTTON_HEIGHT, SkillsRecordScreen.BUTTON_SPRITES, onPress, label);
            this.setColor(RecordTheme.active().overlay().convert());
        }

        private Button withTooltip(@Nullable Component tooltip) {
            if (tooltip != null) this.setTooltip(Tooltip.create(tooltip));
            return this;
        }

        @Override
        public void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
            super.renderWidget(graphics, mouseX, mouseY, partialTick);
            this.renderString(graphics, Minecraft.getInstance().font, (this.active ? 0xFFFFFF : 0xA0A0A0) | Mth.ceil(this.alpha * 255.0F) << 24);
        }
    }
}
