package dev.client.nebula.module.impl;

import dev.client.nebula.module.Module;
import org.lwjgl.glfw.GLFW;

public class Sprint extends Module {
    public Sprint() { super("Sprint", GLFW.GLFW_KEY_R); }

    @Override
    public void onTick() {
        if (mc.player == null) return;
        if (mc.player.input.hasForwardMovement() && !mc.player.isSneaking()
                && !mc.player.horizontalCollision) {
            mc.player.setSprinting(true);
        }
    }
}
