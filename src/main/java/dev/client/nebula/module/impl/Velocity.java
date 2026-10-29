package dev.client.nebula.module.impl;

import dev.client.nebula.module.Module;
import org.lwjgl.glfw.GLFW;

/** Logic lives in ClientPlayNetworkHandlerMixin; this class is just the toggle. */
public class Velocity extends Module {
    public Velocity() { super("Velocity", GLFW.GLFW_KEY_V); }
}
