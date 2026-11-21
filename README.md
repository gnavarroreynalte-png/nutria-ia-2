# Nutria IA (Forge 1.20.1)

## Compilar (lo más fácil)
1. Instala JDK 17.
2. Descarga el MDK de Forge 1.20.1 (47.x) desde https://files.minecraftforge.net/ y descomprímelo.
3. Borra su carpeta `src` y copia aquí la carpeta `src` de este proyecto
   (y deja el build.gradle del MDK; el mío es solo de referencia).
4. Ejecuta `./gradlew build` (Windows: `gradlew build`).
5. El .jar queda en `build/libs/`. Ponlo en `.minecraft/mods` con Forge 1.20.1.

## Uso
- Huevo generador en el creativo o `/summon aiotter:ai_otter`.
- Bacalao o salmón = domesticar y curar.
- Clic derecho mano vacía = saluda. Agachado + clic derecho = quedarse / seguir.
- Chat: `nutria, ¿qué hacemos?` (también `otter` o `bip`), `nutria quédate`, `nutria sígueme`.

## IA real
Se crea `.minecraft/config/ai_otter.json`. Pega tu API key de Anthropic en `apiKey`
(o variable de entorno `ANTHROPIC_API_KEY`). Sin key usa frases predefinidas.
No compartas ese archivo.
