package dev.client.nebula.module.impl;

import dev.client.nebula.module.Module;
import org.lwjgl.glfw.GLFW;

public class Zoom extends Module {
    private int previousFov = 70;

    public Zoom() { super("Zoom", GLFW.GLFW_KEY_C); }

    @Override
    protected void onEnable() {
        previousFov = mc.options.getFov().getValue();
        mc.options.getFov().setValue(25);
    }

    @Override
    protected void onDisable() {
        mc.options.getFov().setValue(previousFov);
    }
}
