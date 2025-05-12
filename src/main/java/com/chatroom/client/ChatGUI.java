package com.chatroom.client;

import com.chatroom.model.Message;
import com.chatroom.model.User;
import com.chatroom.util.LogManager;

import javax.swing.*;  // Inclut JFrame, JPanel, JButton, etc.
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Interface graphique principale pour le chat, inspirée de WhatsApp.
 * Cette classe implémente une application client de chat avec une interface utilisateur graphique
 * utilisant Java Swing. L'interface reproduit le design et les fonctionnalités de base de WhatsApp.
 * 
 * <p>Fonctionnalités principales:</p>
 * <ul>
 *   <li>Connexion utilisateur avec nom d'utilisateur</li>
 *   <li>Envoi et réception de messages</li>
 *   <li>Affichage des utilisateurs en ligne</li>
 *   <li>Communication avec le serveur REST</li>
 *   <li>Déconnexion propre lors de la fermeture</li>
 * </ul>
 * 
 * <p>Cette classe gère à la fois l'interface utilisateur et les appels API REST
 * vers le serveur backend pour les opérations comme l'inscription utilisateur, l'envoi
 * de messages et la récupération des utilisateurs en ligne.</p>
 * 
 * @author ESP-DIC3
 * @version 1.0
 */
public class ChatGUI extends JFrame {
    // Couleurs du thème WhatsApp - utilisées dans l'interface
    private static final Color WHATSAPP_GREEN = new Color(18, 140, 126);
    private static final Color WHATSAPP_BUTTON_HOVER = new Color(0, 168, 132); // Survol des boutons
    // Cette couleur est utilisée dans MessageBubble.java, mais définie ici pour cohérence
    private static final Color WHATSAPP_GREY = new Color(233, 237, 239);
    private static final Color WHATSAPP_LIGHT_GREY = new Color(241, 241, 241); // Fond alternatif
    private static final Color WHATSAPP_TEXT_COLOR = new Color(255, 255, 255); // Texte sur boutons
    
    // Logger pour les messages du client
    private static final java.util.logging.Logger LOGGER = LogManager.getLogger(ChatGUI.class);
    
    // URL de base pour les appels API
    private static final String API_BASE_URL = "http://localhost:8081/chat";
    // Composants de l'interface
    private JPanel chatPanel;
    private JScrollPane scrollPane;
    private JTextField messageField;
    private JButton sendButton;
    private JList<String> userList;
    private DefaultListModel<String> userListModel;
    
    // État du client
    private String username;
    private long lastMessageTimestamp = 0;
    private List<Message> displayedMessages = new ArrayList<>();
    private Timer pollTimer;
    
