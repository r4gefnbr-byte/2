package dev.client.nebula.module.impl;

import dev.client.nebula.module.Module;
import dev.client.nebula.module.Setting;
import net.minecraft.entity.Entity;
import net.minecraft.util.Hand;
import org.lwjgl.glfw.GLFW;

import java.util.Comparator;

public class KillAura extends Module {
    private final Setting range = add("Range", 3.8, 2.0, 6.0, 0.1);

    public KillAura() { super("KillAura", GLFW.GLFW_KEY_K); }

    @Override
    public void onTick() {
        if (mc.player == null || mc.world == null || mc.interactionManager == null) return;
        // Respect vanilla attack cooldown so hits land at full damage.
        if (mc.player.getAttackCooldownProgress(0.5f) < 1.0f) return;
        if (!Critical.allowsAttack()) return; // wait for the falling part of a jump when Critical is on

        double r = range.get();
        mc.world.getPlayers().stream()
                .filter(p -> p != mc.player && p.isAlive())
                .filter(p -> mc.player.squaredDistanceTo(p) <= r * r)
                .min(Comparator.comparingDouble(p -> mc.player.squaredDistanceTo(p)))
                .ifPresent(p -> {
                    mc.interactionManager.attackEntity(mc.player, (Entity) p);
                    mc.player.swingHand(Hand.MAIN_HAND);
                });
    }
}
