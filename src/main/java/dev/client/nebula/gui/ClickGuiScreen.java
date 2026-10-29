package dev.client.nebula.gui;

import dev.client.nebula.NebulaClient;
import dev.client.nebula.module.Module;
import dev.client.nebula.module.Setting;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.util.InputUtil;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

import java.util.HashSet;
import java.util.Set;

/**
 * Single-panel click GUI.
 *   Left click  : toggle module
 *   Right click : expand/collapse settings
 *   Middle click: rebind (press a key; Escape clears)
 *   Settings    : click left half of the row to decrease, right half to increase
 */
public class ClickGuiScreen extends Screen {
    private static final int W = 150, ROW = 16, X = 20, Y = 20;

    private final Set<Module> expanded = new HashSet<>();
    private Module listening;

    public ClickGuiScreen() { super(Text.literal("Nebula")); }

    @Override
    public void render(DrawContext ctx, int mouseX, int mouseY, float delta) {
        int y = Y;
        ctx.fill(X, y, X + W, y + ROW + 2, Theme.BG);
        ctx.fill(X, y, X + W, y + 2, Theme.ACCENT);
        ctx.drawTextWithShadow(textRenderer, "Nebula", X + 6, y + 6, Theme.ACCENT);
        y += ROW + 2;

        for (Module m : NebulaClient.MODULES.all()) {
            boolean hover = mouseX >= X && mouseX < X + W && mouseY >= y && mouseY < y + ROW;
            ctx.fill(X, y, X + W, y + ROW, m.isEnabled() ? Theme.BG_ON : hover ? Theme.BG_ALT : Theme.BG);
            ctx.drawTextWithShadow(textRenderer, m.getName(), X + 6, y + 4, m.isEnabled() ? Theme.ACCENT : Theme.TEXT);

            String bind = listening == m ? "..." : m.getKey() < 0 ? "-" : keyName(m.getKey());
            ctx.drawTextWithShadow(textRenderer, bind, X + W - 6 - textRenderer.getWidth(bind), y + 4, Theme.TEXT_DIM);
            y += ROW;

            if (expanded.contains(m)) {
                for (Setting s : m.getSettings()) {
                    ctx.fill(X, y, X + W, y + ROW, Theme.BG_ALT);
                    ctx.drawTextWithShadow(textRenderer, s.getName(), X + 12, y + 4, Theme.TEXT_DIM);
                    String v = "< " + String.format("%.2f", s.get()) + " >";
                    ctx.drawTextWithShadow(textRenderer, v, X + W - 6 - textRenderer.getWidth(v), y + 4, Theme.TEXT);
                    y += ROW;
                }
            }
        }
    }

    @Override
    public boolean mouseClicked(double mx, double my, int button) {
        int y = Y + ROW + 2;
        for (Module m : NebulaClient.MODULES.all()) {
            if (mx >= X && mx < X + W && my >= y && my < y + ROW) {
                if (button == 0) m.toggle();
                else if (button == 1) { if (!expanded.remove(m)) expanded.add(m); }
                else if (button == 2) listening = m;
                return true;
            }
            y += ROW;
            if (expanded.contains(m)) {
                for (Setting s : m.getSettings()) {
                    if (mx >= X && mx < X + W && my >= y && my < y + ROW) {
                        s.nudge(mx < X + W / 2.0 ? -1 : 1);
                        return true;
                    }
                    y += ROW;
                }
            }
        }
        return super.mouseClicked(mx, my, button);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (listening != null) {
            listening.setKey(keyCode == GLFW.GLFW_KEY_ESCAPE ? -1 : keyCode);
            listening = null;
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    private static String keyName(int key) {
        return InputUtil.fromKeyCode(key, 0).getLocalizedText().getString();
    }

    @Override
    public void removed() {
        dev.client.nebula.ConfigManager.save();
    }

    @Override
    public boolean shouldPause() { return false; }
}
