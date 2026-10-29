package dev.client.nebula.module.impl;

import dev.client.nebula.module.CombatUtil;
import dev.client.nebula.module.Module;
import dev.client.nebula.module.Setting;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.Vec3d;
import org.lwjgl.glfw.GLFW;

import java.util.Comparator;

/**
 * Gentle aim assist. It only considers players inside a cone around your crosshair, pulls a
 * fraction of the remaining angle each tick, and caps the turn per tick, so your own mouse
 * movement always stays in charge. Defaults are light; raise Strength and MaxTurn for more help.
 */
public class AimAssist extends Module {
    private final Setting range     = add("Range", 4.5, 2.0, 8.0, 0.1);
    private final Setting fov       = add("FOV", 70, 10, 180, 5);        // total cone angle
    private final Setting strength  = add("Strength", 0.18, 0.05, 0.6, 0.01);
    private final Setting maxTurn   = add("MaxTurn", 4.0, 0.5, 15, 0.5); // degrees per tick
    private final Setting onlyClick = add("OnlyWhileAttacking", 1, 0, 1, 1);

    public AimAssist() { super("AimAssist", GLFW.GLFW_KEY_Z); }

    @Override
    public void onTick() {
        if (mc.player == null || mc.world == null || mc.currentScreen != null) return;
        if (onlyClick.get() > 0.5 && !mc.options.attackKey.isPressed()) return;

        double r = range.get();
        PlayerEntity target = mc.world.getPlayers().stream()
                .filter(p -> p != mc.player && p.isAlive() && !p.isSpectator())
                .filter(p -> mc.player.squaredDistanceTo(p) <= r * r)
                .filter(p -> CombatUtil.angleTo(aimPoint(p)) <= fov.get() / 2.0)
                .min(Comparator.comparingDouble(p -> CombatUtil.angleTo(aimPoint(p))))
                .orElse(null);
        if (target == null) return;

        CombatUtil.rotateToward(aimPoint(target), strength.getF(), maxTurn.getF());
    }

    private static Vec3d aimPoint(PlayerEntity p) {
        return p.getPos().add(0, p.getHeight() * 0.65, 0); // upper torso
    }
}
