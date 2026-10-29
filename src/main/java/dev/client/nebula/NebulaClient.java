package dev.client.nebula;

import dev.client.nebula.hud.Hud;
import dev.client.nebula.module.ModuleManager;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;

public class NebulaClient implements ClientModInitializer {
    public static final String NAME = "Nebula";
    public static final ModuleManager MODULES = new ModuleManager();
    public static final Hud HUD = new Hud();

    @Override
    public void onInitializeClient() {
        MODULES.init();
        ConfigManager.load();
        HUD.init();
        ClientLifecycleEvents.CLIENT_STOPPING.register(client -> ConfigManager.save());
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            MODULES.tick(client);
            HUD.tick(client);
        });
        // HUD API has shifted across recent versions; if this fails to compile on 1.21.11,
        // swap to HudElementRegistry (see Fabric API changelog).
        HudRenderCallback.EVENT.register((context, tickCounter) -> HUD.render(context));
    }
}
