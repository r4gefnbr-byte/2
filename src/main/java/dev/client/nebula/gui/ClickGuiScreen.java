package dev.client.nebula.gui;

import dev.client.nebula.NebulaClient;
import dev.client.nebula.module.Module;
import dev.client.nebula.module.Setting;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

import java.util.HashSet;
import java.util.Set;

/**
 * Single-panel click GUI. Input is read straight from GLFW each frame instead of overriding
 * the screen's click/key methods, because those signatures changed in 1.21.9+.
 *   Left click  : toggle module
 *   Right click : expand/collapse settings
 *   Middle click: rebind (press a key; Backspace or Delete clears)
 *   Settings    : click left half of the row to decrease, right half to increase
 */
public class ClickGuiScreen extends Screen {
    private static final int W = 150, ROW = 16, X = 20, Y = 20;

    private final Set<Module> expanded = new HashSet<>();
    private final boolean[] mouseWasDown = new boolean[3];
    private final Set<Integer> keysDown = new HashSet<>();
    private Module listening;
    private boolean ignoreFirstFrame = true;

    public ClickGuiScreen() { super(Text.literal("Nebula")); }

    @Override
    public void render(DrawContext ctx, int mouseX, int mouseY, float delta) {
        pollInput(mouseX, mouseY);

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

    private void pollInput(int mouseX, int mouseY) {
        long handle = client.getWindow().getHandle();

        // Skip the first frame so the key that opened the GUI isn't read as input.
        if (ignoreFirstFrame) {
            ignoreFirstFrame = false;
            for (int b = 0; b < 3; b++) mouseWasDown[b] = GLFW.glfwGetMouseButton(handle, b) == GLFW.GLFW_PRESS;
            return;
        }

        // Rebinding: take the next key press.
        if (listening != null) {
            for (int key = GLFW.GLFW_KEY_SPACE; key <= GLFW.GLFW_KEY_LAST; key++) {
                boolean down = GLFW.glfwGetKey(handle, key) == GLFW.GLFW_PRESS;
                if (down && keysDown.add(key)) {
                    if (key == GLFW.GLFW_KEY_ESCAPE) { listening = null; return; }
                    listening.setKey(key == GLFW.GLFW_KEY_BACKSPACE || key == GLFW.GLFW_KEY_DELETE ? -1 : key);
                    listening = null;
                    return;
                } else if (!down) {
                    keysDown.remove(key);
                }
            }
            return;
        }
        keysDown.clear();

        for (int b = 0; b < 3; b++) {
            boolean down = GLFW.glfwGetMouseButton(handle, b) == GLFW.GLFW_PRESS;
            if (down && !mouseWasDown[b]) click(mouseX, mouseY, b);
            mouseWasDown[b] = down;
        }
    }

    private void click(int mx, int my, int button) {
        int y = Y + ROW + 2;
        for (Module m : NebulaClient.MODULES.all()) {
            if (mx >= X && mx < X + W && my >= y && my < y + ROW) {
                if (button == 0) m.toggle();
                else if (button == 1) { if (!expanded.remove(m)) expanded.add(m); }
                else if (button == 2) listening = m;
                return;
            }
            y += ROW;
            if (expanded.contains(m)) {
                for (Setting s : m.getSettings()) {
                    if (mx >= X && mx < X + W && my >= y && my < y + ROW) {
                        s.nudge(mx < X + W / 2.0 ? -1 : 1);
                        return;
                    }
                    y += ROW;
                }
            }
        }
    }

    private static String keyName(int key) {
        switch (key) {
            case GLFW.GLFW_KEY_RIGHT_SHIFT: return "RSHIFT";
            case GLFW.GLFW_KEY_LEFT_SHIFT:  return "LSHIFT";
            case GLFW.GLFW_KEY_RIGHT_CONTROL: return "RCTRL";
            case GLFW.GLFW_KEY_LEFT_CONTROL:  return "LCTRL";
            case GLFW.GLFW_KEY_RIGHT_ALT:   return "RALT";
            case GLFW.GLFW_KEY_LEFT_ALT:    return "LALT";
            case GLFW.GLFW_KEY_SPACE:       return "SPACE";
            case GLFW.GLFW_KEY_TAB:         return "TAB";
            case GLFW.GLFW_KEY_ENTER:       return "ENTER";
            default:
                String n = GLFW.glfwGetKeyName(key, 0);
                return n != null ? n.toUpperCase() : "KEY" + key;
        }
    }

    @Override
    public void removed() {
        dev.client.nebula.ConfigManager.save();
    }

    @Override
    public boolean shouldPause() { return false; }
}
