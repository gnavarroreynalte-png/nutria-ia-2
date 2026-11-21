package com.aiotter;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.minecraft.server.MinecraftServer;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.List;
import java.util.function.Consumer;

/** Llama a la API de Anthropic en segundo plano (no congela el juego). */
public class AiBrain {
    private static final HttpClient HTTP = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(8)).build();

    private static final String SYSTEM = """
            Eres "Nutria IA", una nutria robot compañera de un jugador en Minecraft. Tienes un visor azul, \
            una pantalla en el pecho y antena. Eres leal, curiosa, graciosa y un poco dramática.
            Reglas: responde SIEMPRE en español, con máximo 25 palabras, sin markdown ni emojis. \
            De vez en cuando suelta un "bip". Habla como un personaje que vive dentro del mundo de Minecraft. \
            Usa el contexto del juego si es relevante (salud, hora, bioma, peligros) pero no lo recites entero. \
            Si te piden cosas que no puedes hacer físicamente, bromea con que tus sistemas no lo permiten.
            """;

    /**
     * @param history lista de pares {usuario, nutria} previos (máx. unos pocos)
     * @param ok      se ejecuta en el hilo del servidor con la respuesta
     * @param fail    se ejecuta en el hilo del servidor si no hay key o falla la llamada
     */
    public static void ask(MinecraftServer server, String playerMsg, String context,
                           List<String[]> history, Consumer<String> ok, Runnable fail) {
        AiOtterConfig cfg = AiOtterConfig.get();
        String key = cfg.key();
        if (key.isBlank()) { fail.run(); return; }

        JsonObject body = new JsonObject();
        body.addProperty("model", cfg.model);
        body.addProperty("max_tokens", 200);
        body.addProperty("system", SYSTEM + "\nContexto actual del juego: " + context);
        JsonArray msgs = new JsonArray();
        for (String[] pair : history) {
            msgs.add(msg("user", pair[0]));
            msgs.add(msg("assistant", pair[1]));
        }
        msgs.add(msg("user", playerMsg));
        body.add("messages", msgs);

        HttpRequest req = HttpRequest.newBuilder(URI.create("https://api.anthropic.com/v1/messages"))
                .timeout(Duration.ofSeconds(20))
                .header("content-type", "application/json")
                .header("x-api-key", key)
                .header("anthropic-version", "2023-06-01")
                .POST(HttpRequest.BodyPublishers.ofString(body.toString()))
                .build();

        HTTP.sendAsync(req, HttpResponse.BodyHandlers.ofString()).thenAccept(r -> {
            String text = null;
            if (r.statusCode() == 200) {
                try {
                    JsonObject o = JsonParser.parseString(r.body()).getAsJsonObject();
                    text = o.getAsJsonArray("content").get(0).getAsJsonObject().get("text").getAsString().trim();
                } catch (Exception ignored) { }
            } else {
                System.err.println("[AiOtter] La API respondió con código " + r.statusCode());
            }
            final String t = text;
            server.execute(() -> {
                if (t == null || t.isEmpty()) fail.run();
                else ok.accept(t.length() > 240 ? t.substring(0, 240) : t);
            });
        }).exceptionally(ex -> {
            System.err.println("[AiOtter] Error de red: " + ex.getMessage());
            server.execute(fail);
            return null;
        });
    }

    private static JsonObject msg(String role, String content) {
        JsonObject m = new JsonObject();
        m.addProperty("role", role);
        m.addProperty("content", content);
        return m;
    }
}
