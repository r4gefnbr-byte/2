package dev.client.nebula.module.impl;

import dev.client.nebula.module.Module;
import dev.client.nebula.module.Setting;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import org.lwjgl.glfw.GLFW;

import java.util.Comparator;

public class Aimbot extends Module {
    private final Setting range = add("Range", 4.5, 2.0, 8.0, 0.1);
    private final Setting smoothing = add("Smoothing", 0.35, 0.05, 1.0, 0.05);

    public Aimbot() { super("Aimbot", GLFW.GLFW_KEY_B); }

    @Override
    public void onTick() {
        if (mc.player == null || mc.world == null) return;
        double r = range.get();
        mc.world.getPlayers().stream()
                .filter(p -> p != mc.player && p.isAlive())
                .filter(p -> mc.player.squaredDistanceTo(p) <= r * r)
                .min(Comparator.comparingDouble(p -> mc.player.squaredDistanceTo(p)))
                .ifPresent(this::aimAt);
    }

    private void aimAt(PlayerEntity target) {
        Vec3d to = target.getBoundingBox().getCenter().subtract(mc.player.getEyePos());
        double flat = Math.sqrt(to.x * to.x + to.z * to.z);
        float wantYaw = (float) (MathHelper.atan2(to.z, to.x) * 180.0 / Math.PI) - 90.0f;
        float wantPitch = (float) -(MathHelper.atan2(to.y, flat) * 180.0 / Math.PI);

        float s = smoothing.getF();
        float yaw = mc.player.getYaw();
        float pitch = mc.player.getPitch();
        mc.player.setYaw(yaw + MathHelper.wrapDegrees(wantYaw - yaw) * s);
        mc.player.setPitch(MathHelper.clamp(pitch + (wantPitch - pitch) * s, -90f, 90f));
    }
}
