package com.chatroom;

import com.chatroom.client.ChatGUI;
import com.chatroom.server.RestServer;
import com.chatroom.util.LogManager;
import org.glassfish.grizzly.http.server.HttpServer;

import javax.swing.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Classe principale pour lancer l'application de chat.
 * <p>
 * Cette classe est le point d'entrée principal de l'application. Elle démarre
 * le serveur REST en arrière-plan et initialise l'interface graphique du client.
 * L'application suit un modèle client-serveur simple où le serveur gère les
 * données et expose une API REST, tandis que le client fournit une interface
 * utilisateur graphique pour interagir avec le serveur.
 * </p>
 *
 * @author ESP-DIC3
 * @version 1.0
 */
public class ChatRoomApp {
    
    private static final Logger LOGGER = LogManager.getLogger(ChatRoomApp.class);

    /**
     * Démarre l'application complète (serveur + clients)
     */
    public static void main(String[] args) {
        // Configurer le dossier des logs (crée un dossier 'logs' dans le répertoire de l'application)
        LogManager.setLogDirectory("./logs");
        LogManager.setLogFileName("chatroom.log");
        LogManager.configureLogging();
        LOGGER.info("Démarrage de l'application ChatRoom");
        
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            
            Thread serverThread = new Thread(() -> {
                try {
                    final HttpServer server = RestServer.startServer();
                    LOGGER.info("Serveur REST démarré avec succès sur " + server.getListener("grizzly").getHost() + ":" + server.getListener("grizzly").getPort());
                    System.out.println("Serveur REST démarré avec succès!");
                    System.out.println("Appuyez sur Ctrl+C dans la console pour arrêter le serveur...");
                    
                    Thread.currentThread().join();
                } catch (Exception e) {
                    System.err.println("Erreur lors du démarrage du serveur: " + e.getMessage());
                    e.printStackTrace();
                }
            });
            serverThread.setDaemon(true);
            serverThread.start();
            
            System.out.println("Attente du démarrage du serveur...");
            Thread.sleep(2000);
            
            SwingUtilities.invokeLater(() -> {
                ChatGUI chatGUI = new ChatGUI();
                chatGUI.setVisible(true);
                chatGUI.showLoginDialog();
            });
            
            offerMultipleClients();
            
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Erreur d'initialisation de l'application", e);
            JOptionPane.showMessageDialog(null, 
                "Erreur lors du démarrage de l'application: " + e.getMessage(), 
                "Erreur d'initialisation", 
                JOptionPane.ERROR_MESSAGE);
        }
    }
    
    /**
     * Affiche une boîte de dialogue pour proposer de lancer plusieurs clients
     */
    private static void offerMultipleClients() {
        SwingUtilities.invokeLater(() -> {
            int response = JOptionPane.showConfirmDialog(
                    null,
                    "Voulez-vous lancer des clients supplémentaires pour simuler un groupe WhatsApp?",
                    "Simulation de groupe",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.QUESTION_MESSAGE
            );
            
            if (response == JOptionPane.YES_OPTION) {
                int nbClients = Integer.parseInt(JOptionPane.showInputDialog(
                        null,
                        "Combien de clients supplémentaires souhaitez-vous lancer? (1-3)",
                        "2"
                ));
                
                nbClients = Math.min(Math.max(nbClients, 1), 3);
                
                for (int i = 0; i < nbClients; i++) {
                    final int clientIndex = i + 1;
                    SwingUtilities.invokeLater(() -> {
                        ChatGUI chatGUI = new ChatGUI();
                        chatGUI.setLocation(100 + clientIndex * 50, 100 + clientIndex * 50);
                        chatGUI.setVisible(true);
                        chatGUI.showLoginDialog();
                    });
                    
                    try {
                        Thread.sleep(500);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                }
            }
        });
    }
}
