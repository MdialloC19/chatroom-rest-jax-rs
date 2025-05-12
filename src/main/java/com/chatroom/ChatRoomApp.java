package com.chatroom;

import com.chatroom.client.ChatGUI;
import com.chatroom.server.RestServer;
import com.chatroom.util.LogManager;
import org.glassfish.grizzly.http.server.HttpServer;

import javax.swing.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Classe principale pour lancer l'application de chat
 * Démarre le serveur REST et plusieurs clients pour simuler un groupe WhatsApp
 */
public class ChatRoomApp {
    
    private static final Logger LOGGER = LogManager.getLogger(ChatRoomApp.class);

    /**
     * Démarre l'application complète (serveur + clients)
     */
    public static void main(String[] args) {
        // Configurer le système de logging
        LogManager.configureLogging();
        LOGGER.info("Démarrage de l'application ChatRoom");
        
        try {
            // Configurer le look and feel
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            
            // Démarrer le serveur en arrière-plan
            Thread serverThread = new Thread(() -> {
                try {
                    // Démarrer le serveur et le garder actif
                    final HttpServer server = RestServer.startServer();
                    LOGGER.info("Serveur REST démarré avec succès sur " + server.getListener("grizzly").getHost() + ":" + server.getListener("grizzly").getPort());
                    System.out.println("Serveur REST démarré avec succès!");
                    System.out.println("Appuyez sur Ctrl+C dans la console pour arrêter le serveur...");
                    
                    // Attendre indéfiniment (le programme sera arrêté via Ctrl+C)
                    Thread.currentThread().join();
                } catch (Exception e) {
                    System.err.println("Erreur lors du démarrage du serveur: " + e.getMessage());
                    e.printStackTrace();
                }
            });
            serverThread.setDaemon(true); // Cela permettra d'arrêter le thread serveur quand l'application se termine
            serverThread.start();
            
            // Attendre que le serveur démarre complètement
            System.out.println("Attente du démarrage du serveur...");
            Thread.sleep(5000); // Attendre 5 secondes pour s'assurer que le serveur est prêt
            
            // Lancer le client principal
            SwingUtilities.invokeLater(() -> {
                ChatGUI chatGUI = new ChatGUI();
                chatGUI.setVisible(true);
                chatGUI.showLoginDialog();
            });
            
            // Option pour lancer plusieurs clients (pour simuler un groupe)
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
                
                // Limiter entre 1 et 3 clients additionnels
                nbClients = Math.min(Math.max(nbClients, 1), 3);
                
                // Lancer les clients additionnels
                for (int i = 0; i < nbClients; i++) {
                    final int clientIndex = i + 1;
                    SwingUtilities.invokeLater(() -> {
                        ChatGUI chatGUI = new ChatGUI();
                        // Positionner les fenêtres en cascade
                        chatGUI.setLocation(100 + clientIndex * 50, 100 + clientIndex * 50);
                        chatGUI.setVisible(true);
                        chatGUI.showLoginDialog();
                    });
                    
                    // Petit délai entre chaque lancement
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
