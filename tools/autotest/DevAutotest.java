package net.ixdarklord.ultimine_addition.client.event;

import net.ixdarklord.ultimine_addition.client.gui.config.CategoryPopup;
import net.ixdarklord.ultimine_addition.client.gui.config.ConfirmPopup;
import net.ixdarklord.ultimine_addition.client.gui.config.DropdownScreen;
import net.ixdarklord.ultimine_addition.client.gui.config.FlatButton;
import net.ixdarklord.ultimine_addition.client.gui.config.ToggleSwitch;
import net.ixdarklord.ultimine_addition.config.UAClientConfig;
import net.ixdarklord.ultimine_addition.config.UAConfigs;
import net.ixdarklord.ultimine_addition.core.FTBUltimineAddition;
import net.ixdarklord.coolcatcore.api.client.gui.ItemDecorator;
import net.ixdarklord.coolcatcore.api.item.DecoratedItem;
import net.ixdarklord.ultimine_addition.client.renderer.item.PotionPointPips;
import net.ixdarklord.ultimine_addition.common.item.MiningSkillCardItem;
import net.ixdarklord.ultimine_addition.core.Registration;
import net.minecraft.client.Minecraft;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.events.ContainerEventHandler;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.TitleScreen;

import java.io.File;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

// The Skills Record settings popup's client smoke test, copied into a version's sources by tools/autotest/run.py (never
// committed there) and run with -Dultimine_addition.autotest=true. From the title screen it opens the popup, flips a
// switch, opens the option dropdown and closes with unsaved changes (the discard question); then it checks the
// Completion Envelope's name and the cards' pips decorator, shows the items (older versions) and quits. Nothing is saved.
// Each check logs "AUTOTEST PASS|FAIL <name>"; the last line is "AUTOTEST RESULT passed=<n> failed=<n>".
//
// One file serves every Minecraft version: the calls that differ between versions (how a button is pressed or clicked,
// how a screenshot is taken) go through reflection.
public final class DevAutotest {
    // Ticks between steps: enough for a screen to be laid out and drawn a few times.
    private static final int STEP_TICKS = 4;

    private static int ticks = -1;
    private static int step;
    private static int passed;
    private static int failed;
    private static Screen showcase;

    private DevAutotest() {}

