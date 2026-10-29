package dev.client.nebula.module.impl;

import dev.client.nebula.module.Module;
import dev.client.nebula.module.Setting;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.decoration.EndCrystalEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Items;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import org.lwjgl.glfw.GLFW;

import java.util.Comparator;

/**
 * Crystal PvP with an optional launch combo (Combo setting = 1):
 *   1. Target on the ground: crystals are allowed at low damage, so the explosion launches them.
 *   2. Target in the air: if no crystal base is available near them, an obsidian is placed
 *      beneath them (against any solid neighbor block), then the normal logic crystals it.
 * One action per tick-cooldown. Everything goes through the normal interaction manager.
 * Damage is a simplified explosion estimate (no armor or line of sight).
 */
public class AutoCrystal extends Module {
    private final Setting range   = add("Range", 4.5, 2.0, 6.0, 0.1);
    private final Setting minDmg  = add("MinDamage", 4.0, 1.0, 20.0, 0.5);
    private final Setting maxSelf = add("MaxSelf", 6.0, 1.0, 20.0, 0.5);
    private final Setting delay   = add("Delay", 2, 0, 10, 1);
    private final Setting combo   = add("Combo", 1, 0, 1, 1);
    private int cooldown;

    public AutoCrystal() { super("AutoCrystal", GLFW.GLFW_KEY_X); }

    @Override
    public void onTick() {
        if (mc.player == null || mc.world == null || mc.interactionManager == null) return;
        if (cooldown > 0) { cooldown--; return; }

        PlayerEntity target = mc.world.getPlayers().stream()
                .filter(p -> p != mc.player && p.isAlive())
                .filter(p -> mc.player.squaredDistanceTo(p) <= 12 * 12)
                .min(Comparator.comparingDouble(p -> mc.player.squaredDistanceTo(p)))
                .orElse(null);
        if (target == null) return;

        boolean useCombo = combo.get() > 0.5;
        boolean airborne = isAirborne(target);
        // On the ground with Combo on, accept low-damage crystals: the point is the launch.
        double effMin = (useCombo && !airborne) ? 1.0 : minDmg.get();

        if (tryBreak(target, effMin)) { arm(); return; }

        Hand crystalHand = mc.player.getMainHandStack().isOf(Items.END_CRYSTAL) ? Hand.MAIN_HAND
                         : mc.player.getOffHandStack().isOf(Items.END_CRYSTAL) ? Hand.OFF_HAND : null;
        if (crystalHand != null && tryPlaceCrystal(target, effMin, crystalHand)) { arm(); return; }

        if (useCombo && airborne && tryPlaceObsidian(target)) arm();
    }

    private void arm() { cooldown = (int) delay.get(); }

    private boolean isAirborne(PlayerEntity t) {
        if (t.isOnGround() || t.isTouchingWater() || t.isClimbing()) return false;
        BlockPos feet = t.getBlockPos();
        return mc.world.getBlockState(feet.down()).isAir() && mc.world.getBlockState(feet.down(2)).isAir();
    }

    // ---- breaking ---------------------------------------------------------------------

    private boolean tryBreak(PlayerEntity target, double effMin) {
        double r = range.get();
        EndCrystalEntity best = null;
        double bestScore = -1;
        for (EndCrystalEntity c : mc.world.getEntitiesByClass(EndCrystalEntity.class,
                mc.player.getBoundingBox().expand(r), Entity::isAlive)) {
            if (mc.player.squaredDistanceTo(c) > r * r) continue;
            Vec3d at = c.getPos();
            double dT = damage(at, target), dS = damage(at, mc.player);
            if (dT >= effMin && safe(dS) && dT > bestScore) { bestScore = dT; best = c; }
        }
        if (best == null) return false;
        mc.interactionManager.attackEntity(mc.player, best);
        mc.player.swingHand(Hand.MAIN_HAND);
        return true;
    }

    // ---- placing crystals -------------------------------------------------------------

