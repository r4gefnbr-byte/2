package dev.client.nebula.module.impl;

import dev.client.nebula.module.Module;
import org.lwjgl.glfw.GLFW;

/**
 * Keeps horizontal knockback but throws away the vertical part. Logic lives in
 * ClientPlayNetworkHandlerMixin. If the Velocity module is also on, it wins (cancels everything).
 */
public class NoVerticalKB extends Module {
    public NoVerticalKB() { super("NoVerticalKB", GLFW.GLFW_KEY_M); }
}
