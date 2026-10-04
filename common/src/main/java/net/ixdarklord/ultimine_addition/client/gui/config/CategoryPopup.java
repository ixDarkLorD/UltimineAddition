package net.ixdarklord.ultimine_addition.client.gui.config;import net.ixdarklord.coolcatcore.api.config.Config;
import net.ixdarklord.coolcatcore.api.config.ConfigGroup;
import net.ixdarklord.coolcatcore.api.config.ConfigNode;
import net.ixdarklord.coolcatcore.api.config.ConfigTheme;
import net.ixdarklord.coolcatcore.api.config.ConfigValue;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * One category of a client config in a small window: that group's settings (its nested groups as sections) with Save
 * and Cancel, floating over the screen that opened it. A copy of Glazed Menu's category popup, so the Skills Record's
 * settings open without Glazed Menu installed.
 */
public final class CategoryPopup extends StyledPopup {
    private static final int WIDTH = 340;
    private static final int KEY_S = 83;

    private final ConfigEditSession session;
    private final ConfigGroup group;
    private @Nullable ConfigEntryList list;
    private @Nullable FlatButton saveButton;
    private @Nullable FlatButton resetButton;

    private CategoryPopup(@Nullable Screen parent, ConfigEditSession session, ConfigGroup group, ConfigTheme theme) {
        super(parent, group.parent() == null ? session.config().title() : group.displayName(), theme);
        this.session = session;
        this.group = group;
    }

    /**
     * The group at a dotted path in a config.
     *
     * @throws IllegalArgumentException when no group is at the path
     */
    public static CategoryPopup create(@Nullable Screen parent, Config config, String path, ConfigTheme theme) {
        ConfigNode node = config.root();
        if (!path.isEmpty()) {
            for (String key : path.split("\\.")) {
                ConfigNode child = node instanceof ConfigGroup group ? group.child(key) : null;
                if (child == null) throw new IllegalArgumentException("Config " + config.id() + " has no category " + path);
                node = child;
            }
        }
        if (!(node instanceof ConfigGroup group)) throw new IllegalArgumentException("Config " + config.id() + "'s " + path + " isn't a category");
        return new CategoryPopup(parent, new ConfigEditSession(config), group, theme);
    }

    @Override
    protected PopupStyle.Icon titleIcon() {
        return PopupStyle.Icon.CLIENT;
    }

    // Which config the category belongs to, when it isn't the whole config.
    @Override
    protected @Nullable Component titleNote() {
        return this.group.parent() == null ? null : this.session.config().title();
    }

    @Override
    protected void initPopup() {
        List<ConfigEntryList.Entry> entries = this.entries();
        int listHeight = entries.stream().mapToInt(ConfigEntryList.Entry::preferredHeight).sum() + 6;
        int maxList = this.height - 40 - (PADDING * 2 + TITLE_HEIGHT + FOOTER);
        this.setPanel(WIDTH, PADDING + TITLE_HEIGHT + Math.min(listHeight, Math.max(60, maxList)) + FOOTER + PADDING);

        int listTop = this.contentTop();
        int listBottom = this.footerTop();
        this.list = this.addRenderableWidget(new ConfigEntryList(this.minecraft, this.contentWidth() + 8, listBottom - listTop, listTop));
        this.list.updateSizeAndPosition(this.contentWidth() + 8, listBottom - listTop, this.contentLeft() - 4, listTop);
        this.list.setEntries(entries);

        int buttonY = this.footerButtonY();
        int right = this.contentLeft() + this.contentWidth();
        this.saveButton = this.addRenderableWidget(FlatButton.of(CommonComponents.GUI_DONE, 76, button -> this.save())
                .style(FlatButton.Style.PRIMARY).withIcon(PopupStyle.Icon.CHECK));
        this.saveButton.setPosition(right - 76, buttonY);
        FlatButton cancel = this.addRenderableWidget(FlatButton.of(CommonComponents.GUI_CANCEL, 64, button -> this.onClose()));
        cancel.setPosition(right - 76 - 4 - 64, buttonY);
        this.resetButton = this.addRenderableWidget(FlatButton.icon(PopupStyle.Icon.RESET,
                Component.translatableWithFallback("gui.ultimine_addition.config.reset.tooltip", "Sets the values shown here to their defaults"), button -> {
                    this.session.resetToDefaults(this.values());
                    this.refreshValues();
                }));
        this.resetButton.setPosition(this.contentLeft(), buttonY);
        this.updateButtons();
    }

