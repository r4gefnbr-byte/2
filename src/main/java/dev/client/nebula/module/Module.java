package dev.client.nebula.module;

import net.minecraft.client.MinecraftClient;

import java.util.ArrayList;
import java.util.List;

public abstract class Module {
    private final String name;
    private int key; // GLFW key code, -1 for none
    private boolean enabled;
    private final List<Setting> settings = new ArrayList<>();

    protected final MinecraftClient mc = MinecraftClient.getInstance();

    protected Module(String name, int defaultKey) {
        this.name = name;
        this.key = defaultKey;
    }

    protected Setting add(String name, double def, double min, double max, double step) {
        Setting s = new Setting(name, def, min, max, step);
        settings.add(s);
        return s;
    }

    public String getName() { return name; }
    public int getKey() { return key; }
    public void setKey(int key) { this.key = key; }
    public boolean isEnabled() { return enabled; }
    public List<Setting> getSettings() { return settings; }

    public void toggle() {
        enabled = !enabled;
        if (enabled) onEnable(); else onDisable();
    }

    protected void onEnable() {}
    protected void onDisable() {}
    public void onTick() {}
}