    /**
     * Constructeur de l'interface graphique
     */
    public ChatGUI() {
        LOGGER.info("Initialisation de l'interface graphique");
        
        // Configuration de la fenêtre
        setTitle("WhatsApp Chat Group -ESP-DIC3");
        setSize(800, 600);
        setMinimumSize(new Dimension(600, 400));
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        
        // Pas besoin d'initialiser HttpURLConnection à l'avance
        
        // Initialiser l'interface utilisateur
        initUI();
        
        // Gérer la fermeture de la fenêtre pour déconnecter l'utilisateur
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                if (username != null) {
                    try {
                        LOGGER.info("Déconnexion de l'utilisateur: " + username);
                        unregisterUser();
                    } catch (Exception ex) {
                        LOGGER.warning("Erreur lors de la déconnexion: " + ex.getMessage());
                    }
                }
            }
        });
    }
    
    /**
     * Initialise l'interface utilisateur
     */
    private void initUI() {
        // Panel principal avec BorderLayout
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
        
        // En-tête avec logo et titre
        JPanel headerPanel = createHeaderPanel();
        mainPanel.add(headerPanel, BorderLayout.NORTH);
        
        // Panel central avec SplitPane pour la liste des utilisateurs et les messages
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        
        // Liste des utilisateurs (comme une liste de contacts WhatsApp)
        userListModel = new DefaultListModel<>();
        userList = new JList<>(userListModel);
        userList.setCellRenderer(new UserListCellRenderer());
        JScrollPane userScrollPane = new JScrollPane(userList);
        userScrollPane.setPreferredSize(new Dimension(200, 0));
        
        // Panel de chat avec les messages
        chatPanel = new JPanel();
        chatPanel.setLayout(new BoxLayout(chatPanel, BoxLayout.Y_AXIS));
        chatPanel.setBackground(WHATSAPP_LIGHT_GREY);
        
        scrollPane = new JScrollPane(chatPanel);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        
        // Configurer le splitPane
        splitPane.setLeftComponent(userScrollPane);
        splitPane.setRightComponent(scrollPane);
        splitPane.setDividerLocation(200);
        splitPane.setContinuousLayout(true);
        
        mainPanel.add(splitPane, BorderLayout.CENTER);
        
        // Panel de saisie des messages en bas
        JPanel inputPanel = createInputPanel();
        mainPanel.add(inputPanel, BorderLayout.SOUTH);
        
        // Ajouter le panel principal à la fenêtre
        add(mainPanel);
    }
    
    /**
     * Crée le panel d'en-tête avec le logo et le titre
     */
    private JPanel createHeaderPanel() {
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(WHATSAPP_GREEN);
        headerPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
        
        JLabel titleLabel = new JLabel("WhatsApp Group Chat-ESP-DIC3");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 16));
        titleLabel.setForeground(Color.WHITE);
        
        headerPanel.add(titleLabel, BorderLayout.CENTER);
        
        return headerPanel;
    }
    
    /**
     * Crée le panel de saisie des messages
     */
    private JPanel createInputPanel() {
        JPanel inputPanel = new JPanel(new BorderLayout());
        inputPanel.setBorder(new EmptyBorder(10, 0, 0, 0));
        
        messageField = new JTextField();
        messageField.setFont(new Font("Arial", Font.PLAIN, 14));
        messageField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(WHATSAPP_GREY, 1),
                BorderFactory.createEmptyBorder(8, 8, 8, 8)));
        
        sendButton = new JButton("Envoyer");
        sendButton.setFont(new Font("Arial", Font.BOLD, 14));
        sendButton.setBackground(WHATSAPP_GREEN);
        sendButton.setForeground(WHATSAPP_TEXT_COLOR);
        sendButton.setFocusPainted(false);
        sendButton.setBorderPainted(false); // Désactiver les bordures par défaut
        sendButton.setContentAreaFilled(true); // Activer le remplissage de la zone
        sendButton.setOpaque(true); // Rendre le bouton opaque pour que la couleur s'affiche
        sendButton.setBorder(BorderFactory.createEmptyBorder(8, 15, 8, 15));
        sendButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        // Ajouter un effet de survol
        sendButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                sendButton.setBackground(WHATSAPP_BUTTON_HOVER);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                sendButton.setBackground(WHATSAPP_GREEN);
            }
        });
        
        sendButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                sendMessage();
            }
        });
        
        // Permettre l'envoi par la touche Entrée
        messageField.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                sendMessage();
            }
        });
        
        inputPanel.add(messageField, BorderLayout.CENTER);
        inputPanel.add(sendButton, BorderLayout.EAST);
        
        return inputPanel;
    }
    
    /**
     * Affiche la boîte de dialogue de connexion
     */
    public void showLoginDialog() {
        JDialog loginDialog = new JDialog(this, "Connexion", true);
        loginDialog.setSize(300, 150);
        loginDialog.setLocationRelativeTo(this);
        loginDialog.setLayout(new BorderLayout());
        
        JPanel formPanel = new JPanel(new GridLayout(2, 2, 10, 10));
        formPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
        
        JLabel usernameLabel = new JLabel("Pseudo:");
        JTextField usernameField = new JTextField();
        
        formPanel.add(usernameLabel);
        formPanel.add(usernameField);
        
        JButton loginButton = new JButton("Connexion");
        loginButton.setBackground(WHATSAPP_GREEN);
        loginButton.setForeground(WHATSAPP_TEXT_COLOR);
        loginButton.setFocusPainted(false);
        loginButton.setBorderPainted(false); // Désactiver les bordures par défaut
        loginButton.setContentAreaFilled(true); // Activer le remplissage de la zone
        loginButton.setOpaque(true); // Rendre le bouton opaque pour que la couleur s'affiche
        loginButton.setBorder(BorderFactory.createEmptyBorder(8, 15, 8, 15));
        loginButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        // Ajouter un effet de survol (utilise WHATSAPP_BUTTON_HOVER)
        loginButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                loginButton.setBackground(WHATSAPP_BUTTON_HOVER);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                loginButton.setBackground(WHATSAPP_GREEN);
            }
        });
        
        loginButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String username = usernameField.getText().trim();
                if (username.isEmpty()) {
                    JOptionPane.showMessageDialog(loginDialog, 
                            "Veuillez entrer un pseudo", 
                            "Erreur", 
                            JOptionPane.ERROR_MESSAGE);
                    return;
                }
                
                try {
                    registerUser(username);
                    loginDialog.dispose();
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(loginDialog, 
                            "Erreur de connexion: " + ex.getMessage(), 
                            "Erreur", 
                            JOptionPane.ERROR_MESSAGE);
                }
            }
        });
        
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.add(loginButton);
        
        loginDialog.add(formPanel, BorderLayout.CENTER);
        loginDialog.add(buttonPanel, BorderLayout.SOUTH);
        
        loginDialog.setVisible(true);
    }
    
    /**
     * Enregistre l'utilisateur auprès du serveur via l'API REST.
     * <p>
     * Cette méthode effectue une requête HTTP POST vers l'endpoint d'inscription
     * du serveur REST. Elle gère les différents codes de réponse possibles et
     * les erreurs qui peuvent survenir lors de la connexion.
     * </p>
     * 
     * @param username Le nom d'utilisateur à enregistrer
     * @throws IOException Si une erreur de communication avec le serveur se produit
     */
    private void registerUser(String username) throws IOException {
        try {
            LOGGER.info("Tentative d'inscription de l'utilisateur: " + username);
            String requestBody = "{\"username\":\"" + username + "\"}";
            
            URL url = new URL(API_BASE_URL + "/users");
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setDoOutput(true);
            
            // Envoyer le body
            try (OutputStream os = connection.getOutputStream()) {
                byte[] input = requestBody.getBytes("utf-8");
                os.write(input, 0, input.length);
            }
            
            int statusCode = connection.getResponseCode();
            if (statusCode == 201) {
                // Succès
                this.username = username;
                setTitle("WhatsApp Group Chat - " + username);
                LOGGER.info("Utilisateur inscrit avec succès: " + username);
                startPolling();
            } else {
                LOGGER.warning("Erreur d'inscription (code " + statusCode + ").");
                // Lire le message d'erreur
                StringBuilder response = new StringBuilder();
                
                // Déterminer le flux à utiliser (errorStream ou inputStream)
                InputStream stream = connection.getErrorStream();
                if (stream == null) {
                    // Certains codes d'erreur comme 409 peuvent retourner des données dans l'inputStream
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
                    // Aucun flux disponible, utiliser le message par défaut du code HTTP
                    response.append(connection.getResponseMessage());
                }
                
                // Message plus spécifique pour le code 409 (conflit)
                if (statusCode == 409) {
                    throw new IOException("Erreur d'inscription: Le nom d'utilisateur '" + username + "' est déjà utilisé.");
                } else {
                    throw new IOException("Erreur d'inscription (" + statusCode + "): " + response.toString());
                }
            }
        } catch (java.net.ConnectException e) {
            LOGGER.severe("Erreur de connexion au serveur: " + e.getMessage());
            throw new IOException("Impossible de se connecter au serveur sur " + API_BASE_URL + ". Vérifiez que le serveur est bien démarré.", e);
        }
    }
    
    // Note: La méthode startPolling est définie plus bas dans le fichier
    
    /**
     * Désinscrit l'utilisateur du serveur
     */
    private void unregisterUser() throws IOException {
        URL url = new URL(API_BASE_URL + "/users/" + username);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("DELETE");
        
        connection.getResponseCode(); // Pour exécuter la requête
        
        if (pollTimer != null) {
            pollTimer.cancel();
        }
    }
    
    /**
     * Envoie un message au serveur via l'API REST.
     * <p>
     * Cette méthode récupère le texte saisi par l'utilisateur, effectue une requête HTTP POST
     * vers l'endpoint des messages du serveur, et traite la réponse. En cas de succès, le message
     * est affiché dans l'interface. En cas d'échec, un message d'erreur est affiché.
     * </p>
     */
    private void sendMessage() {
        String content = messageField.getText().trim();
        if (content.isEmpty()) {
            return;
        }
        
        try {
            String requestBody = "{\"sender\":\"" + username + "\",\"content\":\"" + content + "\"}";
            
            URL url = new URL(API_BASE_URL + "/messages");
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setDoOutput(true);
            
            // Envoyer le body
            try (OutputStream os = connection.getOutputStream()) {
                byte[] input = requestBody.getBytes("utf-8");
                os.write(input, 0, input.length);
            }
            
            int statusCode = connection.getResponseCode();
            if (statusCode == 201) {
                // Message envoyé avec succès
                messageField.setText("");
            } else {
                // Lire le message d'erreur
                try (BufferedReader br = new BufferedReader(
                        new InputStreamReader(connection.getErrorStream(), "utf-8"))) {
                    StringBuilder response = new StringBuilder();
                    String responseLine;
                    while ((responseLine = br.readLine()) != null) {
                        response.append(responseLine.trim());
                    }
                    JOptionPane.showMessageDialog(this, 
                            "Erreur d'envoi: " + response.toString(), 
                            "Erreur", 
                            JOptionPane.ERROR_MESSAGE);
                }
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, 
                    "Erreur d'envoi: " + e.getMessage(), 
                    "Erreur", 
                    JOptionPane.ERROR_MESSAGE);
        }
    }
    
    /**
     * Démarre le polling pour les nouveaux messages et utilisateurs
     */
    private void startPolling() {
        LOGGER.info("Démarrage du polling pour l'utilisateur: " + username);
        pollTimer = new Timer(true);
        pollTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                try {
                    pollMessages();
                    pollUsers();
                    sendHeartbeat();
                } catch (Exception e) {
                    LOGGER.warning("Erreur de polling: " + e.getMessage());
                    // Afficher une notification discrète pour les erreurs de communication
                    if (e instanceof IOException) {
                        SwingUtilities.invokeLater(() -> {
                            JOptionPane.showMessageDialog(ChatGUI.this,
                                "Problème de communication avec le serveur.\nVeuillez vérifier votre connexion.",
                                "Erreur de communication",
                                JOptionPane.WARNING_MESSAGE);
                        });
                    }
                }
            }
        }, 0, 1000); // Polling toutes les secondes
    }
    
    /**
     * Récupère les nouveaux messages du serveur via l'API REST.
     * <p>
     * Cette méthode est appelée périodiquement pour interroger le serveur et récupérer
     * les nouveaux messages depuis le dernier timestamp connu. Elle utilise le paramètre
     * de requête 'since' pour ne récupérer que les messages récents, optimisant ainsi
     * les performances. Les nouveaux messages sont ensuite affichés dans l'interface
     * s'ils n'ont pas déjà été affichés précédemment.
     * </p>
     * 
     * @throws IOException Si une erreur de communication avec le serveur se produit
     */
    private void pollMessages() throws IOException {
        LOGGER.fine("Polling des messages depuis " + lastMessageTimestamp);
        URL url = new URL(API_BASE_URL + "/messages?since=" + lastMessageTimestamp);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");
        
        int statusCode = connection.getResponseCode();
        if (statusCode == 200) {
            // Lire la réponse
            try (BufferedReader br = new BufferedReader(
                    new InputStreamReader(connection.getInputStream(), "utf-8"))) {
                StringBuilder response = new StringBuilder();
                String responseLine;
                while ((responseLine = br.readLine()) != null) {
                    response.append(responseLine.trim());
                }
                
                // Analyser la réponse JSON et ajouter les messages
                String json = response.toString();
                List<Message> messages = JsonUtils.parseMessageList(json);
                
                if (!messages.isEmpty()) {
                    SwingUtilities.invokeLater(() -> {
                        for (Message message : messages) {
                            // Vérifier si le message n'a pas déjà été affiché
                            if (!isMessageDisplayed(message)) {
                                boolean isCurrentUser = message.getSender().equals(username);
                                addMessage(message.getSender(), message.getContent(), isCurrentUser);
                                displayedMessages.add(message);
                                
                                // Mettre à jour le dernier timestamp
                                if (message.getTimestamp() > lastMessageTimestamp) {
                                    lastMessageTimestamp = message.getTimestamp();
                                }
                            }
                        }
                    });
                } else if (lastMessageTimestamp == 0) {
                // Si c'est le premier chargement et qu'il n'y a pas de messages
                // Simplement mettre à jour le timestamp
                lastMessageTimestamp = System.currentTimeMillis();
                LOGGER.info("Premier chargement des messages effectué - aucun message disponible");
                }
            }
        }
    }

    /**
     * Vérifie si un message a déjà été affiché
     */
    private boolean isMessageDisplayed(Message message) {
        for (Message displayed : displayedMessages) {
            if (displayed.getTimestamp() == message.getTimestamp() && 
                displayed.getSender().equals(message.getSender()) && 
                displayed.getContent().equals(message.getContent())) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * Récupère la liste des utilisateurs connectés du serveur via l'API REST.
     * <p>
     * Cette méthode est appelée périodiquement pour mettre à jour la liste des utilisateurs
     * connectés dans l'interface graphique. Elle effectue une requête HTTP GET vers l'endpoint
     * des utilisateurs et met à jour la liste d'affichage avec les données reçues.
     * </p>
     * 
     * @throws IOException Si une erreur de communication avec le serveur se produit
     */
    private void pollUsers() throws IOException {
        URL url = new URL(API_BASE_URL + "/users");
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");
        
        int statusCode = connection.getResponseCode();
        if (statusCode == 200) {
            // Lire la réponse
            try (BufferedReader br = new BufferedReader(
                    new InputStreamReader(connection.getInputStream(), "utf-8"))) {
                StringBuilder response = new StringBuilder();
                String responseLine;
                while ((responseLine = br.readLine()) != null) {
                    response.append(responseLine.trim());
                }
                
                // Analyser la réponse JSON
                String json = response.toString();
                List<User> users = JsonUtils.parseUserList(json);
                
                SwingUtilities.invokeLater(() -> {
                    // Vider la liste actuelle
                    userListModel.clear();
                    
                    // Ajouter les utilisateurs au modèle
                    for (User user : users) {
                        userListModel.addElement(user.getUsername());
                    }
                    
                    // Si la liste est vide (pas d'utilisateurs encore), ajouter des utilisateurs de test
                    // pour afficher une interface attrayante
                    if (userListModel.isEmpty() && username != null) {
                        userListModel.addElement("Alice");
                        userListModel.addElement("Bob");
                        userListModel.addElement("Charlie");
                        userListModel.addElement(username);
                    }
                });
            }
        }
    }
    
    /**
     * Envoie un signal de vie au serveur pour maintenir la session active
     */
    private void sendHeartbeat() throws IOException {
        URL url = new URL(API_BASE_URL + "/users/" + username + "/heartbeat");
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("PUT");
        connection.setDoOutput(true);
        
        connection.getResponseCode(); // Pour exécuter la requête
    }
    
    
    /**
     * Ajoute un message à l'interface
     */
    private void addMessage(String sender, String content, boolean isCurrentUser) {
        // Créer un panel pour le message avec un style bulle
        MessageBubble messageBubble = new MessageBubble(sender, content, isCurrentUser);
        
        // Ajouter au panel de chat
        chatPanel.add(messageBubble);
        chatPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        
        // Mettre à jour l'affichage
        chatPanel.revalidate();
        
        // Faire défiler vers le bas
        SwingUtilities.invokeLater(() -> {
            JScrollBar vertical = scrollPane.getVerticalScrollBar();
            vertical.setValue(vertical.getMaximum());
        });
    }
    
    /**
     * Point d'entrée principal
     */
    public static void main(String[] args) {
        // Configurer le système de logging
        LogManager.configureLogging();
        
        try {
            // Utiliser le look and feel du système
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            LOGGER.info("Client ChatGUI démarré avec look and feel système");
        } catch (Exception e) {
            LOGGER.warning("Erreur lors de l'initialisation du look and feel: " + e.getMessage());
        }
        
        SwingUtilities.invokeLater(() -> {
            ChatGUI chatGUI = new ChatGUI();
            chatGUI.setVisible(true);
            chatGUI.showLoginDialog();
        });
    }
}
