package dev.client.nebula.module.impl;

import dev.client.nebula.module.Module;
import net.minecraft.item.Items;
import net.minecraft.screen.slot.SlotActionType;
import org.lwjgl.glfw.GLFW;

public class AutoTotem extends Module {
    public AutoTotem() { super("AutoTotem", GLFW.GLFW_KEY_T); }

    @Override
    public void onTick() {
        if (mc.player == null || mc.interactionManager == null || mc.currentScreen != null) return;
        if (mc.player.getOffHandStack().isOf(Items.TOTEM_OF_UNDYING)) return;

        // Player inventory screen handler: slots 9-35 main, 36-44 hotbar, 45 offhand.
        for (int slot = 9; slot <= 44; slot++) {
            if (mc.player.playerScreenHandler.getSlot(slot).getStack().isOf(Items.TOTEM_OF_UNDYING)) {
                int sync = mc.player.playerScreenHandler.syncId;
                mc.interactionManager.clickSlot(sync, slot, 0, SlotActionType.PICKUP, mc.player);
                mc.interactionManager.clickSlot(sync, 45, 0, SlotActionType.PICKUP, mc.player);
                mc.interactionManager.clickSlot(sync, slot, 0, SlotActionType.PICKUP, mc.player);
                return;
            }
        }
    }
}
