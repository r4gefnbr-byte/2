package dev.client.nebula.module;

import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

import java.util.Comparator;

/** Shared helpers for the combat modules. */
public final class CombatUtil {
    private static final MinecraftClient mc = MinecraftClient.getInstance();

    private CombatUtil() {}

    public static PlayerEntity nearest(double range) {
        if (mc.player == null || mc.world == null) return null;
        return mc.world.getPlayers().stream()
                .filter(p -> p != mc.player && p.isAlive() && !p.isSpectator())
                .filter(p -> mc.player.squaredDistanceTo(p) <= range * range)
                .min(Comparator.comparingDouble(p -> mc.player.squaredDistanceTo(p)))
                .orElse(null);
    }

    /** Vanilla mace: 6 base, +4/block for the first 3 blocks fallen, +2/block for the next 5, +1/block after. */
    public static double maceDamage(double fall) {
        if (fall <= 1.5) return 6.0;
        double bonus = Math.min(fall, 3) * 4 + Math.min(Math.max(fall - 3, 0), 5) * 2 + Math.max(fall - 8, 0);
        return 6.0 + bonus;
    }

    public static int hotbarSlot(Item item) {
        if (mc.player == null) return -1;
        for (int i = 0; i < 9; i++) if (mc.player.getInventory().getStack(i).isOf(item)) return i;
        return -1;
    }

    /**
     * Turns toward a point. Each tick closes `factor` of the remaining angle, capped at
     * maxStep degrees, so it is smooth up close and never snaps.
     */
    public static void rotateToward(Vec3d point, float factor, float maxStep) {
        Vec3d to = point.subtract(mc.player.getEyePos());
        double flat = Math.sqrt(to.x * to.x + to.z * to.z);
        float wantYaw = (float) (MathHelper.atan2(to.z, to.x) * 180.0 / Math.PI) - 90.0f;
        float wantPitch = (float) -(MathHelper.atan2(to.y, flat) * 180.0 / Math.PI);

        float dYaw = MathHelper.wrapDegrees(wantYaw - mc.player.getYaw()) * factor;
        float dPitch = (wantPitch - mc.player.getPitch()) * factor;
        mc.player.setYaw(mc.player.getYaw() + MathHelper.clamp(dYaw, -maxStep, maxStep));
        mc.player.setPitch(MathHelper.clamp(mc.player.getPitch() + MathHelper.clamp(dPitch, -maxStep, maxStep), -90f, 90f));
    }

    /** Angle in degrees between where you look and the direction to a point. */
    public static double angleTo(Vec3d point) {
        Vec3d look = mc.player.getRotationVec(1.0f);
        Vec3d dir = point.subtract(mc.player.getEyePos()).normalize();
        return Math.toDegrees(Math.acos(MathHelper.clamp(look.dotProduct(dir), -1.0, 1.0)));
    }
}
