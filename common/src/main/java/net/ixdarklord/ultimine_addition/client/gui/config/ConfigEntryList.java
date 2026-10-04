package net.ixdarklord.ultimine_addition.client.gui.config;

import net.ixdarklord.coolcatcore.api.config.ConfigGroup;
import net.ixdarklord.coolcatcore.api.config.ConfigValue;
import net.ixdarklord.coolcatcore.api.config.type.BooleanType;
import net.ixdarklord.coolcatcore.api.config.type.EnumType;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.ContainerObjectSelectionList;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.input.InputWithModifiers;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.input.MouseButtonInfo;
import net.minecraft.locale.Language;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

// The rows of a config popup (from Glazed Menu): settings (name, description, editor, reset) and section headers. The
// list draws no background of its own; it sits on the popup's panel. Booleans get a switch and enums a selector;
// other types are shown but left to the config file.
public final class ConfigEntryList extends ContainerObjectSelectionList<ConfigEntryList.Entry> {
    private static final int TOOLTIP_WIDTH = 260;

    public ConfigEntryList(Minecraft minecraft, int width, int height, int y) {
        super(minecraft, width, height, y, 26);
    }

    @Override
    public int getRowWidth() {
        return this.width - 14;
    }

    @Override
    public int getRowLeft() {
        return this.getX() + 4;
    }

    @Override
    protected int scrollBarX() {
        return this.getRight() - 5;
    }

    public void setEntries(List<Entry> entries) {
        this.clearEntries();
        for (Entry entry : entries) this.addEntry(entry, entry.preferredHeight());
        this.setScrollAmount(0);
    }

    /** Shows the pending values again, after a reset. */
    public void refreshValues() {
        this.children().forEach(Entry::refresh);
    }

    @Override
    protected void extractListBackground(GuiGraphicsExtractor graphics) {}

    @Override
    protected void extractListSeparators(GuiGraphicsExtractor graphics) {}

    @Override
    protected void extractScrollbar(GuiGraphicsExtractor graphics, int mouseX, int mouseY) {
        if (!this.scrollable()) return;
        int x = this.scrollBarX();
        PopupStyle.rect(graphics, x, this.getY(), 3, this.getHeight(), PopupStyle.withAlpha(PopupStyle.colors().text(), 0x14));
        PopupStyle.rect(graphics, x, this.scrollBarY(), 3, this.scrollerHeight(), PopupStyle.withAlpha(PopupStyle.accent(), 0xB0));
    }

    public abstract static class Entry extends ContainerObjectSelectionList.Entry<Entry> {
        abstract int preferredHeight();

        void refresh() {}
    }

    /** One setting: its name and a line of description on the left, the editor and a reset button on the right. */
    public static final class ValueEntry<T> extends Entry {
        private final ConfigEditSession session;
        private final ConfigValue<T> value;
        private final @Nullable Component description;
        private final AbstractWidget editor;
        private final FlatButton reset;

        public ValueEntry(ConfigEditSession session, ConfigValue<T> value, int editorWidth) {
            this.session = session;
            this.value = value;
            this.description = description(value);
            this.editor = editor(session, value, editorWidth);
            this.reset = FlatButton.icon(PopupStyle.Icon.RESET,
                    Component.translatableWithFallback("gui.ultimine_addition.config.reset_value", "Reset to default"),
                    button -> session.set(value, value.getDefault()));
        }

        @SuppressWarnings({"unchecked", "rawtypes"})
        private static <T> AbstractWidget editor(ConfigEditSession session, ConfigValue<T> value, int width) {
            if (value.type() instanceof BooleanType) {
                ConfigValue<Boolean> bool = (ConfigValue<Boolean>) value;
                return new ToggleSwitch(width, 20, () -> session.get(bool), button -> session.set(bool, !session.get(bool)));
            }
            if (value.type() instanceof EnumType enumType) return new EnumSelector(session, (ConfigValue) value, enumType, width);
            FlatButton shown = FlatButton.of(Component.literal(value.type().format(session.get(value))), width, button -> {});
            shown.active = false;
            return shown;
        }

        // The first line of the tooltip translation, or of the comment.
        private static @Nullable Component description(ConfigValue<?> value) {
            String tooltipKey = value.translationKey() + ".tooltip";
            if (Language.getInstance().has(tooltipKey)) {
                return Component.literal(Component.translatable(tooltipKey).getString().lines().findFirst().orElse(""));
            }
            return value.comment().isEmpty() ? null : Component.literal(value.comment().getFirst());
        }

