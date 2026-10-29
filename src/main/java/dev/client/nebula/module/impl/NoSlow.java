package dev.client.nebula.module.impl;

import dev.client.nebula.module.Module;
import org.lwjgl.glfw.GLFW;

/** Logic lives in ClientPlayerEntityMixin; this class is just the toggle. */
public class NoSlow extends Module {
    public NoSlow() { super("NoSlow", GLFW.GLFW_KEY_N); }
}