    public static void tick(Minecraft minecraft) {
        if (!Boolean.getBoolean("ultimine_addition.autotest")) return;
        if (ticks < 0) {
            // The title screen, once the loading screen over it is gone (or the screenshots show that instead).
            if (!(minecraft.screen instanceof TitleScreen) || minecraft.getOverlay() != null) return;
            ticks = 0;
        }
        if (++ticks % STEP_TICKS != 0) return;
        try {
            Screen screen = minecraft.screen;
            switch (step++) {
                case 0 -> minecraft.setScreen(CategoryPopup.create(screen, UAClientConfig.CONFIG, UAClientConfig.SKILLS_RECORD_CATEGORY,
                        UAConfigs.SKILLS_RECORD_POPUP_THEME));
                case 1 -> {
                    check("popup_opens", screen instanceof CategoryPopup, describe(screen));
                    List<AbstractWidget> widgets = widgets(screen);
                    check("popup_has_two_switches", widgets.stream().filter(ToggleSwitch.class::isInstance).count() == 2, widgets.size() + " widgets");
                    check("popup_has_option_picker", selector(screen) != null, widgets.size() + " widgets");
                    check("popup_button_says_done", footer(screen, "Done") != null, labels(screen));
                    shot(minecraft, "a_popup");
                }
                case 2 -> press(widgets(screen).stream().filter(ToggleSwitch.class::isInstance).findFirst().orElseThrow());
                case 3 -> {
                    check("switch_marks_unsaved", footer(screen, "Save") != null, labels(screen));
                    shot(minecraft, "b_changed");
                }
                case 4 -> {
                    AbstractWidget selector = selector(screen);
                    click(selector, selector.getX() + selector.getWidth() / 2.0, selector.getY() + selector.getHeight() / 2.0);
                }
                case 5 -> {
                    check("option_picker_opens_dropdown", screen instanceof DropdownScreen, describe(screen));
                    shot(minecraft, "c_dropdown");
                }
                case 6 -> screen.onClose();
                case 7 -> {
                    check("dropdown_returns_to_popup", screen instanceof CategoryPopup, describe(screen));
                    check("changes_kept_after_dropdown", footer(screen, "Save") != null, labels(screen));
                    screen.onClose();
                }
                case 8 -> {
                    check("closing_unsaved_asks_to_discard", screen instanceof ConfirmPopup, describe(screen));
                    shot(minecraft, "d_discard");
                }
                case 9 -> press(footer(screen, "Discard"));
                case 10 -> {
                    check("discard_returns_to_title", screen instanceof TitleScreen, describe(screen));
                    // The sealed Miner Certificate's name, and the card as a CoolCatLib DecoratedItem handing over its pips.
                    List<ItemDecorator> decorators = new ArrayList<>();
                    int cards = 0;
                    Item anyCard = null;
                    for (Item item : BuiltInRegistries.ITEM) {
                        if (!(item instanceof MiningSkillCardItem)) continue;
                        cards++;
                        anyCard = item;
                        if (item instanceof DecoratedItem decorated) decorated.registerDecorators(decorators::add);
                    }
                    check("every_card_hands_over_its_pips", cards > 0 && decorators.size() == cards
                            && decorators.stream().allMatch(PotionPointPips.class::isInstance), decorators.size() + " of " + cards + " cards");
                    // Checks that need an item stack. Since 26.1 a stack can't be made before a world is loaded (its
                    // components aren't bound at the title screen), so there they're skipped.
                    try {
                        ItemStack envelope = new ItemStack(Registration.MINER_CERTIFICATE.get());
                        check("sealed_certificate_is_completion_envelope", envelope.getHoverName().getString().equals("Completion Envelope"),
                                envelope.getHoverName().getString());
                        check("cards_show_no_durability_bar", anyCard != null && !new ItemStack(anyCard).isBarVisible(), String.valueOf(anyCard));
                    } catch (NullPointerException e) {
                        FTBUltimineAddition.LOGGER.info("AUTOTEST SKIP the item stack checks: {}", e.getMessage());
                    }
                }
                // The items drawn large with their decorations (the 1.21.1 and 1.20.1 versions, which have no other
                // in-game check of them): the showcase is only there when run.py copied it in.
                case 11 -> {
                    showcase = showcase();
                    if (showcase != null) minecraft.setScreen(showcase);
                }
                case 12 -> {
                    if (showcase != null) {
                        Class<?> type = showcase.getClass();
                        Object error = type.getField("error").get(null);
                        check("showcase_draws_items_with_decorations", screen == showcase && type.getField("frames").getInt(null) > 0 && error == null,
                                error == null ? type.getMethod("names").invoke(showcase).toString() : error.toString());
                        if (error instanceof Throwable thrown) FTBUltimineAddition.LOGGER.error("AUTOTEST showcase threw", thrown);
                        shot(minecraft, "e_items");
                    }
                }
                default -> finish(minecraft);
            }
        } catch (Throwable e) {
            FTBUltimineAddition.LOGGER.error("AUTOTEST FAIL step_{} threw", step - 1, e);
            failed++;
            finish(minecraft);
        }
    }

    private static Screen showcase() {
        try {
            return (Screen) Class.forName("net.ixdarklord.ultimine_addition.client.event.AutotestShowcase").getConstructor().newInstance();
        } catch (ClassNotFoundException e) {
            return null;
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException(e);
        }
    }

    // Every widget of a screen, those inside its lists' rows included.
    private static List<AbstractWidget> widgets(Screen screen) {
        List<AbstractWidget> widgets = new ArrayList<>();
        collect(screen, widgets, 0);
        return widgets;
    }

    private static void collect(ContainerEventHandler container, List<AbstractWidget> widgets, int depth) {
        for (GuiEventListener child : container.children()) {
            if (child instanceof AbstractWidget widget) widgets.add(widget);
            if (child instanceof ContainerEventHandler nested && depth < 3) collect(nested, widgets, depth + 1);
        }
    }

    // The option picker: a private class of the list.
    private static AbstractWidget selector(Screen screen) {
        return widgets(screen).stream().filter(widget -> widget.getClass().getSimpleName().equals("EnumSelector")).findFirst().orElse(null);
    }

    private static AbstractWidget footer(Screen screen, String label) {
        return widgets(screen).stream().filter(widget -> widget instanceof FlatButton && widget.getMessage().getString().equals(label)).findFirst().orElse(null);
    }

    private static String labels(Screen screen) {
        return widgets(screen).stream().map(widget -> widget.getMessage().getString()).filter(text -> !text.isEmpty()).toList().toString();
    }

    private static void check(String name, boolean ok, String detail) {
        if (ok) passed++;
        else failed++;
        FTBUltimineAddition.LOGGER.info("AUTOTEST {} {} ({})", ok ? "PASS" : "FAIL", name, detail);
    }

