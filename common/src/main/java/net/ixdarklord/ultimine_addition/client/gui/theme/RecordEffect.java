package net.ixdarklord.ultimine_addition.client.gui.theme;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.util.ARGB;
import net.minecraft.util.Mth;

/**
 * The card viewer's background effect for each {@link RecordTheme}, drawn in screen space over its gradient: little
 * pixel scenes inspired by Minecraft. Everything is computed from the time, so nothing is kept between frames.
 */
public enum RecordEffect {
    /** Snowflakes drifting down, swaying. */
    SNOW {
        @Override
        void render(Ctx c) {
            int n = c.count(420);
            for (int i = 0; i < n; i++) {
                float speed = 7 + 10 * c.r(i, 1);
                float y = c.fall(i, speed);
                float x = c.x(i) + Mth.sin(c.t * (0.6F + c.r(i, 2)) + i) * 4;
                int size = c.r(i, 3) > 0.75F ? 2 : 1;
                c.dot(x, y, size, ARGB.color(0.45F + 0.4F * c.r(i, 4), 0xFFFFFF));
            }
        }
    },
    /** Autumn leaves tumbling down. */
    LEAVES {
        private final int[] colors = {0xE3791B, 0xC2451E, 0xF2B233, 0x9E3A1A};

        @Override
        void render(Ctx c) {
            int n = c.count(900);
            for (int i = 0; i < n; i++) {
                float y = c.fall(i, 9 + 7 * c.r(i, 1));
                float x = c.x(i) + Mth.sin(c.t * 1.1F + i * 1.7F) * 9;
                int color = ARGB.color(0.75F, this.colors[i % this.colors.length]);
                boolean flip = Mth.sin(c.t * 2.2F + i) > 0;
                c.dot(x, y, 2, color);
                c.dot(flip ? x + 2 : x - 1, y + (flip ? -1 : 2), 1, color);
            }
        }
    },
    /** Allium petals floating up. */
    ALLIUM {
        @Override
        void render(Ctx c) {
            int n = c.count(700);
            for (int i = 0; i < n; i++) {
                float y = c.rise(i, 5 + 6 * c.r(i, 1));
                float x = c.x(i) + Mth.sin(c.t * 0.9F + i * 2.3F) * 6;
                int color = ARGB.color(0.7F, i % 3 == 0 ? 0x9E4BC4 : 0xD57BE8);
                c.dot(x, y, 1, color);
                c.dot(x + 1, y, 1, color);
                if (i % 2 == 0) c.dot(x, y - 1, 1, ARGB.color(0.5F, 0xF2C4FF));
            }
        }
    },
    /** Soft blocky clouds sailing by. */
    CLOUDS {
        @Override
        void render(Ctx c) {
            int n = Math.max(3, c.count(5000));
            for (int i = 0; i < n; i++) {
                float w = 24 + 26 * c.r(i, 1), h = 6 + 5 * c.r(i, 2);
                float x = c.x0 - w + Mth.frac(c.r(i, 3) + c.t * (0.012F + 0.01F * c.r(i, 4))) * (c.w + w * 2);
                float y = c.y0 + c.r(i, 5) * (c.h - h);
                int color = ARGB.color(0.10F + 0.06F * c.r(i, 6), 0xFFFFFF);
                c.rect(x, y + h / 3, w, h * 2 / 3, color);
                c.rect(x + w * 0.2F, y, w * 0.45F, h / 3 + 1, color);
            }
        }
    },
    /** Pollen in the air and a bee on its rounds. */
    POLLEN {
        @Override
        void render(Ctx c) {
            int n = c.count(500);
            for (int i = 0; i < n; i++) {
                float x = c.x(i) + Mth.sin(c.t * 0.7F + i) * 5;
                float y = c.y(i) + Mth.cos(c.t * 0.5F + i * 1.3F) * 4;
                c.dot(x, y, 1, ARGB.color(0.35F + 0.35F * (Mth.sin(c.t * 2 + i) + 1) / 2, 0xFFE272));
            }
            // A bee: a figure eight across the view.
            float bx = c.x0 + c.w / 2 + Mth.sin(c.t * 0.45F) * c.w * 0.38F;
            float by = c.y0 + c.h / 2 + Mth.sin(c.t * 0.9F) * c.h * 0.28F;
            boolean wingsUp = (int) (c.t * 12) % 2 == 0;
            c.rect(bx, by, 4, 3, 0xE0FFD23A);
            c.rect(bx + 1, by, 1, 3, 0xE03A2A10);
            c.rect(bx + 3, by, 1, 3, 0xE03A2A10);
            c.rect(bx + 1, by - (wingsUp ? 2 : 1), 2, 1, 0xB0E9F6FF);
        }
    },
    /** Little slime cubes bouncing. */
    SLIME {
        @Override
        void render(Ctx c) {
            int n = Math.max(3, c.count(2200));
            for (int i = 0; i < n; i++) {
                int size = 3 + (int) (4 * c.r(i, 1));
                float speed = 2.2F + 1.4F * c.r(i, 2);
                float bounce = Math.abs(Mth.sin(c.t * speed + i * 1.9F));
                float x = c.x0 + Mth.frac(c.r(i, 3) + c.t * 0.02F * (c.r(i, 4) - 0.5F)) * (c.w - size);
                float y = c.groundY - size - 2 - bounce * (8 + 10 * c.r(i, 5));
                // Squashed as it lands.
                float squash = bounce < 0.15F ? 1 : 0;
                c.rectOnGround(x - squash, y + squash, size + squash * 2, size - squash, 0x707FDF5A);
                c.rectOnGround(x + 1, y + 1 + squash, 1, 1, 0x901F4A18);
                c.rectOnGround(x + size - 2, y + 1 + squash, 1, 1, 0x901F4A18);
            }
        }
    },
    /** Cherry blossom petals falling on the breeze. */
    CHERRY {
        @Override
        void render(Ctx c) {
            int n = c.count(420);
            for (int i = 0; i < n; i++) {
                float y = c.fall(i, 6 + 7 * c.r(i, 1));
                float x = c.x(i) + c.t * 4 % c.w + Mth.sin(c.t * 1.3F + i * 2.1F) * 7;
                x = c.x0 + Mth.positiveModulo(x - c.x0, c.w);
                int color = ARGB.color(0.8F, i % 4 == 0 ? 0xFFE3EE : 0xFFB7D5);
                // A petal turning over as it falls: a 2x2 with a tip that flips side.
                boolean flip = Mth.sin(c.t * 2.6F + i) > 0;
                c.dot(x, y, 2, color);
                c.dot(flip ? x + 2 : x - 1, y + 1, 1, ARGB.color(0.6F, 0xFF8FB8));
            }
        }
    },
    /** Gravel dust sifting down through a faint haze. */
    DUST {
        @Override
        void render(Ctx c) {
            for (int band = 0; band < 3; band++) {
                float y = c.y0 + c.h * (0.25F + band * 0.25F) + Mth.sin(c.t * 0.3F + band) * 6;
                c.rect(c.x0, y, c.w, 5, ARGB.color(0.05F, 0xD0D0D0));
            }
            int n = c.count(380);
            for (int i = 0; i < n; i++) {
                float y = c.fall(i, 14 + 16 * c.r(i, 1));
                c.dot(c.x(i), y, c.r(i, 2) > 0.7F ? 2 : 1, ARGB.color(0.55F, i % 2 == 0 ? 0x9A9A9A : 0x6E6E6E));
            }
        }
    },
    /** Rain slanting down, splashing at the bottom. */
    RAIN {
        @Override
        void render(Ctx c) {
            int n = c.count(260);
            for (int i = 0; i < n; i++) {
                float speed = 70 + 40 * c.r(i, 1);
                float y = c.fall(i, speed);
                float x = c.x(i) - (y - c.y0) * 0.25F;
                x = c.x0 + Mth.positiveModulo(x - c.x0, c.w);
                int color = ARGB.color(0.35F + 0.2F * c.r(i, 2), 0xB9DDF2);
                for (int k = 0; k < 4; k++) c.dot(x + k * 0.25F, y - k, 1, color);
            }
        }
    },
    /** Bubbles rising, wobbling, from the ocean floor. */
    BUBBLES {
        @Override
        void render(Ctx c) {
            int n = c.count(700);
            for (int i = 0; i < n; i++) {
                float y = c.rise(i, 10 + 12 * c.r(i, 1));
                float x = c.x(i) + Mth.sin(c.t * 2.2F + i) * 2;
                int size = c.r(i, 2) > 0.6F ? 4 : 3;
                int color = ARGB.color(0.55F, 0xC8FFF6);
                c.rect(x + 1, y, size - 2, 1, color);
                c.rect(x + 1, y + size - 1, size - 2, 1, color);
                c.rect(x, y + 1, 1, size - 2, color);
                c.rect(x + size - 1, y + 1, 1, size - 2, color);
                c.dot(x + 1, y + 1, 1, 0x90FFFFFF);
            }
        }
    },
    /** Amethyst glints twinkling in place. */
    AMETHYST {
        @Override
        void render(Ctx c) {
            int n = c.count(420);
            for (int i = 0; i < n; i++) {
                float age = c.t * (0.35F + 0.3F * c.r(i, 1)) + c.r(i, 2);
                float a = Mth.sin(Mth.frac(age) * Mth.PI);
                if (a < 0.05F) continue;
                // Each glint shows up somewhere new every time it comes back.
                int spot = i + (int) age * 97;
                float x = c.x(spot), y = c.y(spot);
                int color = ARGB.color(a * 0.85F, i % 3 == 0 ? 0xE3C8FF : 0xB27CF0);
                c.dot(x, y, 1, color);
                if (a > 0.5F) {
                    int arm = ARGB.color((a - 0.5F) * 1.2F, 0xE3C8FF);
                    c.dot(x - 1, y, 1, arm);
                    c.dot(x + 1, y, 1, arm);
                    c.dot(x, y - 1, 1, arm);
                    c.dot(x, y + 1, 1, arm);
                }
            }
        }
    },
    /** Light rippling like sunlight through deep water. */
    CAUSTICS {
        @Override
        void render(Ctx c) {
            // Sampled where the world is, so the light slides along with the view as it pans.
            int cell = 4;
            int ox = Mth.floor(c.panX) % cell, oy = Mth.floor(c.panY) % cell;
            for (int y = oy - cell; y < c.viewH; y += cell) {
                for (int x = ox - cell; x < c.viewW; x += cell) {
                    float wx = x - c.panX, wy = y - c.panY;
                    float v = Mth.sin(wx * 0.09F + c.t * 0.9F) + Mth.sin(wy * 0.13F - c.t * 0.7F) + Mth.sin((wx + wy) * 0.06F + c.t * 0.5F);
                    float a = Mth.clamp((v - 1.6F) * 0.12F, 0.0F, 0.16F);
                    if (a > 0.01F) c.rectRaw(c.viewX + x, c.viewY + y, cell, cell, ARGB.color(a, 0x9FC4FF));
                }
            }
        }
    },
    /** Mushroom spores drifting lazily. */
    SPORES {
        @Override
        void render(Ctx c) {
            int n = c.count(360);
            for (int i = 0; i < n; i++) {
                float y = c.rise(i, 2 + 3 * c.r(i, 1));
                float x = c.x(i) + Mth.sin(c.t * 0.5F + i * 1.1F) * 8;
                int color = ARGB.color(0.35F + 0.3F * c.r(i, 2), i % 5 == 0 ? 0xE2442B : 0xE6C9A0);
                c.dot(x, y, 1, color);
            }
        }
    },
    /** Fireflies wandering, glowing on and off. */
    FIREFLIES {
        @Override
        void render(Ctx c) {
            int n = c.count(900);
            for (int i = 0; i < n; i++) {
                float x = c.x(i) + Mth.sin(c.t * (0.3F + 0.2F * c.r(i, 1)) + i) * 12;
                float y = c.y(i) + Mth.sin(c.t * (0.4F + 0.2F * c.r(i, 2)) + i * 2.3F) * 8;
                float glow = Math.max(0, Mth.sin(c.t * (1.2F + c.r(i, 3)) + i * 3.1F));
                if (glow < 0.05F) continue;
                c.rect(x - 1, y - 1, 3, 3, ARGB.color(glow * 0.18F, 0xD8FF6A));
                c.dot(x, y, 1, ARGB.color(0.35F + glow * 0.65F, 0xF2FF9A));
            }
        }
    },
    /** Nether embers rising and flickering out. */
    EMBERS {
        @Override
        void render(Ctx c) {
            int n = c.count(420);
            for (int i = 0; i < n; i++) {
                float life = Mth.frac(c.r(i, 1) + c.t * (0.08F + 0.06F * c.r(i, 2)));
                float y = c.y0 + c.h - life * c.h;
                float x = c.x(i) + Mth.sin(c.t * 1.7F + i) * 3;
                float flicker = 0.6F + 0.4F * Mth.sin(c.t * 9 + i * 2.7F);
                int color = ARGB.color((1.0F - life) * flicker * 0.9F, life < 0.4F ? 0xFFE45A : 0xFF7A2A);
                c.dot(x, y, c.r(i, 3) > 0.8F ? 2 : 1, color);
            }
        }
    },
    /** A night sky: twinkling stars and now and then a shooting star. */
    STARS {
        @Override
        void render(Ctx c) {
            int n = c.count(260);
            for (int i = 0; i < n; i++) {
                float twinkle = 0.35F + 0.65F * (Mth.sin(c.t * (1.0F + 2 * c.r(i, 1)) + i * 4.1F) + 1) / 2;
                float x = c.x(i), y = c.y(i);
                c.dot(x, y, 1, ARGB.color(twinkle * 0.8F, i % 7 == 0 ? 0xFFF3A0 : 0xE8E4FF));
                if (i % 11 == 0 && twinkle > 0.85F) {
                    int arm = ARGB.color((twinkle - 0.85F) * 3, 0xE8E4FF);
                    c.dot(x - 1, y, 1, arm);
                    c.dot(x + 1, y, 1, arm);
                    c.dot(x, y - 1, 1, arm);
                    c.dot(x, y + 1, 1, arm);
                }
            }
            // A shooting star every few seconds.
            float period = 6.0F;
            int shot = (int) (c.t / period);
            float p = (c.t - shot * period) / 0.9F;
            if (p < 1.0F) {
                float sx = c.x0 + c.w * (0.2F + 0.5F * hash(shot, 1)), sy = c.y0 + c.h * 0.1F * hash(shot, 2);
                float hx = sx + p * c.w * 0.4F, hy = sy + p * c.h * 0.35F;
                for (int k = 0; k < 10; k++) {
                    c.dot(hx - k * 1.15F, hy - k, 1, ARGB.color((1.0F - k / 10.0F) * (1.0F - p) * 0.9F, 0xFFFFFF));
                }
            }
        }
    };

