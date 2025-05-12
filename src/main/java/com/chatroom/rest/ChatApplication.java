package com.chatroom.rest;

import org.glassfish.jersey.jackson.JacksonFeature;
import org.glassfish.jersey.server.ResourceConfig;

import javax.ws.rs.ApplicationPath;

/**
 * Configuration de l'application JAX-RS pour le serveur de chat.
 * <p>
 * Cette classe étend ResourceConfig (Jersey) et configure l'application JAX-RS
 * en enregistrant les ressources et les fonctionnalités nécessaires au bon fonctionnement
 * du service REST. Elle définit notamment :
 * </p>
 * 
 * <ul>
 *   <li>Le package contenant les ressources REST</li>
 *   <li>L'intégration de Jackson pour la sérialisation/désérialisation JSON</li>
 *   <li>Le support CORS pour permettre les requêtes cross-origin</li>
 * </ul>
 * 
 * <p>Cette classe est chargée automatiquement par le serveur Grizzly grâce à
 * l'annotation @ApplicationPath qui définit le chemin de base pour tous les endpoints.</p>
 * 
 * @author ESP-DIC3
 * @version 1.0
 */
@ApplicationPath("/api")
public class ChatApplication extends ResourceConfig {
    
    public ChatApplication() {
        // Enregistrer le package contenant les ressources
        packages("com.chatroom.rest");
        
        // Enregistrer la fonctionnalité Jackson pour le support JSON
        register(JacksonFeature.class);
        
        // Configurer CORS (Cross-Origin Resource Sharing)
        register(CORSFilter.class);
    }
}
