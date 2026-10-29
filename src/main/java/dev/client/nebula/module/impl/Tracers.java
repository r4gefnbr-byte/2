package dev.client.nebula.module.impl;

import dev.client.nebula.gui.Theme;
import dev.client.nebula.module.Module;
import dev.client.nebula.module.Setting;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.Vec3d;
import org.lwjgl.glfw.GLFW;

/**
 * 2D tracers drawn on the HUD. Each player's position is projected to the screen with plain
 * camera math (no render-pipeline APIs), then a line is drawn from the bottom-center.
 * Players behind you are pushed to the screen edge so the line still points the right way.
 */
public class Tracers extends Module {
    private final Setting range = add("Range", 128, 16, 512, 16);

    public Tracers() { super("Tracers", GLFW.GLFW_KEY_Y); }

    public void render(DrawContext ctx) {
        if (mc.player == null || mc.world == null) return;

        int w = ctx.getScaledWindowWidth(), h = ctx.getScaledWindowHeight();
        double yaw = Math.toRadians(mc.player.getYaw());
        double pitch = Math.toRadians(mc.player.getPitch());

        // Camera basis. MC yaw 0 faces +Z; right-hand side at yaw 0 is -X.
        Vec3d f = new Vec3d(-Math.sin(yaw) * Math.cos(pitch), -Math.sin(pitch), Math.cos(yaw) * Math.cos(pitch));
        Vec3d r = new Vec3d(-Math.cos(yaw), 0, -Math.sin(yaw));
        Vec3d u = r.crossProduct(f);

        double tanY = Math.tan(Math.toRadians(mc.options.getFov().getValue()) / 2.0);
        double tanX = tanY * ((double) w / h);
        Vec3d eye = mc.player.getEyePos();
        double max = range.get();

        for (PlayerEntity p : mc.world.getPlayers()) {
            if (p == mc.player || !p.isAlive()) continue;
            Vec3d d = p.getBoundingBox().getCenter().subtract(eye);
            if (d.lengthSquared() > max * max) continue;

            double cx = d.dotProduct(r), cy = d.dotProduct(u), cz = d.dotProduct(f);
            double sx, sy;
            if (cz > 0.05) {
                sx = w / 2.0 + (cx / (cz * tanX)) * (w / 2.0);
                sy = h / 2.0 - (cy / (cz * tanY)) * (h / 2.0);
            } else { // behind: aim along the direction and let the clamp put it on the edge
                double len = Math.max(1e-4, Math.hypot(cx, cy));
                sx = w / 2.0 + (cx / len) * w;
                sy = h / 2.0 - (cy / len) * h;
            }
            sx = Math.max(2, Math.min(w - 2, sx));
            sy = Math.max(2, Math.min(h - 2, sy));

            drawLine(ctx, w / 2, h, (int) sx, (int) sy, Theme.ACCENT);
        }
    }

    /** DrawContext has no line primitive, so step along the line with 1px squares. */
    private static void drawLine(DrawContext ctx, int x0, int y0, int x1, int y1, int color) {
        int dx = x1 - x0, dy = y1 - y0;
        int steps = Math.max(Math.abs(dx), Math.abs(dy));
        if (steps == 0) return;
        for (int i = 0; i <= steps; i++) {
            int x = x0 + dx * i / steps;
            int y = y0 + dy * i / steps;
            ctx.fill(x, y, x + 1, y + 1, color);
        }
    }
}
