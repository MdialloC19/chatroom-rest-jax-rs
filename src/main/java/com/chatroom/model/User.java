package com.chatroom.model;

import java.util.Objects;

/**
 * Représente un utilisateur connecté à la chatroom.
 * <p>
 * Cette classe stocke les informations essentielles sur un utilisateur connecté,
 * notamment son nom d'utilisateur unique et son dernier timestamp d'activité.
 * Ce timestamp est utilisé pour détecter les utilisateurs inactifs et les déconnecter
 * automatiquement après une période d'inactivité.
 * </p>
 *
 * @author ESP-DIC3
 * @version 1.0
 */
public class User {
    private String username;
    private long lastActive;

    // Constructeur par défaut nécessaire pour Jackson
    public User() {
        this.lastActive = System.currentTimeMillis();
    }

    public User(String username) {
        this.username = username;
        this.lastActive = System.currentTimeMillis();
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public long getLastActive() {
        return lastActive;
    }

    public void setLastActive(long lastActive) {
        this.lastActive = lastActive;
    }

    public void updateActivity() {
        this.lastActive = System.currentTimeMillis();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        User user = (User) o;
        return Objects.equals(username, user.username);
    }

    @Override
    public int hashCode() {
        return Objects.hash(username);
    }

    @Override
    public String toString() {
        return "User{" +
                "username='" + username + '\'' +
                ", lastActive=" + lastActive +
                '}';
    }
}
