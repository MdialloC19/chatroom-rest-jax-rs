package com.chatroom.rest;

import java.io.IOException;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.container.ContainerResponseFilter;
import javax.ws.rs.ext.Provider;

/**
 * Filtre CORS (Cross-Origin Resource Sharing) pour permettre les requêtes cross-origin.
 * <p>
 * Cette classe implémente un filtre de réponse JAX-RS qui ajoute les en-têtes CORS nécessaires
 * pour permettre aux clients web d'effectuer des requêtes cross-origin vers l'API REST.
 * C'est essentiel pour le développement et le déploiement de l'interface utilisateur
 * sur un domaine ou un port différent de celui du serveur.
 * </p>
 *
 * <p>En-têtes CORS ajoutés à chaque réponse :</p>
 * <ul>
 *   <li>Access-Control-Allow-Origin: * (autorise toutes les origines)</li>
 *   <li>Access-Control-Allow-Methods: GET, POST, PUT, DELETE, OPTIONS</li>
 *   <li>Access-Control-Allow-Headers: origin, content-type, accept, authorization</li>
 *   <li>Access-Control-Allow-Credentials: true</li>
 * </ul>
 *
 * @author ESP-DIC3
 * @version 1.0
 * @see ContainerResponseFilter
 */
@Provider
public class CORSFilter implements ContainerResponseFilter {

    /**
     * Ajoute les en-têtes CORS à chaque réponse HTTP sortante.
     *
     * @param requestContext le contexte de la requête entrante
     * @param responseContext le contexte de la réponse sortante à modifier
     * @throws IOException en cas d'erreur d'entrée/sortie lors du traitement de la réponse
     */
    @Override
    public void filter(ContainerRequestContext requestContext, ContainerResponseContext responseContext) throws IOException {
        // Autoriser toutes les origines
        responseContext.getHeaders().add("Access-Control-Allow-Origin", "*");
        
        // Méthodes HTTP autorisées
        responseContext.getHeaders().add("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
        
        // En-têtes autorisés dans les requêtes
        responseContext.getHeaders().add("Access-Control-Allow-Headers", "origin, content-type, accept, authorization");
        
        // Autoriser l'envoi de cookies et d'informations d'authentification
        responseContext.getHeaders().add("Access-Control-Allow-Credentials", "true");
    }
}
