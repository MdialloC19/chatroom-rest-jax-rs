package com.chatroom.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

/**
 * Gère l'état global de la chatroom (utilisateurs et messages).
 * <p>
 * Cette classe implémente le pattern Singleton pour fournir un point d'accès unique
 * à l'état de la chatroom. Elle maintient la liste des utilisateurs connectés et 
 * l'historique des messages. Toutes les opérations de gestion des utilisateurs et 
 * des messages passent par cette classe.
 * </p>
 * <p>
 * Pour assurer la sécurité thread et la concurrence, toutes les collections utilisées
 * sont thread-safe (ConcurrentHashMap, CopyOnWriteArrayList).
 * </p>
 *
 * @author ESP-DIC3
 * @version 1.0
 */
public class ChatManager {
    private static final ChatManager instance = new ChatManager();
    
    // Utilisations de collections thread-safe pour la concurrence
    private final Map<String, User> users = new ConcurrentHashMap<>();
    private final List<Message> messages = new CopyOnWriteArrayList<>();
    
    // Singleton
    private ChatManager() {}
    
    public static ChatManager getInstance() {
        return instance;
    }
    
    /**
     * Ajoute un utilisateur à la chatroom
     * @param username Le nom d'utilisateur
     * @return L'utilisateur créé, ou null si le nom existe déjà
     */
    public User addUser(String username) {
        if (users.containsKey(username)) {
            return null; // Utilisateur déjà existant
        }
        
        User user = new User(username);
        users.put(username, user);
        
        // Ajouter un message système pour annoncer l'arrivée
        addSystemMessage(username + " a rejoint la chatroom");
        
        return user;
    }
    
    /**
     * Vérifie si un utilisateur existe et met à jour son activité
     * @param username Le nom d'utilisateur
     * @return true si l'utilisateur existe, false sinon
     */
    public boolean userExists(String username) {
        User user = users.get(username);
        if (user != null) {
            user.updateActivity();
            return true;
        }
        return false;
    }
    
    /**
     * Supprime un utilisateur de la chatroom
     * @param username Le nom d'utilisateur à supprimer
     */
    public void removeUser(String username) {
        User user = users.remove(username);
        if (user != null) {
            // Ajouter un message système pour annoncer le départ
            addSystemMessage(username + " a quitté la chatroom");
        }
    }
    
    /**
     * Ajoute un message à la chatroom
     * @param sender L'expéditeur du message
     * @param content Le contenu du message
     * @return Le message créé, ou null si l'utilisateur n'existe pas
     */
    public Message addMessage(String sender, String content) {
        if (!userExists(sender)) {
            return null; // L'utilisateur n'existe pas
        }
        
        Message message = new Message(sender, content);
        messages.add(message);
        return message;
    }
    
    /**
     * Ajoute un message système à la chatroom
     * @param content Le contenu du message système
     */
    public void addSystemMessage(String content) {
        Message message = new Message("System", content);
        messages.add(message);
    }
    
    /**
     * Récupère les messages à partir d'un certain timestamp
     * @param since Le timestamp à partir duquel récupérer les messages (0 pour tous)
     * @return La liste des messages depuis le timestamp spécifié
     */
    public List<Message> getMessagesSince(long since) {
        return messages.stream()
                .filter(message -> message.getTimestamp() > since)
                .collect(Collectors.toList());
    }
    
    /**
     * Récupère tous les messages de la chatroom
     * @return La liste de tous les messages
     */
    public List<Message> getAllMessages() {
        return new ArrayList<>(messages);
    }
    
    /**
     * Récupère tous les utilisateurs connectés
     * @return La liste des utilisateurs
     */
    public List<User> getAllUsers() {
        return new ArrayList<>(users.values());
    }
    
    /**
     * Nettoie les utilisateurs inactifs
     * @param maxInactiveTime Temps maximum d'inactivité en millisecondes
     * @return Le nombre d'utilisateurs supprimés
     */
    public int cleanInactiveUsers(long maxInactiveTime) {
        long currentTime = System.currentTimeMillis();
        List<String> inactiveUsers = users.values().stream()
                .filter(user -> (currentTime - user.getLastActive()) > maxInactiveTime)
                .map(User::getUsername)
                .collect(Collectors.toList());
        
        inactiveUsers.forEach(this::removeUser);
        return inactiveUsers.size();
    }
}
