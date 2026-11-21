package com.aiotter;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.minecraftforge.fml.loading.FMLPaths;

import java.nio.file.Files;
import java.nio.file.Path;

/** Config en .minecraft/config/ai_otter.json */
public class AiOtterConfig {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static AiOtterConfig INSTANCE = new AiOtterConfig();

    /** Tu API key de Anthropic. Si está vacía se usa la variable de entorno ANTHROPIC_API_KEY. Sin key: frases predefinidas. */
    public String apiKey = "";
    public String model = "claude-haiku-5-5";
    public int aiCooldownSeconds = 3;

    public static AiOtterConfig get() { return INSTANCE; }

    public String key() {
        if (apiKey != null && !apiKey.isBlank()) return apiKey.trim();
        String env = System.getenv("ANTHROPIC_API_KEY");
        return env == null ? "" : env.trim();
    }

    public static void load() {
        Path p = FMLPaths.CONFIGDIR.get().resolve("ai_otter.json");
        try {
            if (Files.exists(p)) {
                AiOtterConfig c = GSON.fromJson(Files.readString(p), AiOtterConfig.class);
                if (c != null) INSTANCE = c;
            } else {
                Files.writeString(p, GSON.toJson(INSTANCE));
            }
        } catch (Exception e) {
            System.err.println("[AiOtter] No se pudo leer la config: " + e.getMessage());
        }
    }
}