    // Settings, and nested groups as sections.
    private List<ConfigEntryList.Entry> entries() {
        List<ConfigEntryList.Entry> entries = new ArrayList<>();
        // From the width the panel is about to get: the rows are made before it's sized.
        int contentWidth = Math.min(WIDTH, this.width - 16) - PADDING * 2;
        int editorWidth = Mth.clamp((contentWidth - 14) * 2 / 5, 90, 150);
        this.addGroup(entries, this.group, 0, editorWidth);
        return entries;
    }

    private void addGroup(List<ConfigEntryList.Entry> entries, ConfigGroup group, int depth, int editorWidth) {
        for (ConfigNode child : group.children()) {
            if (child instanceof ConfigValue<?> value && !value.isHidden()) {
                entries.add(new ConfigEntryList.ValueEntry<>(this.session, value, editorWidth));
            } else if (child instanceof ConfigGroup subgroup && !subgroup.isHidden() && subgroup.values().anyMatch(value -> !value.isHidden())) {
                entries.add(new ConfigEntryList.SectionEntry(subgroup, depth));
                this.addGroup(entries, subgroup, depth + 1, editorWidth);
            }
        }
    }

    private List<ConfigValue<?>> values() {
        return this.group.values().filter(value -> !value.isHidden()).toList();
    }

    @Override
    protected void renderPopup(GuiGraphics graphics, int mouseX, int mouseY, float a) {
        // A hairline above the buttons, and what's unsaved or invalid beside them.
        int lineY = this.footerTop() + 1;
        graphics.fill(this.contentLeft(), lineY, this.contentLeft() + this.contentWidth(), lineY + 1, PopupStyle.colors().panelBorder());
        int errors = this.session.errors().size();
        int modified = this.session.modifiedCount();
        Component status = errors > 0 ? Component.translatableWithFallback("gui.ultimine_addition.config.status.errors", "%s invalid", errors)
                : modified > 0 ? Component.translatableWithFallback("gui.ultimine_addition.config.status.modified", "%s unsaved", modified) : null;
        if (status != null) {
            int x = this.contentLeft() + 26;
            int width = this.contentLeft() + this.contentWidth() - 76 - 4 - 64 - 8 - x;
            PopupStyle.text(graphics, this.font, status, x, this.contentBottom() - 14, width,
                    errors > 0 ? PopupStyle.colors().error() : PopupStyle.colors().modified());
        }
    }

    @Override
    public void tick() {
        this.updateButtons();
    }

    private void updateButtons() {
        if (this.saveButton == null) return;
        this.saveButton.active = !this.session.hasErrors();
        this.saveButton.setMessage(this.session.modifiedCount() > 0
                ? Component.translatableWithFallback("gui.ultimine_addition.config.save", "Save") : CommonComponents.GUI_DONE);
        this.saveButton.setTooltip(this.session.hasErrors()
                ? Tooltip.create(Component.translatableWithFallback("gui.ultimine_addition.config.save.errors", "Fix the invalid values first:")
                .append("\n").append(this.session.errors().stream().map(Component::getString).collect(Collectors.joining("\n"))))
                : null);
    }

    private void refreshValues() {
        if (this.list != null) this.list.refreshValues();
        this.updateButtons();
    }

    private void save() {
        if (this.session.hasErrors()) return;
        this.minecraft.setScreen(this.parent);
        this.session.save();
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (Screen.hasControlDown() && keyCode == KEY_S) {
            if (this.saveButton != null && this.saveButton.active) this.save();
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    // Unsaved changes are confirmed before they're dropped, however the popup is closed.
    @Override
    public void onClose() {
        if (this.session.modifiedCount() == 0 && !this.session.hasErrors()) {
            this.minecraft.setScreen(this.parent);
            return;
        }
        this.minecraft.setScreen(new ConfirmPopup(this, this.theme,
                Component.translatableWithFallback("gui.ultimine_addition.config.discard.title", "Discard changes?"),
                Component.translatableWithFallback("gui.ultimine_addition.config.discard.message", "%s unsaved changes will be lost.", this.session.modifiedCount()),
                Component.translatableWithFallback("gui.ultimine_addition.config.discard.yes", "Discard"), true,
                discard -> this.minecraft.setScreen(discard ? this.parent : this)));
    }

    // Coming back from a dropdown or the discard question.
    @Override
    public void added() {
        super.added();
        this.refreshValues();
    }
}
