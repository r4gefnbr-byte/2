package dev.client.nebula.hud;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import dev.client.nebula.gui.Theme;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;

/**
 * Now-playing card. Polls the Spotify Web API on a background thread every 5 seconds.
 * Paste an access token (scope: user-read-currently-playing) into
 * config/nebula/spotify_token.txt. Tokens expire after ~1 hour; see README note.
 */
public class SpotifyHud {
    private static final Path TOKEN = FabricLoader.getInstance().getConfigDir().resolve("nebula/spotify_token.txt");
    private final HttpClient http = HttpClient.newHttpClient();

    private volatile String title = "";
    private volatile String artist = "";
    private volatile long progressMs, durationMs;
    private volatile long fetchedAt;
    private volatile boolean playing;
    private volatile String status = "Add token to config/nebula/spotify_token.txt";

    public void start() {
        Thread t = new Thread(() -> {
            while (true) {
                try { poll(); } catch (Exception e) { status = "Spotify: " + e.getClass().getSimpleName(); }
                try { Thread.sleep(5000); } catch (InterruptedException ignored) { return; }
            }
        }, "nebula-spotify");
        t.setDaemon(true);
        t.start();
    }

    private void poll() throws Exception {
        if (!Files.exists(TOKEN)) { playing = false; return; }
        String token = Files.readString(TOKEN).trim();
        if (token.isEmpty()) { playing = false; return; }

        HttpRequest req = HttpRequest.newBuilder(URI.create("https://api.spotify.com/v1/me/player/currently-playing"))
                .header("Authorization", "Bearer " + token)
                .timeout(Duration.ofSeconds(4)).GET().build();
        HttpResponse<String> res = http.send(req, HttpResponse.BodyHandlers.ofString());

        if (res.statusCode() == 204) { playing = false; status = "Nothing playing"; return; }
        if (res.statusCode() == 401) { playing = false; status = "Spotify token expired"; return; }
        if (res.statusCode() != 200) { playing = false; status = "Spotify HTTP " + res.statusCode(); return; }

        JsonObject root = JsonParser.parseString(res.body()).getAsJsonObject();
        if (!root.has("item") || root.get("item").isJsonNull()) { playing = false; return; }
        JsonObject item = root.getAsJsonObject("item");
        title = item.get("name").getAsString();
        artist = item.getAsJsonArray("artists").get(0).getAsJsonObject().get("name").getAsString();
        durationMs = item.get("duration_ms").getAsLong();
        progressMs = root.get("progress_ms").getAsLong();
        fetchedAt = System.currentTimeMillis();
        playing = root.get("is_playing").getAsBoolean();
        status = "";
    }

    public void render(DrawContext ctx, int x, int y) {
        MinecraftClient mc = MinecraftClient.getInstance();
        int w = 130, h = 34;
        ctx.fill(x, y, x + w, y + h, Theme.BG);
        ctx.fill(x, y, x + 2, y + h, Theme.GOOD);

        if (!playing && !status.isEmpty()) {
            ctx.drawTextWithShadow(mc.textRenderer, "Spotify", x + 8, y + 6, Theme.GOOD);
            ctx.drawTextWithShadow(mc.textRenderer, trim(mc, status, w - 12), x + 8, y + 18, Theme.TEXT_DIM);
            return;
        }
        ctx.drawTextWithShadow(mc.textRenderer, trim(mc, title, w - 12), x + 8, y + 5, Theme.TEXT);
        ctx.drawTextWithShadow(mc.textRenderer, trim(mc, artist, w - 12), x + 8, y + 16, Theme.TEXT_DIM);

        long shown = progressMs + (playing ? System.currentTimeMillis() - fetchedAt : 0);
        float frac = durationMs <= 0 ? 0 : Math.min(1f, shown / (float) durationMs);
        ctx.fill(x + 8, y + 28, x + w - 6, y + 30, Theme.BG_ALT);
        ctx.fill(x + 8, y + 28, x + 8 + (int) ((w - 14) * frac), y + 30, Theme.GOOD);
    }

    private static String trim(MinecraftClient mc, String s, int maxW) {
        if (mc.textRenderer.getWidth(s) <= maxW) return s;
        while (s.length() > 1 && mc.textRenderer.getWidth(s + "...") > maxW) s = s.substring(0, s.length() - 1);
        return s + "...";
    }
}
