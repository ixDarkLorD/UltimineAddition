package net.ixdarklord.ultimine_addition.client.gui.screens;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.ixdarklord.ultimine_addition.client.gui.GuiDraw;
import net.ixdarklord.ultimine_addition.common.data.shape.CustomShapes;
import net.ixdarklord.ultimine_addition.config.UAServerConfig;
import net.ixdarklord.ultimine_addition.core.FTBUltimineAddition;
import net.ixdarklord.ultimine_addition.network.PayloadHandler;
import net.ixdarklord.ultimine_addition.network.payloads.ShapePayload;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import org.joml.Matrix4f;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

// The Shape Designer: a shape of one's own, on the Shape Selector's blueprint. The shape is drawn layer by layer on a
// 5 x 5 grid, seen as when facing the block being broken (the gold cell in the middle of the front layer); the column
// of small layers on the left picks the one to draw on, front to back, up to four behind the face. The preview on the
// right shows the whole shape as blocks and turns when dragged. Name it and save it: the server checks it
// (CustomShapes.create) and answers with a notice. Opened from the Shape Selector.
//
// The sheet beside the board lists the shapes the player already keeps (ShapePayload.Mine): one picked there can be
// loaded onto the grid, replaced by the drawing or deleted. Copy and Paste carry the drawing through the clipboard as
// JSON, in the form of a data pack shape (name, pattern, blocks), to share it.
public final class ShapeDesignerScreen extends Screen {
    private static final ResourceLocation BACKGROUND_TEXTURE = FTBUltimineAddition.getGuiTexture("container/shape_designer", "png");
    private static final int WIDTH = 236, HEIGHT = 212;
    // How far above the Shape Selector the popup is drawn.
    private static final float POPUP_Z = 400.0F;
    private static final int SIZE = CustomShapes.SIZE, DEPTH = CustomShapes.MAX_DEPTH;
    // Places on the board, from its top left corner.
    private static final int TITLE_Y = 33, CAPTION_Y = 50, MAIN_Y = 62, NAME_Y = 160, BUTTON_Y = 180;
    private static final int INNER_LEFT = 14, INNER_RIGHT = 222;
    private static final int THUMB_CELL = 3, THUMB = SIZE * THUMB_CELL, THUMB_PITCH = THUMB + 4;
    private static final int CELL = 18, GRID = SIZE * CELL + 1, GRID_X = 35;
    private static final int VIEW_X = 132, VIEW_WIDTH = INNER_RIGHT - VIEW_X, VIEW_HEIGHT = GRID;
    // The sheet of the player's shapes, beside the board (below its clip).
    private static final int SIDE_GAP = 3, SIDE_WIDTH = 120, SIDE_TOP = 14, SIDE_HEIGHT = HEIGHT - SIDE_TOP, SIDE_INNER = SIDE_WIDTH - 28;
    private static final int LIST_Y = 50, ENTRY_HEIGHT = 23, ENTRY_PITCH = 25, VISIBLE = 4, ACTIONS_Y = 151;

    private static final int LINE = 0xFFC3E5F5, WASH = 0xFF7FD3F5, INSET = 0xFF2586BD, INSET_SHADE = 0xFF1C6FA0, CELL_LINE = 0xFF3A9FD8;
    private static final int BLOCK = 0xFFC3E5F5, GHOST = 0xFF3594CC, ORIGIN = 0xFFFBD25A;
    // In the preview: the layer being drawn on, the others, and the edges between blocks.
    private static final int SOLID_ACTIVE = 0xFFEAF7FF, SOLID = 0xFF79C4EC, SOLID_EDGE = 0xFF1C6FA0, ORIGIN_EDGE = 0xFF8A6A1E;

    // Each face of a block: the way it looks, the two edges that span it, and how lit it is.
    private static final int[][] NORMALS = {{1, 0, 0}, {-1, 0, 0}, {0, 1, 0}, {0, -1, 0}, {0, 0, 1}, {0, 0, -1}};
    private static final int[][] SPAN_U = {{0, 1, 0}, {0, 1, 0}, {1, 0, 0}, {1, 0, 0}, {1, 0, 0}, {1, 0, 0}};
    private static final int[][] SPAN_V = {{0, 0, 1}, {0, 0, 1}, {0, 0, 1}, {0, 0, 1}, {0, 1, 0}, {0, 1, 0}};
    private static final float[] LIGHT = {0.62F, 0.7F, 1.0F, 0.45F, 0.55F, 0.84F};

    // By layer (0 is the face being broken), row and column.
    private final boolean[][][] cells = new boolean[DEPTH][SIZE][SIZE];
    private int layer;
    private EditBox name;
    private BlueprintButton save, load, replace, delete, paste;
    // In the list of the player's shapes: the one picked, how far it is scrolled, and whether Delete was pressed once.
    private ResourceLocation selected;
    private int scroll;
    private int deleteArmed;
    // What just happened (copied, pasted, loaded), shown for a moment in place of the title.
    private Component status;
    private int statusTicks;
    private int left, top;
    // While the mouse is held on the grid: whether it adds or removes blocks.
    private Boolean painting;
    // When the list was last clicked, to tell a second click on the picked shape from a first.
    private long lastEntryClick;
    private boolean turning;
    private float yaw = 0.6F, pitch = 0.45F;
    // How deep the preview frames, eased so it doesn't jump when a layer is added.
    private float shownDepth = 1.0F;

    // The Shape Selector it pops up over: drawn dimmed behind it, and shown again when it closes. Its menu stays open
    // meanwhile, so a tool in its slot stays there.
    private final Screen parent;

    public ShapeDesignerScreen(Screen parent) {
        super(Component.translatable("gui.ultimine_addition.shape_designer.title"));
        this.parent = parent;
    }