        @Override
        int preferredHeight() {
            return this.description != null ? 34 : 26;
        }

        @Override
        public void extractContent(GuiGraphicsExtractor graphics, int mouseX, int mouseY, boolean hovered, float a) {
            Font font = Minecraft.getInstance().font;
            int x = this.getX();
            int y = this.getY();
            int width = this.getWidth();
            int height = this.getHeight();
            boolean active = this.session.isActive(this.value);
            Optional<Component> error = this.session.error(this.value);
            boolean modified = this.session.isModified(this.value);

            if (hovered) PopupStyle.rect(graphics, x, y + 1, width, height - 2, PopupStyle.colors().rowHover());
            // A mark in the margin for changed or invalid values, like a diff.
            if (error.isPresent() || modified) {
                PopupStyle.rect(graphics, x, y + 4, 2, height - 8, error.isPresent() ? PopupStyle.colors().error() : PopupStyle.colors().modified());
            }

            // Values shown read-only stay off.
            if (this.editor instanceof ToggleSwitch || this.editor instanceof EnumSelector) this.editor.active = active;
            this.reset.active = !this.session.isDefault(this.value);
            this.reset.setPosition(x + width - 22, y + (height - 20) / 2);
            this.editor.setPosition(this.reset.getX() - 4 - this.editor.getWidth(), y + (height - this.editor.getHeight()) / 2);
            this.editor.extractRenderState(graphics, mouseX, mouseY, a);
            this.reset.extractRenderState(graphics, mouseX, mouseY, a);

            int textX = x + 10;
            int labelWidth = this.editor.getX() - textX - 8;
            int nameY = this.description != null ? y + height / 2 - 10 : y + (height - 8) / 2;
            int nameColor = error.isPresent() ? PopupStyle.colors().error() : !active ? PopupStyle.colors().textMuted()
                    : modified ? PopupStyle.colors().modified() : PopupStyle.colors().text();
            graphics.text(font, PopupStyle.ellipsize(font, this.value.displayName(), Math.max(10, labelWidth)), textX, nameY, nameColor, false);
            if (this.description != null) {
                PopupStyle.text(graphics, font, this.description, textX, nameY + 12, labelWidth, !active ? PopupStyle.colors().textMuted() : PopupStyle.colors().textDim());
            }

            if (hovered && mouseX >= x && mouseX < textX + labelWidth && mouseY >= y && mouseY < y + height) {
                List<FormattedCharSequence> lines = new ArrayList<>();
                for (Component line : this.tooltip(error)) lines.addAll(font.split(line, TOOLTIP_WIDTH));
                graphics.setTooltipForNextFrame(font, lines, mouseX, mouseY);
            }
        }

        private List<Component> tooltip(Optional<Component> error) {
            List<Component> lines = new ArrayList<>();
            lines.add(this.value.displayName().copy().withStyle(ChatFormatting.WHITE, ChatFormatting.BOLD));
            String tooltipKey = this.value.translationKey() + ".tooltip";
            if (Language.getInstance().has(tooltipKey)) {
                lines.add(Component.translatable(tooltipKey));
            } else {
                this.value.comment().forEach(line -> lines.add(Component.literal(line)));
            }
            this.value.type().describe().forEach(line -> lines.add(line.copy().withStyle(ChatFormatting.GRAY)));
            lines.add(Component.translatableWithFallback("gui.ultimine_addition.config.default", "Default: %s", this.value.type().format(this.value.getDefault()))
                    .withStyle(ChatFormatting.GRAY));
            error.ifPresent(message -> lines.add(message.copy().withStyle(ChatFormatting.RED)));
            return lines;
        }

        @Override
        void refresh() {
            if (this.editor instanceof EnumSelector<?> selector) selector.refresh();
        }

        @Override
        public List<? extends GuiEventListener> children() {
            return List.of(this.editor, this.reset);
        }

        @Override
        public List<? extends NarratableEntry> narratables() {
            return List.of(this.editor, this.reset);
        }
    }

    /** A nested group's heading: its name in the accent, then a rule to the edge; deeper groups are indented. */
    public static final class SectionEntry extends Entry {
        private final ConfigGroup group;
        private final int depth;

