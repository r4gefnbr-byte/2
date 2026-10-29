package dev.client.nebula.hud;

import dev.client.nebula.NebulaClient;
import dev.client.nebula.gui.Theme;
import dev.client.nebula.module.Module;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Deque;
import java.util.List;

public class Hud {
    private final Deque<Long> clicks = new ArrayDeque<>();
    private boolean leftWasDown;
    private final SpotifyHud spotify = new SpotifyHud();

    public void init() { spotify.start(); }

    /** Called every client tick to sample mouse state for the CPS counter. */
    public void tick(MinecraftClient mc) {
        if (mc.getWindow() == null) return;
        boolean down = GLFW.glfwGetMouseButton(mc.getWindow().getHandle(), GLFW.GLFW_MOUSE_BUTTON_LEFT) == GLFW.GLFW_PRESS;
        if (down && !leftWasDown) clicks.add(System.currentTimeMillis());
        leftWasDown = down;
    }

    private int cps() {
        long cutoff = System.currentTimeMillis() - 1000;
        while (!clicks.isEmpty() && clicks.peekFirst() < cutoff) clicks.pollFirst();
        return clicks.size();
    }

    public void render(DrawContext ctx) {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.options.hudHidden) return;

        dev.client.nebula.module.impl.Tracers tracers = NebulaClient.MODULES.get(dev.client.nebula.module.impl.Tracers.class);
        if (tracers != null && tracers.isEnabled()) tracers.render(ctx);

        // Watermark pill
        String mark = NebulaClient.NAME + "  " + mc.getCurrentFps() + " fps  " + cps() + " cps";
        int mw = mc.textRenderer.getWidth(mark) + 12;
        ctx.fill(4, 4, 4 + mw, 18, Theme.BG);
        ctx.fill(4, 4, 4 + mw, 5, Theme.ACCENT);
        ctx.drawTextWithShadow(mc.textRenderer, mark, 10, 8, Theme.TEXT);

        // Module list, longest first, accent bar on the right edge
        int sw = ctx.getScaledWindowWidth();
        List<Module> active = new ArrayList<>();
        for (Module m : NebulaClient.MODULES.all()) if (m.isEnabled()) active.add(m);
        active.sort(Comparator.comparingInt((Module m) -> -mc.textRenderer.getWidth(m.getName())));
        int y = 4;
        for (Module m : active) {
            int tw = mc.textRenderer.getWidth(m.getName());
            ctx.fill(sw - tw - 10, y, sw, y + 11, Theme.BG);
            ctx.fill(sw - 2, y, sw, y + 11, Theme.ACCENT);
            ctx.drawTextWithShadow(mc.textRenderer, m.getName(), sw - tw - 6, y + 2, Theme.TEXT);
            y += 11;
        }

        renderKeystrokes(ctx, mc, 8, ctx.getScaledWindowHeight() - 62);
        spotify.render(ctx, sw - 136, ctx.getScaledWindowHeight() - 40);
    }

    private void renderKeystrokes(DrawContext ctx, MinecraftClient mc, int x, int y) {
        key(ctx, mc, "W", mc.options.forwardKey.isPressed(), x + 19, y);
        key(ctx, mc, "A", mc.options.leftKey.isPressed(), x, y + 19);
        key(ctx, mc, "S", mc.options.backKey.isPressed(), x + 19, y + 19);
        key(ctx, mc, "D", mc.options.rightKey.isPressed(), x + 38, y + 19);
    }

    private void key(DrawContext ctx, MinecraftClient mc, String label, boolean down, int x, int y) {
        ctx.fill(x, y, x + 17, y + 17, down ? Theme.ACCENT_2 : Theme.BG);
        ctx.drawCenteredTextWithShadow(mc.textRenderer, label, x + 9, y + 5, Theme.TEXT);
    }
}
