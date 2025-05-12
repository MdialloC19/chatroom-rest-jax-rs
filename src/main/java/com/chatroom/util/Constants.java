package com.chatroom.util;

import java.awt.Color;

/**
 * Classe utilitaire centralisant toutes les constantes de l'application.
 * <p>
 * Cette classe évite la duplication des constantes dans plusieurs classes
 * et facilite la maintenance en centralisant les valeurs importantes.
 * </p>
 *
 * @author ESP-DIC3
 * @version 1.0
 */
public final class Constants {
    // Empêcher l'instanciation
    private Constants() {}
    
    /**
     * Configuration du serveur et de l'API
     */
    public static final class Server {
        public static final String HOST = "localhost";
        public static final int PORT = 8081;
        public static final String BASE_URI = "http://" + HOST + ":" + PORT + "/";
        public static final String API_PATH = "chat";
        public static final String API_BASE_URL = BASE_URI + API_PATH;
    }
    
    /**
     * Couleurs de l'interface utilisateur
     */
    public static final class Colors {
        public static final Color WHATSAPP_GREEN = new Color(18, 140, 126);
        public static final Color WHATSAPP_LIGHT_GREEN = new Color(220, 248, 198);
        public static final Color WHATSAPP_BACKGROUND = new Color(230, 230, 230);
        public static final Color WHATSAPP_BUTTON_HOVER = new Color(0, 168, 132);
        public static final Color WHATSAPP_GREY = new Color(233, 237, 239);
        public static final Color WHATSAPP_LIGHT_GREY = new Color(241, 241, 241);
        public static final Color WHATSAPP_TEXT_COLOR = new Color(255, 255, 255);
        public static final Color MESSAGE_BUBBLE_GRAY = new Color(240, 240, 240);
        public static final Color MESSAGE_TIME_COLOR = new Color(128, 128, 128);
        public static final Color SELECTED_USER_COLOR = new Color(229, 242, 255);
    }
    
    /**
     * Constantes pour la gestion des polling et timeouts
     */
    public static final class Timing {
        public static final int POLLING_INTERVAL_MS = 1000;
        public static final int CONNECTION_TIMEOUT_MS = 5000;
        public static final int READ_TIMEOUT_MS = 5000;
        public static final int USER_EXPIRY_SECONDS = 30;
    }
}
