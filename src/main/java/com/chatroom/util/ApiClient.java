package com.chatroom.util;

import com.chatroom.model.Message;
import com.chatroom.model.User;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import static com.chatroom.util.Constants.Server.API_BASE_URL;

/**
 * Utilitaire pour les appels à l'API REST du serveur de chat.
 * <p>
 * Cette classe centralise toutes les communications avec le serveur REST et
 * implémente les méthodes nécessaires pour enregistrer des utilisateurs,
 * envoyer des messages et récupérer les données du chat.
 * </p>
 * 
 * @author ESP-DIC3
 * @version 1.0
 */
public class ApiClient {
    private static final Logger LOGGER = LogManager.getLogger(ApiClient.class);
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    
    /**
     * Enregistre un utilisateur auprès du serveur
     * 
     * @param username Nom d'utilisateur à enregistrer
     * @return L'utilisateur enregistré
     * @throws IOException En cas d'erreur de communication avec le serveur
     */
    public static User registerUser(String username) throws IOException {
        LOGGER.info("Tentative d'inscription de l'utilisateur: " + username);
        String requestBody = "{\"username\":\"" + username + "\"}";
        
        URL url = new URL(API_BASE_URL + "/users");
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("POST");
        connection.setRequestProperty("Content-Type", "application/json");
        connection.setDoOutput(true);
        
        
        try (OutputStream os = connection.getOutputStream()) {
            byte[] input = requestBody.getBytes("utf-8");
            os.write(input, 0, input.length);
        }
        
        int statusCode = connection.getResponseCode();
        LOGGER.info("Code de statut reçu: " + statusCode);
        
        if (statusCode == 201) {
          
            try (BufferedReader br = new BufferedReader(
                    new InputStreamReader(connection.getInputStream(), "utf-8"))) {
                StringBuilder response = new StringBuilder();
                String responseLine;
                while ((responseLine = br.readLine()) != null) {
                    response.append(responseLine.trim());
                }
                
                return OBJECT_MAPPER.readValue(response.toString(), User.class);
            }
        } else {
        
            StringBuilder response = new StringBuilder();
            InputStream stream = connection.getErrorStream();
            if (stream == null) {
                stream = connection.getInputStream();
            }
            
            if (stream != null) {
                try (BufferedReader br = new BufferedReader(new InputStreamReader(stream, "utf-8"))) {
                    String responseLine;
                    while ((responseLine = br.readLine()) != null) {
                        response.append(responseLine.trim());
                    }
                }
            } else {
                response.append(connection.getResponseMessage());
            }
            
            if (statusCode == 409) {
                throw new IOException("Erreur d'inscription: Le nom d'utilisateur '" + username + "' est déjà utilisé.");
            } else {
                throw new IOException("Erreur d'inscription (" + statusCode + "): " + response.toString());
            }
        }
    }
    
    /**
     * Envoie un message au serveur
     * 
     * @param sender Expéditeur du message
     * @param content Contenu du message
     * @return Le message créé et enregistré
     * @throws IOException En cas d'erreur de communication avec le serveur
     */
    public static Message sendMessage(String sender, String content) throws IOException {
        Map<String, String> messageInfo = new HashMap<>();
        messageInfo.put("sender", sender);
        messageInfo.put("content", content);
        
        String requestBody = OBJECT_MAPPER.writeValueAsString(messageInfo);
        
        URL url = new URL(API_BASE_URL + "/messages");
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("POST");
        connection.setRequestProperty("Content-Type", "application/json");
        connection.setDoOutput(true);
        try (OutputStream os = connection.getOutputStream()) {
            byte[] input = requestBody.getBytes("utf-8");
            os.write(input, 0, input.length);
        }
        
        int statusCode = connection.getResponseCode();
        if (statusCode == 201) {
            try (BufferedReader br = new BufferedReader(
                    new InputStreamReader(connection.getInputStream(), "utf-8"))) {
                StringBuilder response = new StringBuilder();
                String responseLine;
                while ((responseLine = br.readLine()) != null) {
                    response.append(responseLine.trim());
                }
                
                return OBJECT_MAPPER.readValue(response.toString(), Message.class);
            }
        } else {
          
            StringBuilder errorMsg = new StringBuilder();
            try (BufferedReader br = new BufferedReader(
                    new InputStreamReader(
                            connection.getErrorStream() != null ? 
                                    connection.getErrorStream() : 
                                    connection.getInputStream(), "utf-8"))) {
                String responseLine;
                while ((responseLine = br.readLine()) != null) {
                    errorMsg.append(responseLine.trim());
                }
            }
            
            throw new IOException("Erreur d'envoi de message (" + statusCode + "): " + errorMsg.toString());
        }
    }
    
    /**
     * Récupère les messages depuis un certain timestamp
     * 
     * @param since Timestamp depuis lequel récupérer les messages
     * @return Liste des messages récupérés
     * @throws IOException En cas d'erreur de communication avec le serveur
     */
    public static List<Message> getMessages(long since) throws IOException {
        URL url = new URL(API_BASE_URL + "/messages?since=" + since);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");
        
        int statusCode = connection.getResponseCode();
        if (statusCode == 200) {
           
            try (BufferedReader br = new BufferedReader(
                    new InputStreamReader(connection.getInputStream(), "utf-8"))) {
                StringBuilder response = new StringBuilder();
                String responseLine;
                while ((responseLine = br.readLine()) != null) {
                    response.append(responseLine.trim());
                }
                
                
                String json = response.toString();
                return OBJECT_MAPPER.readValue(json, new TypeReference<List<Message>>(){});
            }
        } else {
            throw new IOException("Erreur lors de la récupération des messages: " + connection.getResponseMessage());
        }
    }
    
    /**
     * Récupère la liste des utilisateurs connectés
     * 
     * @return Liste des utilisateurs
     * @throws IOException En cas d'erreur de communication avec le serveur
     */
    public static List<User> getUsers() throws IOException {
        URL url = new URL(API_BASE_URL + "/users");
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");
        
        int statusCode = connection.getResponseCode();
        if (statusCode == 200) {
           
            try (BufferedReader br = new BufferedReader(
                    new InputStreamReader(connection.getInputStream(), "utf-8"))) {
                StringBuilder response = new StringBuilder();
                String responseLine;
                while ((responseLine = br.readLine()) != null) {
                    response.append(responseLine.trim());
                }
                
               
                String json = response.toString();
                return OBJECT_MAPPER.readValue(json, new TypeReference<List<User>>(){});
            }
        } else {
            return new ArrayList<>(); 
        }
    }
    
    /**
     * Envoie un signal de vie au serveur pour maintenir la session active
     * 
     * @param username Nom d'utilisateur à maintenir actif
     * @throws IOException En cas d'erreur de communication avec le serveur
     */
    public static void sendHeartbeat(String username) throws IOException {
        URL url = new URL(API_BASE_URL + "/users/" + username + "/heartbeat");
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("PUT");
        connection.setDoOutput(true);
        connection.getResponseCode();
    }
}
