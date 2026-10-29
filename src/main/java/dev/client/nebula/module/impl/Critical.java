package dev.client.nebula.module.impl;

import dev.client.nebula.NebulaClient;
import dev.client.nebula.module.CombatUtil;
import dev.client.nebula.module.Module;
import dev.client.nebula.module.Setting;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.effect.StatusEffects;
import org.lwjgl.glfw.GLFW;

/**
 * Crits through normal movement, no packet tricks: when a player is in range and you are on
 * the ground it jumps for you, and KillAura then holds its swing until you are falling,
 * which is when vanilla gives the critical hit.
 */
public class Critical extends Module {
    private final Setting range = add("Range", 4.0, 2.0, 6.0, 0.1);
    private boolean jumping;

    public Critical() { super("Critical", GLFW.GLFW_KEY_I); }

    @Override
    public void onTick() {
        if (mc.player == null) return;
        boolean want = mc.player.isOnGround() && !mc.player.isSneaking()
                && CombatUtil.nearest(range.get()) != null;
        if (want) {
            mc.options.jumpKey.setPressed(true);
            jumping = true;
        } else if (jumping) {
            mc.options.jumpKey.setPressed(false);
            jumping = false;
        }
    }

    @Override
    protected void onDisable() {
        if (jumping) mc.options.jumpKey.setPressed(false);
        jumping = false;
    }

    /** True when an attack right now would be a critical hit (vanilla conditions). */
    public static boolean canCrit(ClientPlayerEntity p) {
        return p.fallDistance > 0 && !p.isOnGround() && !p.isClimbing() && !p.isTouchingWater()
                && !p.hasStatusEffect(StatusEffects.BLINDNESS) && !p.hasVehicle() && !p.isGliding();
    }

    /** Combat modules call this: always true when Critical is off, otherwise only while crit-ready. */
    public static boolean allowsAttack() {
        Critical c = NebulaClient.MODULES.get(Critical.class);
        MinecraftClient mc = MinecraftClient.getInstance();
        return c == null || !c.isEnabled() || mc.player == null || canCrit(mc.player);
    }
}