        public SectionEntry(ConfigGroup group, int depth) {
            this.group = group;
            this.depth = depth;
        }

        @Override
        int preferredHeight() {
            return 24;
        }

        @Override
        public void extractContent(GuiGraphicsExtractor graphics, int mouseX, int mouseY, boolean hovered, float a) {
            Font font = Minecraft.getInstance().font;
            int x = this.getX() + 6 + this.depth * 10;
            int y = this.getY() + this.getHeight() - 12;
            int accent = PopupStyle.accent();
            Component name = this.group.displayName().copy().withStyle(ChatFormatting.BOLD);
            int textWidth = Math.min(font.width(name), this.getWidth() - (x - this.getX()) - 24);
            PopupStyle.Icon.CHEVRON.draw(graphics, x, y - 1, accent);
            PopupStyle.text(graphics, font, name, x + 12, y, textWidth, accent);
            int lineX = x + 12 + textWidth + 6;
            graphics.fill(lineX, y + 4, this.getX() + this.getWidth() - 4, y + 5, PopupStyle.withAlpha(accent, 0x40));
        }

        @Override
        public List<? extends GuiEventListener> children() {
            return List.of();
        }

        @Override
        public List<? extends NarratableEntry> narratables() {
            return List.of();
        }
    }

    // A selector between arrows: its arrows step to the previous or next constant, and its middle opens a dropdown of
    // them all. Enter steps forward; right-click or shift steps back.
    private static final class EnumSelector<E extends Enum<E>> extends FlatButton {
        // How wide each arrow's clickable end is.
        private static final int ARROW_ZONE = 16;

        private final ConfigEditSession session;
        private final ConfigValue<E> value;
        private final EnumType<E> type;

        EnumSelector(ConfigEditSession session, ConfigValue<E> value, EnumType<E> type, int width) {
            super(width, 20, CommonComponents.EMPTY, button -> {});
            this.session = session;
            this.value = value;
            this.type = type;
            this.refresh();
        }

        void refresh() {
            this.setMessage(this.type.displayName(this.session.get(this.value)));
        }

        private void step(boolean backwards) {
            this.session.set(this.value, this.type.cycle(this.session.get(this.value), backwards));
            this.refresh();
        }

        // Every constant in a dropdown under the box; picking one sets it.
        private void openDropdown() {
            Minecraft minecraft = Minecraft.getInstance();
            minecraft.setScreen(new DropdownScreen<>(minecraft.screen, this.getX(), this.getY(), this.getWidth(), this.getHeight(),
                    this.type.constants(), this.session.get(this.value), this.type::displayName, this.type::description, picked -> {
                        this.session.set(this.value, picked);
                        this.refresh();
                    }));
        }

        @Override
        public void onPress(InputWithModifiers input) {
            if (input instanceof MouseButtonEvent event && event.button() == 0) {
                if (event.x() < this.getX() + ARROW_ZONE) this.step(true);
                else if (event.x() >= this.getRight() - ARROW_ZONE) this.step(false);
                else this.openDropdown();
                return;
            }
            this.step(input.hasShiftDown() || input instanceof MouseButtonEvent);
        }

        @Override
        protected boolean isValidClickButton(MouseButtonInfo buttonInfo) {
            return buttonInfo.button() == 0 || buttonInfo.button() == 1;
        }

        @Override
        protected void extractContents(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a) {
            this.refresh();
            super.extractContents(graphics, mouseX, mouseY, a);
            // Each arrow lights up while the mouse is over its end, the one it would step toward.
            boolean hovered = this.active && this.isHovered();
            int y = this.getY() + (this.getHeight() - PopupStyle.Icon.SIZE) / 2;
            PopupStyle.Icon.CHEVRON_LEFT.draw(graphics, this.getX() + 5, y, this.arrowColor(hovered && mouseX < this.getX() + ARROW_ZONE));
            PopupStyle.Icon.CHEVRON.draw(graphics, this.getRight() - 5 - PopupStyle.Icon.SIZE, y,
                    this.arrowColor(hovered && mouseX >= this.getRight() - ARROW_ZONE));
        }

        private int arrowColor(boolean lit) {
            if (!this.active) return PopupStyle.colors().textMuted();
            return lit || this.isFocused() ? PopupStyle.accent() : PopupStyle.colors().textDim();
        }
    }
}
