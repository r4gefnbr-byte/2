package dev.client.nebula.module.impl;

import dev.client.nebula.module.CombatUtil;
import dev.client.nebula.module.Module;
import dev.client.nebula.module.Setting;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Items;
import net.minecraft.util.Hand;
import net.minecraft.util.math.Vec3d;
import org.lwjgl.glfw.GLFW;

/**
 * Elytra dive-bomber. Needs an elytra equipped, a mace and firework rockets in the hotbar.
 *   - Takes off and glides.
 *   - Climbs with fireworks until it is Height blocks above the target.
 *   - Dives at them (gliding down builds fall distance) and smashes once the estimated
 *     damage reaches the Damage setting.
 * Speed is the glide speed (blocks/tick) it tries to hold with fireworks.
 */
public class ElytraMaceAura extends Module {
    private final Setting range    = add("Range", 3.5, 2.0, 6.0, 0.1);
    private final Setting damage   = add("Damage", 20, 6, 100, 2);
    private final Setting speed    = add("Speed", 1.2, 0.5, 2.5, 0.1);
    private final Setting height   = add("Height", 20, 8, 60, 2);
    private final Setting turn     = add("TurnSpeed", 12, 2, 40, 1);   // max degrees per tick
    private final Setting delay    = add("Delay", 4, 0, 20, 1);
    private int attackCooldown, rocketCooldown, ticks;

    public ElytraMaceAura() { super("ElytraMaceAura", GLFW.GLFW_KEY_O); }

    @Override
    public void onTick() {
        if (mc.player == null || mc.interactionManager == null) return;
        if (!mc.player.getEquippedStack(EquipmentSlot.CHEST).isOf(Items.ELYTRA)) return;

        ticks++;
        if (attackCooldown > 0) attackCooldown--;
        if (rocketCooldown > 0) rocketCooldown--;

        PlayerEntity target = CombatUtil.nearest(80);
        if (target == null) { mc.options.jumpKey.setPressed(false); return; }

        // Take off: jump on the ground, then re-press in the air until gliding starts.
        if (!mc.player.isGliding()) {
            boolean press = mc.player.isOnGround() || (ticks % 2 == 0);
            mc.options.jumpKey.setPressed(press);
            return;
        }
        mc.options.jumpKey.setPressed(false);

        Vec3d tp = target.getBoundingBox().getCenter();
        boolean needsHeight = mc.player.getY() < target.getY() + height.get() - 2;
        double horizontalSpeed = mc.player.getVelocity().horizontalLength();
        float maxStep = turn.getF();

        if (needsHeight) {
            // Climb: face the target horizontally but point the nose up.
            Vec3d up = new Vec3d(tp.x, mc.player.getEyeY() + 30, tp.z);
            CombatUtil.rotateToward(up, 0.4f, maxStep);
            if (horizontalSpeed < speed.get()) useFirework();
            return;
        }

        // Dive: aim at them and smash when the estimate is high enough.
        CombatUtil.rotateToward(tp, 0.5f, maxStep);
        if (horizontalSpeed < speed.get() * 0.6 && rocketCooldown == 0 && mc.player.getVelocity().y > -0.3) useFirework();

        if (attackCooldown == 0 && mc.player.squaredDistanceTo(target) <= range.get() * range.get()
                && CombatUtil.maceDamage(mc.player.fallDistance) >= damage.get()) {
            int slot = CombatUtil.hotbarSlot(Items.MACE);
            if (slot < 0) return;
            int prev = mc.player.getInventory().getSelectedSlot();
            mc.player.getInventory().setSelectedSlot(slot);
            mc.interactionManager.attackEntity(mc.player, target);
            mc.player.swingHand(Hand.MAIN_HAND);
            mc.player.getInventory().setSelectedSlot(prev);
            attackCooldown = (int) delay.get();
        }
    }

    private void useFirework() {
        if (rocketCooldown > 0) return;
        if (mc.player.getOffHandStack().isOf(Items.FIREWORK_ROCKET)) {
            mc.interactionManager.interactItem(mc.player, Hand.OFF_HAND);
        } else {
            int slot = CombatUtil.hotbarSlot(Items.FIREWORK_ROCKET);
            if (slot < 0) return;
            int prev = mc.player.getInventory().getSelectedSlot();
            mc.player.getInventory().setSelectedSlot(slot);
            mc.interactionManager.interactItem(mc.player, Hand.MAIN_HAND);
            mc.player.getInventory().setSelectedSlot(prev);
        }
        rocketCooldown = 15;
    }

    @Override
    protected void onDisable() {
        mc.options.jumpKey.setPressed(false);
    }
}