    private boolean tryPlaceCrystal(PlayerEntity target, double effMin, Hand hand) {
        double r = range.get();
        BlockPos origin = target.getBlockPos();
        BlockPos bestPos = null;
        double bestScore = -1;
        for (int dx = -3; dx <= 3; dx++) for (int dy = -4; dy <= 1; dy++) for (int dz = -3; dz <= 3; dz++) {
            BlockPos base = origin.add(dx, dy, dz);
            if (!canPlaceCrystalOn(base)) continue;
            Vec3d crystal = new Vec3d(base.getX() + 0.5, base.getY() + 1, base.getZ() + 0.5);
            if (mc.player.getEyePos().squaredDistanceTo(crystal) > r * r) continue;
            double dT = damage(crystal, target), dS = damage(crystal, mc.player);
            if (dT >= effMin && safe(dS) && dT > bestScore) { bestScore = dT; bestPos = base; }
        }
        if (bestPos == null) return false;
        Vec3d hit = new Vec3d(bestPos.getX() + 0.5, bestPos.getY() + 1, bestPos.getZ() + 0.5);
        mc.interactionManager.interactBlock(mc.player, hand, new BlockHitResult(hit, Direction.UP, bestPos, false));
        mc.player.swingHand(hand);
        return true;
    }

    private boolean canPlaceCrystalOn(BlockPos base) {
        Block b = mc.world.getBlockState(base).getBlock();
        if (b != Blocks.OBSIDIAN && b != Blocks.BEDROCK) return false;
        if (!mc.world.getBlockState(base.up()).isAir() || !mc.world.getBlockState(base.up(2)).isAir()) return false;
        Box box = new Box(base.getX(), base.getY() + 1, base.getZ(), base.getX() + 1, base.getY() + 3, base.getZ() + 1);
        return mc.world.getOtherEntities(null, box).isEmpty();
    }

    // ---- placing obsidian under an airborne target ------------------------------------

    private boolean tryPlaceObsidian(PlayerEntity target) {
        int slot = findHotbar(Items.OBSIDIAN);
        if (slot < 0) return false;

        double r = range.get();
        BlockPos origin = target.getBlockPos();
        BlockPos bestPlace = null, bestNeighbor = null;
        Direction bestFace = null;
        double bestDist = Double.MAX_VALUE;

        for (int dx = -1; dx <= 1; dx++) for (int dy = -4; dy <= -1; dy++) for (int dz = -1; dz <= 1; dz++) {
            BlockPos p = origin.add(dx, dy, dz);
            if (!mc.world.getBlockState(p).isAir()) continue;
            if (!mc.world.getBlockState(p.up()).isAir() || !mc.world.getBlockState(p.up(2)).isAir()) continue;
            Vec3d center = Vec3d.ofCenter(p);
            if (mc.player.getEyePos().squaredDistanceTo(center) > r * r) continue;

            for (Direction dir : Direction.values()) {
                BlockPos n = p.offset(dir);
                if (mc.world.getBlockState(n).isAir()) continue; // need something to click against
                double d = center.squaredDistanceTo(target.getPos());
                if (d < bestDist) {
                    bestDist = d; bestPlace = p; bestNeighbor = n; bestFace = dir.getOpposite();
                }
            }
        }
        if (bestPlace == null) return false;

        // Click the neighbor's face that points at the empty spot.
        Vec3d hit = Vec3d.ofCenter(bestNeighbor).add(
                bestFace.getOffsetX() * 0.5, bestFace.getOffsetY() * 0.5, bestFace.getOffsetZ() * 0.5);

        int prev = mc.player.getInventory().getSelectedSlot();
        mc.player.getInventory().setSelectedSlot(slot);
        mc.interactionManager.interactBlock(mc.player, Hand.MAIN_HAND, new BlockHitResult(hit, bestFace, bestNeighbor, false));
        mc.player.swingHand(Hand.MAIN_HAND);
        mc.player.getInventory().setSelectedSlot(prev); // interactBlock already synced the slot to the server
        return true;
    }

    private int findHotbar(net.minecraft.item.Item item) {
        for (int i = 0; i < 9; i++) {
            if (mc.player.getInventory().getStack(i).isOf(item)) return i;
        }
        return -1;
    }

    // ---- helpers ----------------------------------------------------------------------

    private boolean safe(double selfDamage) {
        return selfDamage <= maxSelf.get() && selfDamage < mc.player.getHealth() - 1;
    }

    /** Vanilla explosion falloff for power 6, assuming full exposure and no armor. */
    private static double damage(Vec3d explosion, Entity e) {
        double dist = Math.sqrt(e.squaredDistanceTo(explosion)) / 12.0;
        if (dist > 1.0) return 0;
        double impact = 1.0 - dist;
        return ((impact * impact + impact) / 2.0) * 7.0 * 12.0 + 1.0;
    }
}