    abstract void render(Ctx c);

    /** Draws the effect over the given area (the caller clips to it), not panned. */
    public void draw(GuiGraphicsExtractor graphics, ScreenRectangle bounds, boolean animated) {
        this.draw(graphics, bounds, animated, 0, 0);
    }

    /**
     * Draws the effect over the given area (the caller clips to it), moved along with a view panned by
     * ({@code panX}, {@code panY}) screen pixels: the particles wrap around the area as they scroll.
     */
    public void draw(GuiGraphicsExtractor graphics, ScreenRectangle bounds, boolean animated, float panX, float panY) {
        if (bounds.width() <= 0 || bounds.height() <= 0) return;
        float t = animated ? (System.currentTimeMillis() % 3_600_000L) / 1000.0F : 12.0F;
        this.render(new Ctx(graphics, bounds, t, this.ordinal(), panX, panY));
    }

    static float hash(int a, int b) {
        return (Mth.murmurHash3Mixer(a * 0x9E3779B9 ^ b * 0x85EBCA6B) >>> 8) / (float) (1 << 24);
    }

    // Particles live in the view grown by this much on every side, so the ones wrapping around as the view pans
    // come in from off screen instead of popping up at the edge.
    private static final int MARGIN = 40;

    static final class Ctx {
        final GuiGraphicsExtractor g;
        // The area particles live in (the view and its margin), where the effects place them.
        final float x0, y0, w, h, t;
        // The view itself, its ground (its bottom) and how far it's panned.
        final float viewX, viewY, viewW, viewH, groundY, panX, panY;
        final int salt;