    private static void finish(Minecraft minecraft) {
        FTBUltimineAddition.LOGGER.info("AUTOTEST RESULT passed={} failed={}", passed, failed);
        minecraft.stop();
        // A dev client may not exit after a stop like this; the test is over, so the process ends either way.
        Thread exit = new Thread(() -> {
            try {
                Thread.sleep(2500);
            } catch (InterruptedException ignored) {
            }
            Runtime.getRuntime().halt(0);
        }, "Ultimine Addition autotest exit");
        exit.setDaemon(true);
        exit.start();
    }

    private static String describe(Object object) {
        return object == null ? "null" : object.getClass().getName();
    }

    // --- What differs between Minecraft versions ---

    // A button's press: onPress() before 26.1, onPress(input) since (given an Enter key press).
    private static void press(AbstractWidget button) throws ReflectiveOperationException {
        if (button == null) throw new NoSuchMethodException("no button to press");
        for (Method method : button.getClass().getMethods()) {
            if (!method.getName().equals("onPress")) continue;
            if (method.getParameterCount() == 0) {
                method.invoke(button);
                return;
            }
            if (method.getParameterCount() == 1) {
                Constructor<?> key = Class.forName("net.minecraft.client.input.KeyEvent").getConstructor(int.class, int.class, int.class);
                method.invoke(button, key.newInstance(257, 0, 0));
                return;
            }
        }
        throw new NoSuchMethodException("onPress on " + button.getClass().getName());
    }

    // A left click on a widget: mouseClicked(x, y, button) before 26.1, mouseClicked(event, doubleClick) since.
    private static void click(AbstractWidget widget, double x, double y) throws ReflectiveOperationException {
        try {
            AbstractWidget.class.getMethod("mouseClicked", double.class, double.class, int.class).invoke(widget, x, y, 0);
        } catch (NoSuchMethodException e) {
            Class<?> info = Class.forName("net.minecraft.client.input.MouseButtonInfo");
            Class<?> event = Class.forName("net.minecraft.client.input.MouseButtonEvent");
            Object click = event.getConstructor(double.class, double.class, info).newInstance(x, y, info.getConstructor(int.class, int.class).newInstance(0, 0));
            AbstractWidget.class.getMethod("mouseClicked", event, boolean.class).invoke(widget, click, false);
        }
    }

    // Something of a type reachable from an object: what one of its no-argument getters returns or one of its fields
    // holds, or (a level further) one of those objects' own. Finds the main render target wherever a version keeps it.
    private static Object find(Object root, Class<?> type, int depth) {
        for (Method method : root.getClass().getMethods()) {
            if (method.getParameterCount() != 0 || Modifier.isStatic(method.getModifiers()) || !type.isAssignableFrom(method.getReturnType())) continue;
            try {
                Object value = method.invoke(root);
                if (value != null) return value;
            } catch (ReflectiveOperationException ignored) {
            }
        }
        for (java.lang.reflect.Field field : root.getClass().getFields()) {
            if (Modifier.isStatic(field.getModifiers()) || field.getType().isPrimitive()) continue;
            try {
                Object value = field.get(root);
                if (value == null) continue;
                if (type.isInstance(value)) return value;
                if (depth > 1 && field.getType().getName().startsWith("net.minecraft.client.")) {
                    Object deeper = find(value, type, depth - 1);
                    if (deeper != null) return deeper;
                }
            } catch (ReflectiveOperationException ignored) {
            }
        }
        return null;
    }

    // Screenshot.grab, whose arguments changed over the versions; a failed screenshot fails nothing.
    private static void shot(Minecraft minecraft, String name) {
        try {
            Class<?> screenshots = Class.forName("net.minecraft.client.Screenshot");
            for (Method method : screenshots.getMethods()) {
                Class<?>[] types = method.getParameterTypes();
                if (!method.getName().equals("grab") || !Modifier.isStatic(method.getModifiers()) || types.length < 4
                        || types[0] != File.class || types[1] != String.class) continue;
                Object[] arguments = new Object[types.length];
                for (int i = 0; i < types.length; i++) {
                    if (types[i] == File.class) arguments[i] = minecraft.gameDirectory;
                    else if (types[i] == String.class) arguments[i] = "autotest_" + name + ".png";
                    else if (types[i] == int.class) arguments[i] = 1;
                    else if (types[i] == Consumer.class) arguments[i] = (Consumer<Object>) message -> {};
                    else arguments[i] = find(minecraft, types[i], 2);
                }
                method.invoke(null, arguments);
                return;
            }
            FTBUltimineAddition.LOGGER.warn("AUTOTEST no screenshot method for {}", name);
        } catch (ReflectiveOperationException | RuntimeException e) {
            FTBUltimineAddition.LOGGER.warn("AUTOTEST screenshot {} failed: {}", name, e.toString());
        }
    }
}
