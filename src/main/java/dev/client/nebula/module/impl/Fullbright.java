package dev.client.nebula.module.impl;

import dev.client.nebula.module.Module;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import org.lwjgl.glfw.GLFW;

public class Fullbright extends Module {
    public Fullbright() { super("Fullbright", GLFW.GLFW_KEY_G); }

    @Override
    public void onTick() {
        if (mc.player == null) return;
        // Client-side night vision; refreshed each tick so it never flickers off.
        mc.player.addStatusEffect(new StatusEffectInstance(StatusEffects.NIGHT_VISION, 400, 0, false, false, false));
    }

    @Override
    protected void onDisable() {
        if (mc.player != null) mc.player.removeStatusEffect(StatusEffects.NIGHT_VISION);
    }
}
