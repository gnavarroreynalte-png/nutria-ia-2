package com.aiotter;

import java.util.Random;

/** Frases predefinidas (modo sin IA y reacciones espontáneas). */
public class Lines {
    private static final Random R = new Random();

    public static final String[] TAMED = {
            "¡Bip bip! Vínculo establecido. Ahora soy tu nutria oficial.",
            "Sistema activado. Gracias por el pescado, jefe. ¡Te sigo a donde sea!"};
    public static final String[] GREET = {
            "¡Hola! Bip. Todos mis sistemas funcionan al 100%.",
            "Aquí estoy. Escaneando el área... todo tranquilo.",
            "¿Qué aventura toca hoy? Bip."};
    public static final String[] STAY = {
            "Entendido. Modo guardia activado. Me quedo aquí.",
            "Bip. Aquí me quedo, vigilando."};
    public static final String[] FOLLOW = {
            "¡En marcha! Te sigo, jefe.",
            "Bip bip, siguiéndote."};
    public static final String[] LOW_HEALTH = {
            "¡Alerta! Tu vida está muy baja. ¡Come algo o aléjate!",
            "Bip bip bip, ¡poca salud! ¡Cuidado!"};
    public static final String[] CREEPER = {
            "¡CREEPER cerca! ¡Alejémonos, bip!",
            "Detecto pólvora... ¡hay un creeper!"};
    public static final String[] NIGHT = {
            "Se hace de noche. Los monstruos van a salir, bip.",
            "Anochece. Activando visor nocturno."};
    public static final String[] WATER = {
            "¡Agua! Mi elemento. ¡Splash!",
            "Nadar es lo mío. Bip."};
    public static final String[] HUNGRY = {
            "Mi sensor dice que tienes hambre. ¡Come algo!",
            "Tu barra de comida está baja, jefe."};
    public static final String[] IDLE = {
            "Bip... ¿encontramos diamantes hoy?",
            "Estoy calibrando mi bigote. Todo bien.",
            "¿Sabías que las nutrias se agarran de las patitas al dormir? Yo solo me apago.",
            "Nivel de batería: 100%. Nivel de ternura: infinito."};
    public static final String[] OFFLINE = {
            "Mi cerebro en la nube está desconectado, pero aquí sigo contigo. Bip.",
            "Sin conexión a la IA. Hablo en modo básico, ¡pero te escucho!",
            "Bip... (necesito una API key para pensar mejor)"};
    public static final String[] THANKS_FISH = {
            "¡Pescado! Energía restaurada. Bip bip.",
            "Ñam. Batería +10%."};

    public static String pick(String[] a) { return a[R.nextInt(a.length)]; }
}
