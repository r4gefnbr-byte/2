package dev.client.nebula.mixin;

import dev.client.nebula.NebulaClient;
import dev.client.nebula.module.impl.NoVerticalKB;
import dev.client.nebula.module.impl.Velocity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.network.packet.s2c.play.EntityVelocityUpdateS2CPacket;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPlayNetworkHandler.class)
public abstract class ClientPlayNetworkHandlerMixin {
    @Unique private double nebula$preY;

    @Inject(method = "onEntityVelocityUpdate", at = @At("HEAD"), cancellable = true)
    private void nebula$velocityHead(EntityVelocityUpdateS2CPacket packet, CallbackInfo ci) {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null || packet.getEntityId() != mc.player.getId()) return;

        Velocity v = NebulaClient.MODULES.get(Velocity.class);
        if (v != null && v.isEnabled()) { ci.cancel(); return; } // drop all knockback

        nebula$preY = mc.player.getVelocity().y; // remember Y so we can restore it afterwards
    }

    @Inject(method = "onEntityVelocityUpdate", at = @At("RETURN"))
    private void nebula$velocityReturn(EntityVelocityUpdateS2CPacket packet, CallbackInfo ci) {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null || packet.getEntityId() != mc.player.getId()) return;

        NoVerticalKB nv = NebulaClient.MODULES.get(NoVerticalKB.class);
        if (nv != null && nv.isEnabled()) {
            Vec3d now = mc.player.getVelocity();
            mc.player.setVelocity(now.x, nebula$preY, now.z); // horizontal stays, vertical reverted
        }
    }
}
