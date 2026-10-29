package dev.client.nebula.module.impl;

import dev.client.nebula.module.Module;
import dev.client.nebula.module.Setting;
import org.lwjgl.glfw.GLFW;

/**
 * Two modes, picked with the Mode setting:
 *   0 = NoFog      : pushes the fog distances out so nothing is hazed.
 *   1 = Fog of War : pulls the fog in to Radius blocks so everything beyond it fades out.
 * The actual work happens in FogRendererMixin, which calls adjust().
 */
public class Fog extends Module {
    public static final int ENV_START = 0, ENV_END = 1, RD_START = 2, RD_END = 3, SKY_END = 4, CLOUD_END = 5;

    private final Setting mode = add("Mode", 0, 0, 1, 1);
    private final Setting radius = add("Radius", 24, 8, 128, 4);

    public Fog() { super("Fog", GLFW.GLFW_KEY_J); }

    /** Called by the mixin for each fog distance; returns the value to use. */
    public float adjust(float original, int which) {
        if (mode.get() < 0.5) { // NoFog
            return (which == ENV_START || which == RD_START) ? 900_000f : 1_000_000f;
        }
        float r = radius.getF(); // Fog of War
        switch (which) {
            case ENV_START: case RD_START: return r * 0.4f;
            case ENV_END: case RD_END:     return r;
            default:                        return Math.min(original, r * 1.5f);
        }
    }
}
