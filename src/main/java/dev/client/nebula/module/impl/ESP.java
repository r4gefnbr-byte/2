package dev.client.nebula.module.impl;

import dev.client.nebula.module.Module;
import net.minecraft.entity.player.PlayerEntity;
import org.lwjgl.glfw.GLFW;

/** Uses the vanilla glowing outline, set client-side only, so players show through walls. */
public class ESP extends Module {
    public ESP() { super("ESP", GLFW.GLFW_KEY_H); }

    @Override
    public void onTick() {
        if (mc.world == null) return;
        for (PlayerEntity p : mc.world.getPlayers()) {
            if (p != mc.player) p.setGlowing(true);
        }
    }

    @Override
    protected void onDisable() {
        if (mc.world == null) return;
        for (PlayerEntity p : mc.world.getPlayers()) {
            if (p != mc.player) p.setGlowing(false);
        }
    }
}
