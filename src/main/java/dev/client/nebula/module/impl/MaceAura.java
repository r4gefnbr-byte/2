package dev.client.nebula.module.impl;

import dev.client.nebula.module.CombatUtil;
import dev.client.nebula.module.Module;
import dev.client.nebula.module.Setting;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Items;
import net.minecraft.util.Hand;
import org.lwjgl.glfw.GLFW;

/**
 * Smash-attacks with a mace when you are falling onto someone. It waits until the estimated
 * smash damage reaches the Damage setting, so you choose how big each hit is. The mace is
 * pulled from your hotbar for the swing and your previous slot is restored.
 */
public class MaceAura extends Module {
    private final Setting range  = add("Range", 3.5, 2.0, 6.0, 0.1);
    private final Setting damage = add("Damage", 20, 6, 100, 2);   // minimum estimated damage per hit
    private final Setting delay  = add("Delay", 4, 0, 20, 1);      // ticks between hits
    private int cooldown;

    public MaceAura() { super("MaceAura", GLFW.GLFW_KEY_P); }

    @Override
    public void onTick() {
        if (mc.player == null || mc.interactionManager == null) return;
        if (cooldown > 0) { cooldown--; return; }
        if (mc.player.isOnGround() || mc.player.isGliding()) return;

        PlayerEntity target = CombatUtil.nearest(range.get());
        if (target == null) return;

        double est = CombatUtil.maceDamage(mc.player.fallDistance);
        if (est < damage.get()) return;

        int slot = CombatUtil.hotbarSlot(Items.MACE);
        if (slot < 0) return;

        int prev = mc.player.getInventory().getSelectedSlot();
        mc.player.getInventory().setSelectedSlot(slot);
        mc.interactionManager.attackEntity(mc.player, target);
        mc.player.swingHand(Hand.MAIN_HAND);
        mc.player.getInventory().setSelectedSlot(prev);
        cooldown = (int) delay.get();
    }
}
