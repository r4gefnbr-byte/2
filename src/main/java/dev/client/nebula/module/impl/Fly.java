package dev.client.nebula.module.impl;

import dev.client.nebula.module.Module;
import dev.client.nebula.module.Setting;
import org.lwjgl.glfw.GLFW;

public class Fly extends Module {
    private final Setting speed = add("Speed", 0.08, 0.02, 0.5, 0.01);

    public Fly() { super("Fly", GLFW.GLFW_KEY_F); }

    @Override
    public void onTick() {
        if (mc.player == null) return;
        mc.player.getAbilities().allowFlying = true;
        mc.player.getAbilities().flying = true;
        mc.player.getAbilities().setFlySpeed(speed.getF());
    }

    @Override
    protected void onDisable() {
        if (mc.player == null) return;
        mc.player.getAbilities().setFlySpeed(0.05f);
        if (!mc.player.isCreative() && !mc.player.isSpectator()) {
            mc.player.getAbilities().flying = false;
            mc.player.getAbilities().allowFlying = false;
        }
    }
}
