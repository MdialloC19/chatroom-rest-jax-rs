package com.chatroom.rest;

import com.chatroom.model.ChatManager;
import com.chatroom.model.Message;
import com.chatroom.model.User;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

/**
 * Classe de ressource REST pour la chatroom qui expose les endpoints API pour gérer les utilisateurs et les messages.
 * <p>
 * Cette classe est le point central d'accès à l'API REST de l'application de chat. Elle fournit
 * des endpoints pour toutes les opérations liées aux utilisateurs et aux messages, permettant au client
 * d'interagir avec le système de chat via des requêtes HTTP standard.
 * </p>
 * 
 * <p>Endpoints principaux:</p>
 * <ul>
 *   <li><b>GET /chat/users</b> - Récupère la liste des utilisateurs connectés</li>
 *   <li><b>POST /chat/users</b> - Inscrit un nouvel utilisateur</li>
 *   <li><b>DELETE /chat/users/{username}</b> - Déconnecte un utilisateur</li>
 *   <li><b>PUT /chat/users/{username}/heartbeat</b> - Maintient un utilisateur actif</li>
 *   <li><b>GET /chat/messages</b> - Récupère les messages (avec paramètre optionnel since)</li>
 *   <li><b>POST /chat/messages</b> - Envoie un nouveau message</li>
 * </ul>
 * 
 * <p>Chaque endpoint renvoie une réponse appropriée avec un code de statut HTTP et, si nécessaire, 
 * un corps de réponse au format JSON.</p>
 * 
 * @author ESP-DIC3
 * @version 1.0
 */
@Path("/chat")
public class ChatResource {
    private final ChatManager chatManager = ChatManager.getInstance();
    
    /**
     * Enregistre un nouvel utilisateur dans la chatroom
     * @param userInfo Map contenant les informations de l'utilisateur (username)
     * @return Réponse HTTP avec statut et corps appropriés
     */
    @POST
    @Path("/users")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response registerUser(Map<String, String> userInfo) {
        if (!userInfo.containsKey("username")) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("Le nom d'utilisateur est requis")
                    .build();
        }
        
        String username = userInfo.get("username");
        User user = chatManager.addUser(username);
        
        if (user == null) {
            return Response.status(Response.Status.CONFLICT)
                    .entity("Le nom d'utilisateur est déjà pris")
                    .build();
        }
        
        return Response.status(Response.Status.CREATED)
                .entity(user)
                .build();
    }
    
    /**
     * Récupère la liste des utilisateurs connectés
     * @return Liste des utilisateurs
     */
    @GET
    @Path("/users")
    @Produces(MediaType.APPLICATION_JSON)
    public List<User> getUsers() {
        return chatManager.getAllUsers();
    }
    
    /**
     * Vérifie si un utilisateur est toujours actif et met à jour son statut
     * @param username Le nom d'utilisateur à vérifier
     * @return Réponse HTTP avec statut approprié
     */
    @PUT
    @Path("/users/{username}/heartbeat")
    @Produces(MediaType.APPLICATION_JSON)
    public Response heartbeat(@PathParam("username") String username) {
        boolean exists = chatManager.userExists(username);
        
        if (!exists) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity("Utilisateur non trouvé")
                    .build();
        }
        
        Map<String, Boolean> result = new HashMap<>();
        result.put("active", true);
        
        return Response.status(Response.Status.OK)
                .entity(result)
                .build();
    }
    
    /**
     * Déconnecte un utilisateur de la chatroom
     * @param username Le nom d'utilisateur à déconnecter
     * @return Réponse HTTP avec statut approprié
     */
    @DELETE
    @Path("/users/{username}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response removeUser(@PathParam("username") String username) {
        if (!chatManager.userExists(username)) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity("Utilisateur non trouvé")
                    .build();
        }
        
        chatManager.removeUser(username);
        return Response.status(Response.Status.OK)
                .entity("Utilisateur déconnecté avec succès")
                .build();
    }
    
    /**
     * Ajoute un nouveau message à la chatroom
     * @param messageInfo Map contenant les informations du message (sender, content)
     * @return Réponse HTTP avec statut et corps appropriés
     */
    @POST
    @Path("/messages")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response addMessage(Map<String, String> messageInfo) {
        if (!messageInfo.containsKey("sender") || !messageInfo.containsKey("content")) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("L'expéditeur et le contenu sont requis")
                    .build();
        }
        
        String sender = messageInfo.get("sender");
        String content = messageInfo.get("content");
        
        Message message = chatManager.addMessage(sender, content);
        
        if (message == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity("Utilisateur non trouvé")
                    .build();
        }
        
        return Response.status(Response.Status.CREATED)
                .entity(message)
                .build();
    }
    
    /**
     * Récupère les messages de la chatroom
     * @param since Paramètre optionnel pour récupérer uniquement les messages depuis un certain timestamp
     * @return Liste des messages
     */
    @GET
    @Path("/messages")
    @Produces(MediaType.APPLICATION_JSON)
    public List<Message> getMessages(@QueryParam("since") @DefaultValue("0") long since) {
        return chatManager.getMessagesSince(since);
    }
}
