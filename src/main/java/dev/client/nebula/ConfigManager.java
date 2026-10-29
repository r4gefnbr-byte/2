package dev.client.nebula;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import dev.client.nebula.module.Module;
import dev.client.nebula.module.Setting;
import net.fabricmc.loader.api.FabricLoader;

import java.nio.file.Files;
import java.nio.file.Path;

/** Saves keybinds and setting values to config/nebula/config.json. Enabled state is not restored. */
public final class ConfigManager {
    private static final Path FILE = FabricLoader.getInstance().getConfigDir().resolve("nebula/config.json");
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    private ConfigManager() {}

    public static void save() {
        try {
            JsonObject root = new JsonObject();
            for (Module m : NebulaClient.MODULES.all()) {
                JsonObject mo = new JsonObject();
                mo.addProperty("key", m.getKey());
                JsonObject so = new JsonObject();
                for (Setting s : m.getSettings()) so.addProperty(s.getName(), s.get());
                mo.add("settings", so);
                root.add(m.getName(), mo);
            }
            Files.createDirectories(FILE.getParent());
            Files.writeString(FILE, GSON.toJson(root));
        } catch (Exception ignored) {}
    }

    public static void load() {
        try {
            if (!Files.exists(FILE)) return;
            JsonObject root = JsonParser.parseString(Files.readString(FILE)).getAsJsonObject();
            for (Module m : NebulaClient.MODULES.all()) {
                if (!root.has(m.getName())) continue;
                JsonObject mo = root.getAsJsonObject(m.getName());
                if (mo.has("key")) m.setKey(mo.get("key").getAsInt());
                if (mo.has("settings")) {
                    JsonObject so = mo.getAsJsonObject("settings");
                    for (Setting s : m.getSettings()) {
                        if (so.has(s.getName())) s.set(so.get(s.getName()).getAsDouble());
                    }
                }
            }
        } catch (Exception ignored) {}
    }
}
