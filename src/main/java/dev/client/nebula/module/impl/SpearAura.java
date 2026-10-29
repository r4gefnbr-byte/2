package dev.client.nebula.module.impl;

import dev.client.nebula.module.CombatUtil;
import dev.client.nebula.module.Module;
import dev.client.nebula.module.Setting;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.registry.Registries;
import net.minecraft.util.Hand;
import org.lwjgl.glfw.GLFW;

/**
 * Jab attacks with whatever spear you are holding. The held item is matched by its registry
 * name (anything ending in "_spear"), so this doesn't depend on exact item constants.
 * It does not do the charged lunge, only the normal attack.
 */
public class SpearAura extends Module {
    private final Setting range = add("Range", 4.0, 2.0, 6.0, 0.1);
    private final Setting delay = add("Delay", 0, 0, 20, 1);   // extra ticks on top of the attack cooldown
    private int cooldown;

    public SpearAura() { super("SpearAura", GLFW.GLFW_KEY_L); }

    @Override
    public void onTick() {
        if (mc.player == null || mc.interactionManager == null) return;
        if (cooldown > 0) { cooldown--; return; }

        String id = Registries.ITEM.getId(mc.player.getMainHandStack().getItem()).getPath();
        if (!id.endsWith("_spear")) return;
        if (mc.player.getAttackCooldownProgress(0.5f) < 1.0f) return;

        PlayerEntity target = CombatUtil.nearest(range.get());
        if (target == null) return;

        mc.interactionManager.attackEntity(mc.player, target);
        mc.player.swingHand(Hand.MAIN_HAND);
        cooldown = (int) delay.get();
    }
}
