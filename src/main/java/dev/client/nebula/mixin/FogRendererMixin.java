package dev.client.nebula.mixin;

import dev.client.nebula.NebulaClient;
import dev.client.nebula.module.impl.Fog;
import net.minecraft.client.render.fog.FogRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

/**
 * Hooks the private overload that writes the fog distances into the fog uniform buffer
 * (verified against yarn 1.21.7; if 1.21.11 changed it the game still starts, this
 * mixin config is non-required, and Fog just does nothing).
 */
@Mixin(FogRenderer.class)
public abstract class FogRendererMixin {
    private static final String TARGET = "applyFog(Ljava/nio/ByteBuffer;ILorg/joml/Vector4f;FFFFFF)V";

    @ModifyVariable(method = TARGET, at = @At("HEAD"), argsOnly = true, ordinal = 0)
    private float nebula$envStart(float v) { return nebula$fog(v, Fog.ENV_START); }

    @ModifyVariable(method = TARGET, at = @At("HEAD"), argsOnly = true, ordinal = 1)
    private float nebula$envEnd(float v) { return nebula$fog(v, Fog.ENV_END); }

    @ModifyVariable(method = TARGET, at = @At("HEAD"), argsOnly = true, ordinal = 2)
    private float nebula$rdStart(float v) { return nebula$fog(v, Fog.RD_START); }

    @ModifyVariable(method = TARGET, at = @At("HEAD"), argsOnly = true, ordinal = 3)
    private float nebula$rdEnd(float v) { return nebula$fog(v, Fog.RD_END); }

    @ModifyVariable(method = TARGET, at = @At("HEAD"), argsOnly = true, ordinal = 4)
    private float nebula$skyEnd(float v) { return nebula$fog(v, Fog.SKY_END); }

    @ModifyVariable(method = TARGET, at = @At("HEAD"), argsOnly = true, ordinal = 5)
    private float nebula$cloudEnd(float v) { return nebula$fog(v, Fog.CLOUD_END); }

    private static float nebula$fog(float original, int which) {
        Fog fog = NebulaClient.MODULES.get(Fog.class);
        return fog != null && fog.isEnabled() ? fog.adjust(original, which) : original;
    }
}
