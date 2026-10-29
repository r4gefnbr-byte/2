package dev.client.nebula.module;

import dev.client.nebula.gui.ClickGuiScreen;
import dev.client.nebula.module.impl.Aimbot;
import dev.client.nebula.module.impl.AutoTotem;
import dev.client.nebula.module.impl.ESP;
import dev.client.nebula.module.impl.Fly;
import dev.client.nebula.module.impl.AimAssist;
import dev.client.nebula.module.impl.AutoCrystal;
import dev.client.nebula.module.impl.Critical;
import dev.client.nebula.module.impl.MaceAura;
import dev.client.nebula.module.impl.ElytraMaceAura;
import dev.client.nebula.module.impl.SpearAura;
import dev.client.nebula.module.impl.ElytraBounce;
import dev.client.nebula.module.impl.Fog;
import dev.client.nebula.module.impl.Tracers;
import dev.client.nebula.module.impl.Fullbright;
import dev.client.nebula.module.impl.KillAura;
import dev.client.nebula.module.impl.NoSlow;
import dev.client.nebula.module.impl.NoVerticalKB;
import dev.client.nebula.module.impl.Sprint;
import dev.client.nebula.module.impl.Velocity;
import dev.client.nebula.module.impl.Zoom;
import net.minecraft.client.MinecraftClient;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ModuleManager {
    private static final int GUI_KEY = GLFW.GLFW_KEY_RIGHT_SHIFT;

    private final List<Module> modules = new ArrayList<>();
    private final Set<Integer> held = new HashSet<>();

    public void init() {
        modules.add(new Sprint());
        modules.add(new Fullbright());
        modules.add(new Fly());
        modules.add(new KillAura());
        modules.add(new Aimbot());
        modules.add(new Velocity());
        modules.add(new NoVerticalKB());
        modules.add(new NoSlow());
        modules.add(new AutoTotem());
        modules.add(new ESP());
        modules.add(new Fog());
        modules.add(new Tracers());
        modules.add(new ElytraBounce());
        modules.add(new AutoCrystal());
        modules.add(new MaceAura());
        modules.add(new ElytraMaceAura());
        modules.add(new SpearAura());
        modules.add(new Critical());
        modules.add(new AimAssist());
        modules.add(new Zoom());
    }

    public List<Module> all() { return modules; }

    @SuppressWarnings("unchecked")
    public <T extends Module> T get(Class<T> type) {
        for (Module m : modules) if (type.isInstance(m)) return (T) m;
        return null;
    }

    public void tick(MinecraftClient client) {
        if (client.player == null || client.getWindow() == null) return;
        long handle = client.getWindow().getHandle();

        // Keybinds are polled directly; ignored while a screen (chat, inventory) is open.
        if (client.currentScreen == null) {
            if (edge(handle, GUI_KEY)) client.setScreen(new ClickGuiScreen());
            for (Module m : modules) {
                if (m.getKey() >= 0 && edge(handle, m.getKey())) m.toggle();
            }
        }

        for (Module m : modules) {
            if (m.isEnabled()) m.onTick();
        }
    }

    /** True once per key press. */
    private boolean edge(long handle, int key) {
        boolean down = GLFW.glfwGetKey(handle, key) == GLFW.GLFW_PRESS;
        if (down) return held.add(key);
        held.remove(key);
        return false;
    }
}
