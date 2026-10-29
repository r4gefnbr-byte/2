package dev.client.nebula.module.impl;

import dev.client.nebula.module.Module;
import dev.client.nebula.module.Setting;
import net.minecraft.item.Items;
import net.minecraft.entity.EquipmentSlot;
import org.lwjgl.glfw.GLFW;

/**
 * Elytra bounce: hold forward + sprint, jump off the ground, and re-press jump in the air so
 * the game's own gliding check starts the glide (vanilla sends the start-gliding command itself).
 * Needs an elytra equipped. Pitch is locked to the Pitch setting while active.
 */
public class ElytraBounce extends Module {
    private final Setting pitch = add("Pitch", 90, 0, 90, 5);
    private int ticks;

    public ElytraBounce() { super("ElytraBounce", GLFW.GLFW_KEY_U); }

    @Override
    public void onTick() {
        if (mc.player == null) return;
        if (!mc.player.getEquippedStack(EquipmentSlot.CHEST).isOf(Items.ELYTRA)) return;

        ticks++;
        mc.options.forwardKey.setPressed(true);
        mc.options.sprintKey.setPressed(true);
        mc.player.setPitch(pitch.getF());

        // Jump on the ground; in the air, alternate released/pressed so each press is a fresh
        // jump input (vanilla only starts gliding on a new press). Stop pressing once gliding.
        boolean press;
        if (mc.player.isOnGround()) press = true;
        else if (mc.player.isGliding()) press = false;
        else press = (ticks % 2 == 0);
        mc.options.jumpKey.setPressed(press);
    }

    @Override
    protected void onDisable() {
        mc.options.forwardKey.setPressed(false);
        mc.options.sprintKey.setPressed(false);
        mc.options.jumpKey.setPressed(false);
    }
}