        Ctx(GuiGraphicsExtractor g, ScreenRectangle view, float t, int salt, float panX, float panY) {
            this.g = g;
            this.viewX = view.left();
            this.viewY = view.top();
            this.viewW = view.width();
            this.viewH = view.height();
            this.groundY = view.bottom();
            this.x0 = view.left() - MARGIN;
            this.y0 = view.top() - MARGIN;
            this.w = view.width() + MARGIN * 2;
            this.h = view.height() + MARGIN * 2;
            this.t = t;
            this.panX = panX;
            this.panY = panY;
            this.salt = salt * 131;
        }

        /** How many particles for this area, one per {@code areaPer} pixels. */
        int count(int areaPer) {
            return Mth.clamp((int) (this.w * this.h / areaPer), 1, 400);
        }

        float r(int i, int k) {
            return hash(i + this.salt, k);
        }

        float x(int i) {
            return this.x0 + this.r(i, 11) * this.w;
        }

        float y(int i) {
            return this.y0 + this.r(i, 12) * this.h;
        }

        /** Falling at {@code speed} pixels a second, wrapping to the top. */
        float fall(int i, float speed) {
            return this.y0 + Mth.frac(this.r(i, 13) + this.t * speed / this.h) * this.h;
        }

        /** Rising at {@code speed} pixels a second, wrapping to the bottom. */
        float rise(int i, float speed) {
            return this.y0 + this.h - Mth.frac(this.r(i, 14) + this.t * speed / this.h) * this.h;
        }

        void dot(float x, float y, int size, int color) {
            this.rect(x, y, size, size, color);
        }

        /** A rectangle at a place in the particle area, moved with the pan and wrapped around the area. */
        void rect(float x, float y, float w, float h, int color) {
            this.rectRaw(this.wrapX(x), this.y0 + Mth.positiveModulo(y - this.y0 + this.panY, this.h), w, h, color);
        }

        /** Like {@link #rect}, but it stays on the view's ground: it only follows the pan sideways. */
        void rectOnGround(float x, float y, float w, float h, int color) {
            this.rectRaw(this.wrapX(x), y, w, h, color);
        }

        private float wrapX(float x) {
            return this.x0 + Mth.positiveModulo(x - this.x0 + this.panX, this.w);
        }

        /** A rectangle in screen space, as is. */
        void rectRaw(float x, float y, float w, float h, int color) {
            int ix = Math.round(x), iy = Math.round(y);
            this.g.fill(ix, iy, ix + Math.max(1, Math.round(w)), iy + Math.max(1, Math.round(h)), color);
        }
    }
}