    private int blocks() {
        int count = 0;
        for (boolean[][] grid : this.cells) for (boolean[] row : grid) for (boolean cell : row) if (cell) count++;
        return count;
    }

    // The broken block: the middle of the first layer, which is never a cell to toggle.
    private static boolean isOrigin(int layer, int row, int column) {
        return layer == 0 && row == SIZE / 2 && column == SIZE / 2;
    }

    private boolean solid(int layer, int row, int column) {
        if (layer < 0 || layer >= DEPTH || row < 0 || row >= SIZE || column < 0 || column >= SIZE) return false;
        return this.cells[layer][row][column] || isOrigin(layer, row, column);
    }

    // The deepest layer drawn on.
    private int deepest() {
        int deepest = 0;
        for (int depth = 1; depth < DEPTH; depth++) {
            for (boolean[] row : this.cells[depth]) for (boolean cell : row) if (cell) deepest = depth;
        }
        return deepest;
    }

    private void setLayer(int layer) {
        layer = Mth.clamp(layer, 0, DEPTH - 1);
        if (layer == this.layer) return;
        this.layer = layer;
        Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F + layer * 0.08F));
    }

    private static boolean within(double x, double y, int left, int top, int width, int height) {
        return x >= left && x < left + width && y >= top && y < top + height;
    }

    // The layer whose small picture is under the mouse, or -1.
    private int thumbAt(double x, double y) {
        for (int depth = 0; depth < DEPTH; depth++) {
            if (within(x, y, this.left + INNER_LEFT - 2, this.top + MAIN_Y + depth * THUMB_PITCH - 2, THUMB + 4, THUMB + 4)) return depth;
        }
        return -1;
    }

    // The cell under the mouse as row * SIZE + column, or -1 (the broken block is no cell).
    private int cellAt(double x, double y) {
        int gridLeft = this.left + GRID_X, gridTop = this.top + MAIN_Y;
        if (!within(x, y, gridLeft, gridTop, GRID - 1, GRID - 1)) return -1;
        int column = (int) ((x - gridLeft) / CELL), row = (int) ((y - gridTop) / CELL);
        return isOrigin(this.layer, row, column) ? -1 : row * SIZE + column;
    }

    private boolean overPreview(double x, double y) {
        return within(x, y, this.left + VIEW_X, this.top + MAIN_Y, VIEW_WIDTH, VIEW_HEIGHT);
    }

    private void say(String key, Object... args) {
        this.status = Component.translatable("gui.ultimine_addition.shape_designer." + key, args);
        this.statusTicks = 50;
    }

    private int sideLeft() {
        return this.left + WIDTH + SIDE_GAP + 14;
    }

    private ShapePayload.Saved picked() {
        for (ShapePayload.Saved saved : ShapePayload.Mine.latest().shapes()) if (saved.id().equals(this.selected)) return saved;
        return null;
    }

    // The shape of the list under the mouse, or null.
    private ShapePayload.Saved entryAt(double x, double y) {
        List<ShapePayload.Saved> mine = ShapePayload.Mine.latest().shapes();
        for (int i = 0; i < VISIBLE && i + this.scroll < mine.size(); i++) {
            if (within(x, y, this.sideLeft(), this.top + LIST_Y + i * ENTRY_PITCH, SIDE_INNER, ENTRY_HEIGHT)) return mine.get(i + this.scroll);
        }
        return null;
    }

    private static boolean drawn(List<String> rows, int layer, int row, int column) {
        int index = layer * SIZE + row;
        return index < rows.size() && column < rows.get(index).length() && rows.get(index).charAt(column) == '#';
    }

    private void clear() {
        for (boolean[][] grid : this.cells) for (boolean[] row : grid) java.util.Arrays.fill(row, false);
        this.layer = 0;
    }

    private void load(ShapePayload.Saved saved) {
        this.clear();
        for (int depth = 0; depth < DEPTH; depth++) {
            for (int row = 0; row < SIZE; row++) {
                for (int column = 0; column < SIZE; column++) this.cells[depth][row][column] = drawn(saved.rows(), depth, row, column);
            }
        }
        this.name.setValue(saved.name());
        this.say("loaded", saved.name());
    }

    // The drawing as a data pack shape: the front layer as its pattern, the layers behind as [right, up, depth] blocks.
    private String toJson() {
        JsonObject json = new JsonObject();
        String name = this.name.getValue().strip();
        json.addProperty("name", name.isEmpty() ? Component.translatable("gui.ultimine_addition.shape_designer.default_name").getString() : name);
        JsonArray pattern = new JsonArray();
        for (int row = 0; row < SIZE; row++) {
            StringBuilder line = new StringBuilder();
            for (int column = 0; column < SIZE; column++) line.append(isOrigin(0, row, column) ? 'o' : this.cells[0][row][column] ? '#' : '.');
            pattern.add(line.toString());
        }
        json.add("pattern", pattern);
        JsonArray blocks = new JsonArray();
        for (int depth = 1; depth < DEPTH; depth++) {
            for (int row = 0; row < SIZE; row++) {
                for (int column = 0; column < SIZE; column++) {
                    if (!this.cells[depth][row][column]) continue;
                    JsonArray block = new JsonArray();
                    block.add(column - SIZE / 2);
                    block.add(SIZE / 2 - row);
                    block.add(depth);
                    blocks.add(block);
                }
            }
        }
        if (!blocks.isEmpty()) json.add("blocks", blocks);
        return json.toString();
    }

    // Puts a block of a pasted shape on the grid; false when it lies outside what the designer can draw.
    private static boolean place(boolean[][][] cells, int right, int up, int depth) {
        int column = right + SIZE / 2, row = SIZE / 2 - up;
        if (depth < 0 || depth >= DEPTH || row < 0 || row >= SIZE || column < 0 || column >= SIZE) return false;
        if (!isOrigin(depth, row, column)) cells[depth][row][column] = true;
        return true;
    }

    // A shape from the clipboard: what Copy wrote, or a data pack's shape file that fits the designer.
    private void paste() {
        boolean[][][] pasted = new boolean[DEPTH][SIZE][SIZE];
        String name = "";
        try {
            JsonObject json = JsonParser.parseString(this.minecraft.keyboardHandler.getClipboard().strip()).getAsJsonObject();
            if (json.has("name")) name = json.get("name").getAsString();
            boolean fits = json.has("pattern") || json.has("blocks");
            if (json.has("pattern")) {
                JsonArray pattern = json.getAsJsonArray("pattern");
                int originRow = -1, originColumn = -1;
                for (int row = 0; row < pattern.size(); row++) {
                    int at = pattern.get(row).getAsString().indexOf('o');
                    if (at >= 0) {
                        originRow = row;
                        originColumn = at;
                    }
                }
                if (originRow < 0) fits = false;
                for (int row = 0; fits && row < pattern.size(); row++) {
                    String line = pattern.get(row).getAsString();
                    for (int column = 0; column < line.length(); column++) {
                        if (line.charAt(column) == '#') fits &= place(pasted, column - originColumn, originRow - row, 0);
                    }
                }
            }
            if (json.has("blocks")) {
                for (JsonElement element : json.getAsJsonArray("blocks")) {
                    JsonArray block = element.getAsJsonArray();
                    fits &= block.size() == 3 && place(pasted, block.get(0).getAsInt(), block.get(1).getAsInt(), block.get(2).getAsInt());
                }
            }
            if (!fits) {
                this.say("paste.too_big");
                return;
            }
        } catch (RuntimeException e) {
            this.say("paste.none");
            return;
        }
        for (int depth = 0; depth < DEPTH; depth++) this.cells[depth] = pasted[depth];
        this.layer = 0;
        if (!name.isBlank()) this.name.setValue(name.length() > 24 ? name.substring(0, 24) : name);
        this.say("pasted");
    }

    @Override
    public void tick() {
        super.tick();
        if (this.statusTicks > 0) this.statusTicks--;
        if (this.deleteArmed > 0 && --this.deleteArmed == 0) this.delete.setMessage(Component.translatable("gui.ultimine_addition.shape_designer.delete"));
    }

    @Override
    protected void init() {
        if (this.parent != null && (this.parent.width != this.width || this.parent.height != this.height)) this.parent.resize(this.minecraft, this.width, this.height);
        this.left = Math.max(0, (this.width - WIDTH - SIDE_GAP - SIDE_WIDTH) / 2);
        this.top = (this.height - HEIGHT) / 2;
        String typed = this.name == null ? "" : this.name.getValue();
        int nameWidth = GRID_X + GRID - INNER_LEFT;
        this.name = this.addRenderableWidget(new EditBox(this.font, this.left + INNER_LEFT + 4, this.top + NAME_Y + 3, nameWidth - 8, 10, Component.translatable("gui.ultimine_addition.shape_designer.name")));
        this.name.setBordered(false);
        this.name.setMaxLength(24);
        this.name.setTextColor(0xFFFFFFFF);
        this.name.setHint(Component.translatable("gui.ultimine_addition.shape_designer.default_name").withStyle(style -> style.withColor(0x8FC8E6)));
        this.name.setValue(typed);

        int width = INNER_RIGHT - INNER_LEFT, each = (width - 8) / 3, y = this.top + BUTTON_Y;
        this.save = this.addRenderableWidget(new BlueprintButton(this.left + INNER_LEFT, y, each, 16, Component.translatable("gui.ultimine_addition.shape_designer.save"), this::save));
        this.addRenderableWidget(new BlueprintButton(this.left + INNER_LEFT + each + 4, y, width - each * 2 - 8, 16, Component.translatable("gui.ultimine_addition.shape_designer.clear"), this::clear));
        this.addRenderableWidget(new BlueprintButton(this.left + INNER_RIGHT - each, y, each, 16, CommonComponents.GUI_CANCEL, this::onClose));

        // Under the list: what to do with the shape picked in it, and the clipboard.
        int side = this.sideLeft(), half = (SIDE_INNER - 4) / 2, actions = this.top + ACTIONS_Y;
        this.load = this.addRenderableWidget(new BlueprintButton(side, actions, half, 14, Component.translatable("gui.ultimine_addition.shape_designer.load"), () -> {
            if (this.picked() != null) this.load(this.picked());
        }).tooltip("gui.ultimine_addition.shape_designer.load.tooltip"));
        this.replace = this.addRenderableWidget(new BlueprintButton(side + SIDE_INNER - half, actions, half, 14, Component.translatable("gui.ultimine_addition.shape_designer.replace"), () -> {
            if (this.picked() != null) this.send(this.picked().id().toString());
        }).tooltip("gui.ultimine_addition.shape_designer.replace.tooltip"));
        this.delete = this.addRenderableWidget(new BlueprintButton(side, actions + 16, SIDE_INNER, 14, Component.translatable("gui.ultimine_addition.shape_designer.delete"), () -> {
            // Twice to delete: the first press only asks.
            if (this.picked() == null) return;
            if (this.deleteArmed == 0) {
                this.deleteArmed = 60;
                this.delete.setMessage(Component.translatable("gui.ultimine_addition.shape_designer.delete.confirm"));
                return;
            }
            PayloadHandler.sendToServer(new ShapePayload.Delete(this.selected));
            this.say("deleted", this.picked().name());
            this.selected = null;
            this.deleteArmed = 0;
            this.delete.setMessage(Component.translatable("gui.ultimine_addition.shape_designer.delete"));
        }));
        this.addRenderableWidget(new BlueprintButton(side, actions + 32, half, 14, Component.translatable("gui.ultimine_addition.shape_designer.copy"), () -> {
            this.minecraft.keyboardHandler.setClipboard(this.toJson());
            this.say("copied");
        }).tooltip("gui.ultimine_addition.shape_designer.copy.tooltip"));
        this.paste = this.addRenderableWidget(new BlueprintButton(side + SIDE_INNER - half, actions + 32, half, 14, Component.translatable("gui.ultimine_addition.shape_designer.paste"), this::paste)
                .tooltip("gui.ultimine_addition.shape_designer.paste.tooltip"));
        PayloadHandler.sendToServer(new ShapePayload.ListRequest(true));
    }

    private void save() {
        this.send("");
    }

    // The drawing to the server: as a new shape, or in place of the one with this id.
    private void send(String replace) {
        // The layers down to the deepest one drawn on, five rows each.
        List<String> rows = new ArrayList<>();
        for (int depth = 0; depth <= this.deepest(); depth++) {
            for (int row = 0; row < SIZE; row++) {
                StringBuilder line = new StringBuilder();
                for (int column = 0; column < SIZE; column++) {
                    line.append(isOrigin(depth, row, column) ? 'o' : this.cells[depth][row][column] ? '#' : '.');
                }
                rows.add(line.toString());
            }
        }
        PayloadHandler.sendToServer(new ShapePayload.Design(this.name.getValue(), rows, replace));
        this.onClose();
    }

    private void paint(int cell) {
        boolean[] row = this.cells[this.layer][cell / SIZE];
        if (row[cell % SIZE] == this.painting) return;
        row[cell % SIZE] = this.painting;
        Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, this.painting ? 1.2F : 0.9F));
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        int thumb = this.thumbAt(mouseX, mouseY), cell = this.cellAt(mouseX, mouseY);
        ShapePayload.Saved entry = this.entryAt(mouseX, mouseY);
        if (entry != null) {
            // A second click on the picked shape loads it.
            long now = Util.getMillis();
            boolean doubleClick = now - this.lastEntryClick < 250L;
            this.lastEntryClick = now;
            if (entry.id().equals(this.selected) && doubleClick) this.load(entry);
            this.selected = entry.id();
            this.deleteArmed = 0;
            this.delete.setMessage(Component.translatable("gui.ultimine_addition.shape_designer.delete"));
            Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
            return true;
        }
        if (thumb >= 0) {
            this.setLayer(thumb);
            return true;
        }
        if (cell >= 0) {
            // The first cell decides: on an empty one the stroke draws, on a block it erases.
            this.painting = !this.cells[this.layer][cell / SIZE][cell % SIZE];
            this.paint(cell);
            this.setFocused(null);
            return true;
        }
        if (this.overPreview(mouseX, mouseY)) {
            this.turning = true;
            this.setFocused(null);
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        if (this.painting != null) {
            int cell = this.cellAt(mouseX, mouseY);
            if (cell >= 0) this.paint(cell);
            return true;
        }
        if (this.turning) {
            this.yaw += (float) dragX * 0.02F;
            this.pitch = Mth.clamp(this.pitch + (float) dragY * 0.02F, -1.4F, 1.4F);
            return true;
        }
        return super.mouseDragged(mouseX, mouseY, button, dragX, dragY);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        this.painting = null;
        this.turning = false;
        return super.mouseReleased(mouseX, mouseY, button);
    }

    // The wheel steps through the layers, over the grid and the layer column.
    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollY) {
        if (within(mouseX, mouseY, this.left + INNER_LEFT - 2, this.top + MAIN_Y - 2, GRID_X + GRID - INNER_LEFT + 4, GRID + 4) && scrollY != 0) {
            this.setLayer(this.layer + (scrollY < 0 ? 1 : -1));
            return true;
        }
        if (within(mouseX, mouseY, this.sideLeft(), this.top + LIST_Y, SIDE_INNER, VISIBLE * ENTRY_PITCH) && scrollY != 0) {
            this.scroll = Mth.clamp(this.scroll + (scrollY < 0 ? 1 : -1), 0, Math.max(0, ShapePayload.Mine.latest().shapes().size() - VISIBLE));
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, scrollY);
    }

    // A sunken plate on the blueprint, framed in its line color.
    private static void inset(GuiGraphics graphics, int x, int y, int width, int height, int frame) {
        graphics.fill(x - 1, y - 1, x + width + 1, y + height + 1, frame);
        graphics.fill(x, y, x + width, y + height, INSET);
        graphics.fill(x, y, x + width, y + 1, INSET_SHADE);
    }

    // Text shrunk to fit a width; align is 0 for left, 1 for centered and 2 for right.
    private void label(GuiGraphics graphics, Component text, int x, int y, int maxWidth, int color, int align, boolean shadow) {
        int width = this.font.width(text);
        float scale = Math.min(1.0F, maxWidth / (float) Math.max(1, width));
        var pose = graphics.pose();
        pose.pushPose();
        pose.translate(x - width * scale * align / 2.0F, y + (1.0F - scale) * 4.0F, 0.0F);
        pose.scale(scale, scale, 1.0F);
        GuiDraw.text(graphics, this.font, text, 0, 0, color, shadow);
        pose.popPose();
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        // A popup: the selector behind it (which draws the dimmed world), dimmed, and the designer above it (its z is
        // above the selector's items and their counts), so the selector's items and text stay under it.
        if (this.parent != null) {
            this.parent.render(graphics, -10000, -10000, partialTick);
            GuiDraw.nextStratum(graphics);
        } else {
            this.renderBackground(graphics);
        }
        var pose = graphics.pose();
        pose.pushPose();
        pose.translate(0.0F, 0.0F, POPUP_Z);
        if (this.parent != null) graphics.fill(0, 0, this.width, this.height, 0x90101418);
        this.renderBoard(graphics, mouseX, mouseY);
        super.render(graphics, mouseX, mouseY, partialTick);
        pose.popPose();
        GuiDraw.renderTooltip(graphics);
    }

    private void renderBoard(GuiGraphics graphics, int mouseX, int mouseY) {
        int left = this.left, top = this.top;
        GuiDraw.blit(graphics, BACKGROUND_TEXTURE, left, top, 0.0F, 0.0F, WIDTH, HEIGHT, 256, 256);

        inset(graphics, left + INNER_LEFT, top + TITLE_Y, INNER_RIGHT - INNER_LEFT, 12, LINE);
        this.label(graphics, this.statusTicks > 0 ? this.status : this.title, left + WIDTH / 2, top + TITLE_Y + 2, INNER_RIGHT - INNER_LEFT - 8,
                this.statusTicks > 0 ? ORIGIN : 0xFFFFFFFF, 1, true);

        // Over the grid: the layer drawn on, and where it lies.
        int gridRight = left + GRID_X + GRID;
        Component where = this.layer == 0 ? Component.translatable("gui.ultimine_addition.shape_designer.layer.front")
                : Component.translatable("gui.ultimine_addition.shape_designer.layer.behind", this.layer);
        this.label(graphics, Component.translatable("gui.ultimine_addition.shape_designer.depth", this.layer + 1, DEPTH), left + INNER_LEFT, top + CAPTION_Y, 64, 0xFFFFFFFF, 0, false);
        this.label(graphics, where, gridRight, top + CAPTION_Y, 44, this.layer == 0 ? ORIGIN : LINE, 2, false);
        this.label(graphics, Component.translatable(this.turning || this.overPreview(mouseX, mouseY) ? "gui.ultimine_addition.shape_designer.turn" : "gui.ultimine_addition.shape_designer.preview"),
                left + VIEW_X, top + CAPTION_Y, VIEW_WIDTH, 0xFFFFFFFF, 0, false);

        this.drawLayers(graphics, mouseX, mouseY);
        this.drawGrid(graphics, mouseX, mouseY);
        inset(graphics, left + VIEW_X, top + MAIN_Y, VIEW_WIDTH, VIEW_HEIGHT, LINE);
        graphics.enableScissor(left + VIEW_X, top + MAIN_Y + 1, left + VIEW_X + VIEW_WIDTH, top + MAIN_Y + VIEW_HEIGHT);
        this.drawPreview(graphics, left + VIEW_X + VIEW_WIDTH / 2.0F, top + MAIN_Y + VIEW_HEIGHT / 2.0F);
        graphics.disableScissor();

        int max = UAServerConfig.CUSTOM_SHAPES_MAX_BLOCKS.get(), blocks = this.blocks();
        inset(graphics, left + INNER_LEFT, top + NAME_Y, GRID_X + GRID - INNER_LEFT, 14, this.name.isFocused() ? 0xFFFFFFFF : LINE);
        // Beside the name: how many blocks the shape has of those it may have, as words and as a bar.
        Component count = blocks == 0 ? Component.translatable("gui.ultimine_addition.shape_designer.blocks.empty", max)
                : Component.translatable("gui.ultimine_addition.shape_designer.blocks", blocks, max);
        this.label(graphics, count, left + VIEW_X, top + NAME_Y, VIEW_WIDTH, blocks > max ? 0xFFFFD0C8 : 0xFFFFFFFF, 0, false);
        int barTop = top + NAME_Y + 11, filled = max <= 0 ? 0 : Math.min(VIEW_WIDTH, Math.round(VIEW_WIDTH * blocks / (float) max));
        inset(graphics, left + VIEW_X, barTop, VIEW_WIDTH, 3, LINE);
        graphics.fill(left + VIEW_X, barTop, left + VIEW_X + filled, barTop + 3, blocks > max ? 0xFFE0584F : 0xFFFFFFFF);
        if (within(mouseX, mouseY, left + VIEW_X, top + NAME_Y - 1, VIEW_WIDTH, 16)) {
            List<Component> lines = new ArrayList<>();
            lines.add(Component.translatable("gui.ultimine_addition.shape_designer.blocks.tooltip", max));
            if (blocks > max) lines.add(Component.translatable("gui.ultimine_addition.shape_designer.blocks.over", blocks - max).withStyle(style -> style.withColor(0xFF8A80)));
            List<net.minecraft.util.FormattedCharSequence> wrapped = new ArrayList<>();
            for (Component line : lines) wrapped.addAll(this.font.split(line, 170));
            GuiDraw.tooltipLines(graphics, this.font, wrapped, mouseX, mouseY);
        }
        ShapePayload.Mine mine = ShapePayload.Mine.latest();
        boolean fine = blocks > 0 && blocks <= max, chosen = this.picked() != null;
        this.save.active = fine && mine.shapes().size() < mine.limit();
        this.load.active = chosen;
        this.replace.active = chosen && fine;
        this.delete.active = chosen;
        this.drawMine(graphics, mine, mouseX, mouseY);
    }

    // A sheet of the blueprint as the board's, without the clip.
    private static void sheet(GuiGraphics graphics, int x, int y, int width, int height) {
        int right = x + width, bottom = y + height;
        graphics.fill(x + 2, y, right - 2, bottom, 0xFF47494E);
        graphics.fill(x, y + 2, right, bottom - 2, 0xFF47494E);
        graphics.fill(x + 1, y + 1, right - 1, bottom - 1, 0xFF47494E);
        graphics.fill(x + 1, y + 2, right - 1, bottom - 2, 0xFF6B7386);
        graphics.fill(x + 2, y + 1, right - 2, bottom - 1, 0xFF6B7386);
        graphics.fill(x + 2, y + 1, right - 2, y + 2, 0xFF8B92AA);
        graphics.fill(x + 1, y + 2, x + 2, bottom - 2, 0xFF8B92AA);
        graphics.fill(right - 2, y + 2, right - 1, bottom - 2, 0xFF556275);
        graphics.fill(x + 2, bottom - 2, right - 2, bottom - 1, 0xFF556275);
        graphics.fill(x + 7, y + 8, right - 6, bottom - 6, 0xFF3F4656);
        graphics.fill(x + 7, y + 8, right - 7, bottom - 7, 0xFF2A8FC4);
        graphics.fill(x + 8, y + 9, right - 8, bottom - 8, 0xFF3EB6ED);
        for (int line = y + 16; line < bottom - 8; line += 8) graphics.fill(x + 8, line, right - 8, line + 1, 0xFF52BFF0);
        for (int line = x + 15; line < right - 8; line += 8) graphics.fill(line, y + 9, line + 1, bottom - 8, 0xFF52BFF0);
        graphics.fill(x + 10, y + 11, right - 10, y + 12, LINE);
        graphics.fill(x + 10, bottom - 11, right - 10, bottom - 10, LINE);
        graphics.fill(x + 10, y + 11, x + 11, bottom - 10, LINE);
        graphics.fill(right - 11, y + 11, right - 10, bottom - 10, LINE);
    }

    // The player's shapes: each a plate with its front layer (what lies behind it dimmer), its name and its size.
    private void drawMine(GuiGraphics graphics, ShapePayload.Mine mine, int mouseX, int mouseY) {
        int x = this.sideLeft(), top = this.top;
        sheet(graphics, this.left + WIDTH + SIDE_GAP, top + SIDE_TOP, SIDE_WIDTH, SIDE_HEIGHT);
        inset(graphics, x, top + TITLE_Y, SIDE_INNER, 12, LINE);
        boolean full = mine.shapes().size() >= mine.limit();
        Component count = Component.literal(mine.shapes().size() + "/" + mine.limit());
        this.label(graphics, Component.translatable("gui.ultimine_addition.shape_designer.mine"), x + 4, top + TITLE_Y + 2, SIDE_INNER - 12 - this.font.width(count), 0xFFFFFFFF, 0, true);
        GuiDraw.text(graphics, this.font, count, x + SIDE_INNER - 4 - this.font.width(count), top + TITLE_Y + 2, full ? ORIGIN : LINE, true);

        this.scroll = Mth.clamp(this.scroll, 0, Math.max(0, mine.shapes().size() - VISIBLE));
        if (mine.shapes().isEmpty()) {
            int y = top + LIST_Y + 4;
            for (var line : this.font.split(Component.translatable("gui.ultimine_addition.shape_designer.mine.none"), SIDE_INNER)) {
                GuiDraw.text(graphics, this.font, line, x, y, 0xFFFFFFFF, false);
                y += 10;
            }
        }
        ShapePayload.Saved hovered = this.entryAt(mouseX, mouseY);
        for (int i = 0; i < VISIBLE && i + this.scroll < mine.shapes().size(); i++) {
            ShapePayload.Saved saved = mine.shapes().get(i + this.scroll);
            int y = top + LIST_Y + i * ENTRY_PITCH;
            boolean chosen = saved.id().equals(this.selected);
            if (chosen) graphics.fill(x - 2, y - 2, x + SIDE_INNER + 2, y + ENTRY_HEIGHT + 2, 0xFFFFFFFF);
            inset(graphics, x, y, SIDE_INNER, ENTRY_HEIGHT, chosen ? INSET_SHADE : saved == hovered ? WASH : LINE);
            int blocks = 0, layers = 1;
            for (int depth = DEPTH - 1; depth >= 0; depth--) {
                for (int row = 0; row < SIZE; row++) {
                    for (int column = 0; column < SIZE; column++) {
                        boolean origin = isOrigin(depth, row, column);
                        if (!origin && !drawn(saved.rows(), depth, row, column)) continue;
                        if (!origin) blocks++;
                        layers = Math.max(layers, depth + 1);
                        int cx = x + 4 + column * THUMB_CELL, cy = y + 4 + row * THUMB_CELL;
                        graphics.fill(cx, cy, cx + THUMB_CELL, cy + THUMB_CELL, origin ? ORIGIN : depth == 0 ? 0xFFFFFFFF : GHOST);
                    }
                }
            }
            this.label(graphics, Component.literal(saved.name()), x + 23, y + 3, SIDE_INNER - 26, 0xFFFFFFFF, 0, false);
            Component size = layers > 1 ? Component.translatable("gui.ultimine_addition.shape_designer.mine.size.deep", blocks, layers)
                    : Component.translatable("gui.ultimine_addition.shape_designer.mine.size", blocks);
            this.label(graphics, size, x + 23, y + 13, SIDE_INNER - 26, LINE, 0, false);
        }
        // More above or below what is shown.
        if (this.scroll > 0) graphics.fill(x + SIDE_INNER / 2 - 3, top + LIST_Y - 3, x + SIDE_INNER / 2 + 3, top + LIST_Y - 2, 0xFFFFFFFF);
        if (this.scroll + VISIBLE < mine.shapes().size()) graphics.fill(x + SIDE_INNER / 2 - 3, top + LIST_Y + VISIBLE * ENTRY_PITCH - 1, x + SIDE_INNER / 2 + 3, top + LIST_Y + VISIBLE * ENTRY_PITCH, 0xFFFFFFFF);
    }

    // The layers, front to back, each a small picture of what is drawn on it; the one being drawn on is framed.
    private void drawLayers(GuiGraphics graphics, int mouseX, int mouseY) {
        int x = this.left + INNER_LEFT, hovered = this.thumbAt(mouseX, mouseY);
        for (int depth = 0; depth < DEPTH; depth++) {
            int y = this.top + MAIN_Y + depth * THUMB_PITCH;
            boolean current = depth == this.layer;
            if (current) graphics.fill(x - 2, y - 2, x + THUMB + 2, y + THUMB + 2, 0xFFFFFFFF);
            inset(graphics, x, y, THUMB, THUMB, current ? INSET_SHADE : depth == hovered ? WASH : 0xFF2A8FC4);
            for (int row = 0; row < SIZE; row++) {
                for (int column = 0; column < SIZE; column++) {
                    if (!this.solid(depth, row, column)) continue;
                    int cx = x + column * THUMB_CELL, cy = y + row * THUMB_CELL;
                    graphics.fill(cx, cy, cx + THUMB_CELL, cy + THUMB_CELL, isOrigin(depth, row, column) ? ORIGIN : current ? 0xFFFFFFFF : BLOCK);
                }
            }
            // The current layer points at the grid it is drawn on.
            if (current) {
                graphics.fill(x + THUMB + 2, y + THUMB / 2 - 1, x + THUMB + 3, y + THUMB / 2 + 2, 0xFFFFFFFF);
                graphics.fill(x + THUMB + 3, y + THUMB / 2, x + THUMB + 4, y + THUMB / 2 + 1, 0xFFFFFFFF);
            }
        }
        if (hovered >= 0) {
            GuiDraw.tooltip(graphics, this.font, Component.translatable("gui.ultimine_addition.shape_designer.depth", hovered + 1, DEPTH), mouseX, mouseY);
        }
    }

    private void drawGrid(GuiGraphics graphics, int mouseX, int mouseY) {
        int gridLeft = this.left + GRID_X, gridTop = this.top + MAIN_Y;
        inset(graphics, gridLeft, gridTop, GRID, GRID, LINE);
        graphics.fill(gridLeft, gridTop, gridLeft + GRID, gridTop + GRID, CELL_LINE);
        int hovered = this.painting == null && this.turning ? -1 : this.cellAt(mouseX, mouseY);
        for (int row = 0; row < SIZE; row++) {
            for (int column = 0; column < SIZE; column++) {
                int x = gridLeft + column * CELL + 1, y = gridTop + row * CELL + 1, end = CELL - 1;
                boolean origin = isOrigin(this.layer, row, column);
                graphics.fill(x, y, x + end, y + end, INSET);
                if (origin || this.cells[this.layer][row][column]) {
                    // A block: lit along the top and left, shaded along the bottom and right.
                    graphics.fill(x, y, x + end, y + end, origin ? 0xFFFFE9A0 : 0xFFFFFFFF);
                    graphics.fill(x + 1, y + 1, x + end, y + end, origin ? 0xFFC99A2E : 0xFF8FC8E6);
                    graphics.fill(x + 1, y + 1, x + end - 1, y + end - 1, origin ? ORIGIN : BLOCK);
                } else if (this.solid(this.layer - 1, row, column)) {
                    // What the layer in front has here shows through, to draw against.
                    graphics.fill(x + 3, y + 3, x + end - 3, y + end - 3, GHOST);
                    if (isOrigin(this.layer - 1, row, column)) graphics.fill(x + 6, y + 6, x + end - 6, y + end - 6, ORIGIN);
                }
                if (row * SIZE + column == hovered) {
                    graphics.fill(x - 1, y - 1, x + end + 1, y, 0xFFFFFFFF);
                    graphics.fill(x - 1, y + end, x + end + 1, y + end + 1, 0xFFFFFFFF);
                    graphics.fill(x - 1, y, x, y + end, 0xFFFFFFFF);
                    graphics.fill(x + end, y, x + end + 1, y + end, 0xFFFFFFFF);
                }
            }
        }
    }

    // A point or direction of the shape (right, up, behind) as seen in the preview: right and up on the screen, and how
    // far from the eye.
    private float[] turned(float x, float y, float z) {
        float cosYaw = Mth.cos(this.yaw), sinYaw = Mth.sin(this.yaw), cosPitch = Mth.cos(this.pitch), sinPitch = Mth.sin(this.pitch);
        float across = x * cosYaw - z * sinYaw, away = x * sinYaw + z * cosYaw;
        return new float[]{across, y * cosPitch + away * sinPitch, away * cosPitch - y * sinPitch};
    }

    // A parallelogram from a corner along two edges: one rectangle, sheared into place by the pose.
    private static void quad(GuiGraphics graphics, float x, float y, float ax, float ay, float bx, float by, int color) {
        var pose = graphics.pose();
        pose.pushPose();
        // Kept facing the same way round whichever way the edges run (a face seen from behind would be culled).
        if (ax * by - ay * bx < 0) pose.mulPoseMatrix(affine(bx, by, ax, ay, x, y));
        else pose.mulPoseMatrix(affine(ax, ay, bx, by, x, y));
        graphics.fill(0, 0, 1, 1, color);
        pose.popPose();
    }

    // The 2D transform taking (1, 0) to (ax, ay) and (0, 1) to (bx, by), moved to (x, y), as a matrix of the pose.
    private static Matrix4f affine(float ax, float ay, float bx, float by, float x, float y) {
        return new Matrix4f(
                ax, ay, 0.0F, 0.0F,
                bx, by, 0.0F, 0.0F,
                0.0F, 0.0F, 1.0F, 0.0F,
                x, y, 0.0F, 1.0F);
    }

    private static void line(GuiGraphics graphics, float x1, float y1, float x2, float y2, int color) {
        float dx = x2 - x1, dy = y2 - y1, length = Mth.sqrt(dx * dx + dy * dy);
        if (length < 0.01F) return;
        float nx = -dy / length, ny = dx / length;
        quad(graphics, x1 - nx / 2, y1 - ny / 2, dx, dy, nx, ny, color);
    }

    private static int lit(int color, float light) {
        int r = (int) ((color >> 16 & 0xFF) * light), g = (int) ((color >> 8 & 0xFF) * light), b = (int) ((color & 0xFF) * light);
        return 0xFF000000 | r << 16 | g << 8 | b;
    }

    private record Solid(int layer, int row, int column, float away) {
    }

    // The whole shape as blocks, seen from the angle it was turned to: the farthest drawn first, each with the faces
    // that look at the eye and aren't against another block. Then the outline of the layer being drawn on.
    private void drawPreview(GuiGraphics graphics, float centerX, float centerY) {
        this.shownDepth += (Math.max(this.deepest(), this.layer) + 1 - this.shownDepth) * 0.2F;
        float unit = Math.min(VIEW_WIDTH, VIEW_HEIGHT) * 1.2F / Mth.sqrt(SIZE * SIZE * 2 + this.shownDepth * this.shownDepth);
        float middle = (this.shownDepth - 1.0F) / 2.0F;

        List<Solid> solids = new ArrayList<>();
        for (int depth = 0; depth < DEPTH; depth++) {
            for (int row = 0; row < SIZE; row++) {
                for (int column = 0; column < SIZE; column++) {
                    if (this.solid(depth, row, column)) solids.add(new Solid(depth, row, column, this.turned(column - SIZE / 2, SIZE / 2 - row, depth - middle)[2]));
                }
            }
        }
        solids.sort(Comparator.comparingDouble(solid -> -solid.away()));

        for (Solid solid : solids) {
            boolean origin = isOrigin(solid.layer(), solid.row(), solid.column());
            int color = origin ? ORIGIN : solid.layer() == this.layer ? SOLID_ACTIVE : SOLID, edge = origin ? ORIGIN_EDGE : SOLID_EDGE;
            float x = solid.column() - SIZE / 2, y = SIZE / 2 - solid.row(), z = solid.layer() - middle;
            for (int face = 0; face < NORMALS.length; face++) {
                int[] n = NORMALS[face], u = SPAN_U[face], v = SPAN_V[face];
                if (this.turned(n[0], n[1], n[2])[2] > -0.001F) continue;
                if (this.solid(solid.layer() + n[2], solid.row() - n[1], solid.column() + n[0])) continue;
                float[] corner = this.turned(x + (n[0] - u[0] - v[0]) / 2.0F, y + (n[1] - u[1] - v[1]) / 2.0F, z + (n[2] - u[2] - v[2]) / 2.0F);
                float[] a = this.turned(u[0], u[1], u[2]), b = this.turned(v[0], v[1], v[2]);
                float ox = centerX + corner[0] * unit, oy = centerY - corner[1] * unit;
                float ax = a[0] * unit, ay = -a[1] * unit, bx = b[0] * unit, by = -b[1] * unit;
                quad(graphics, ox, oy, ax, ay, bx, by, edge);
                float rim = Math.min(0.12F, 0.8F / unit);
                quad(graphics, ox + (ax + bx) * rim, oy + (ay + by) * rim, ax * (1 - rim * 2), ay * (1 - rim * 2), bx * (1 - rim * 2), by * (1 - rim * 2), lit(color, LIGHT[face]));
            }
        }

        float half = SIZE / 2.0F;
        float[][] corners = {this.turned(-half, half, this.layer - middle), this.turned(half, half, this.layer - middle),
                this.turned(half, -half, this.layer - middle), this.turned(-half, -half, this.layer - middle)};
        for (int i = 0; i < corners.length; i++) {
            float[] from = corners[i], to = corners[(i + 1) % corners.length];
            line(graphics, centerX + from[0] * unit, centerY - from[1] * unit, centerX + to[0] * unit, centerY - to[1] * unit, 0x90FFFFFF);
        }
    }

    // Closing shows the Shape Selector it popped up over again.
    @Override
    public void onClose() {
        if (this.parent != null) this.minecraft.setScreen(this.parent);
        else super.onClose();
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    // A button of the blueprint, as the Shape Selector's, with its name on it.
    private static final class BlueprintButton extends AbstractButton {
        private final Runnable onPress;
        private Component tooltip;

        private BlueprintButton tooltip(String key) {
            this.tooltip = Component.translatable(key);
            return this;
        }

        private BlueprintButton(int x, int y, int width, int height, Component message, Runnable onPress) {
            super(x, y, width, height, message);
            this.onPress = onPress;
        }

        @Override
        public void onPress() {
            this.onPress.run();
        }

        @Override
        protected void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
            ShapeSelectorScreen.drawBlueprintPlate(graphics, this.getX(), this.getY(), this.getWidth(), this.getHeight(), this.active, this.active && this.isHoveredOrFocused());
            var font = Minecraft.getInstance().font;
            int width = font.width(this.getMessage());
            float scale = Math.min(1.0F, (this.getWidth() - 6) / (float) Math.max(1, width));
            var pose = graphics.pose();
            pose.pushPose();
            pose.translate(this.getX() + this.getWidth() / 2.0F, this.getY() + this.getHeight() / 2.0F, 0.0F);
            pose.scale(scale, scale, 1.0F);
            GuiDraw.centeredText(graphics, font, this.getMessage(), 0, -4, this.active ? 0xFFFFFFFF : 0xFF8FC8E6);
            pose.popPose();
            if (this.tooltip != null && this.isHovered()) GuiDraw.tooltipLines(graphics, font, font.split(this.tooltip, 170), mouseX, mouseY);
        }

        @Override
        protected void updateWidgetNarration(NarrationElementOutput output) {
            this.defaultButtonNarrationText(output);
        }
    }
}
