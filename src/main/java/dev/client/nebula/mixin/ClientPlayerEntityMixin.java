package dev.client.nebula.mixin;

import dev.client.nebula.NebulaClient;
import dev.client.nebula.module.impl.NoSlow;
import net.minecraft.client.network.ClientPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(ClientPlayerEntity.class)
public abstract class ClientPlayerEntityMixin {
    // Vanilla slows movement while isUsingItem() is true (eating, bows, shields).
    // Reporting false at that one call site removes the slowdown without
    // touching the actual item-use state.
    @Redirect(
        method = "tickMovement",
        at = @At(value = "INVOKE",
                 target = "Lnet/minecraft/client/network/ClientPlayerEntity;isUsingItem()Z")
    )
    private boolean nebula$noSlow(ClientPlayerEntity self) {
        NoSlow noSlow = NebulaClient.MODULES.get(NoSlow.class);
        if (noSlow != null && noSlow.isEnabled()) return false;
        return self.isUsingItem();
    }
}
