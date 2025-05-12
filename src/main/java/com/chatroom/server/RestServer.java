package com.chatroom.server;

import com.chatroom.model.ChatManager;
import com.chatroom.rest.ChatApplication;
import com.chatroom.util.LogManager;
import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.jersey.grizzly2.httpserver.GrizzlyHttpServerFactory;

import java.io.IOException;
import java.net.URI;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Serveur REST principal qui démarre le serveur HTTP Grizzly et enregistre l'application JAX-RS.
 * <p>
 * Cette classe est responsable de l'initialisation et du démarrage du serveur REST pour
 * l'application de chat. Elle utilise le serveur HTTP Grizzly comme conteneur pour
 * l'application JAX-RS, configure le nettoyage périodique des utilisateurs inactifs,
 * et gère le cycle de vie du serveur.
 * </p>
 * 
 * <p>Fonctionnalités principales:</p>
 * <ul>
 *   <li>Démarrage du serveur HTTP sur le port 8081</li>
 *   <li>Enregistrement de l'application JAX-RS (endpoints REST)</li>
 *   <li>Nettoyage périodique des utilisateurs inactifs</li>
 *   <li>Arrêt propre du serveur</li>
 * </ul>
 * 
 * @author ESP-DIC3
 * @version 1.0
 */
public class RestServer {
    // Base URI du serveur
    public static final String BASE_URI = "http://localhost:8081/";
    
    // Temps d'inactivité maximum pour un utilisateur (15 minutes)
    private static final long MAX_INACTIVE_TIME = 15 * 60 * 1000;
    
    // Logger pour les messages du serveur
    private static final Logger LOGGER = LogManager.getLogger(RestServer.class);
    
    /**
     * Démarre le serveur HTTP Grizzly avec l'application JAX-RS.
     * @return Le serveur HTTP
     */
    public static HttpServer startServer() {
        // Créer l'application JAX-RS
        final ChatApplication resourceConfig = new ChatApplication();
        
        // Créer et démarrer le serveur HTTP
        final HttpServer server = GrizzlyHttpServerFactory.createHttpServer(
                URI.create(BASE_URI), resourceConfig);
                
        LOGGER.info("Serveur HTTP Grizzly créé sur " + BASE_URI);
        return server;
    }
    
    /**
     * Méthode principale qui démarre le serveur
     * @param args Arguments de ligne de commande (non utilisés)
     * @throws IOException Si une erreur survient lors du démarrage du serveur
     */
    public static void main(String[] args) throws IOException {
        LOGGER.info("Démarrage du serveur REST...");
        
        // Démarrer le serveur HTTP
        final HttpServer server = startServer();
        
        // Configurer un nettoyage périodique des utilisateurs inactifs
        LOGGER.info("Configuration du nettoyage des utilisateurs inactifs");
        Timer timer = new Timer(true);
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                try {
                    int removedCount = ChatManager.getInstance().cleanInactiveUsers(MAX_INACTIVE_TIME);
                    if (removedCount > 0) {
                        LOGGER.info(removedCount + " utilisateur(s) inactif(s) supprimé(s)");
                    }
                } catch (Exception e) {
                    LOGGER.log(Level.SEVERE, "Erreur lors du nettoyage des utilisateurs inactifs", e);
                }
            }
        }, 60000, 60000); // Exécuter toutes les minutes
        
        // Afficher un message de démarrage
        String serverUrl = BASE_URI + "api/chat";
        LOGGER.info("Serveur REST démarré avec succès!");
        LOGGER.info("Endpoints disponibles:");
        LOGGER.info("  * " + serverUrl + "/users (GET, POST)");
        LOGGER.info("  * " + serverUrl + "/users/{username} (DELETE)");
        LOGGER.info("  * " + serverUrl + "/users/{username}/heartbeat (PUT)");
        LOGGER.info("  * " + serverUrl + "/messages (GET, POST)");
        LOGGER.info("Appuyez sur Entrée pour arrêter le serveur...");
        
        // Aussi afficher à la console standard pour être sûr que l'utilisateur le voit
        System.out.println("=====================================================");
        System.out.println("| Serveur REST démarré avec succès sur " + BASE_URI + " |");
        System.out.println("| Appuyez sur Entrée pour arrêter le serveur...         |");
        System.out.println("=====================================================");
        
        try {
            // Attendre que l'utilisateur appuie sur Entrée
            System.in.read();
            
            // Arrêter le serveur
            server.shutdownNow();
            LOGGER.info("Serveur arrêté.");
            System.out.println("Serveur arrêté.");
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Erreur lors de l'arrêt du serveur", e);
        }
    }
}
