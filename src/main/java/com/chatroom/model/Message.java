package com.chatroom.model;

/**
 * Représente un message dans la chatroom.
 * <p>
 * Cette classe est utilisée pour stocker les informations relatives à un message 
 * envoyé dans la chatroom, notamment son expéditeur, son contenu et son timestamp.
 * Elle est sérialisée/désérialisée en JSON pour les communications REST.
 * </p>
 *
 * @author ESP-DIC3
 * @version 1.0
 */
public class Message {
    private String sender;
    private String content;
    private long timestamp;

    // Constructeur par défaut nécessaire pour Jackson
    public Message() {
        this.timestamp = System.currentTimeMillis();
    }

    public Message(String sender, String content) {
        this.sender = sender;
        this.content = content;
        this.timestamp = System.currentTimeMillis();
    }

    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    @Override
    public String toString() {
        return "Message{" +
                "sender='" + sender + '\'' +
                ", content='" + content + '\'' +
                ", timestamp=" + timestamp +
                '}';
    }
}
