package com.chatroom.client;

import com.chatroom.model.Message;
import com.chatroom.model.User;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.ArrayList;
import java.util.List;

/**
 * Utilitaire pour la conversion JSON
 */
public class JsonUtils {
    private static final ObjectMapper mapper = new ObjectMapper();

    /**
     * Convertit une chaîne JSON en liste de messages
     */
    public static List<Message> parseMessageList(String json) {
        try {
            return mapper.readValue(json, new TypeReference<List<Message>>() {});
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    /**
     * Convertit une chaîne JSON en liste d'utilisateurs
     */
    public static List<User> parseUserList(String json) {
        try {
            return mapper.readValue(json, new TypeReference<List<User>>() {});
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    /**
     * Convertit un objet en chaîne JSON
     */
    public static String toJson(Object object) {
        try {
            return mapper.writeValueAsString(object);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            return "{}";
        }
    }
}
