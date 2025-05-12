package com.chatroom.client;

import com.chatroom.model.Message;
import com.chatroom.model.User;
import com.chatroom.util.ApiClient;
import com.chatroom.util.LogManager;

import static com.chatroom.util.Constants.Colors.*;
import static com.chatroom.util.Constants.Timing.POLLING_INTERVAL_MS;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
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
 * <p>Cette classe gère l'interface utilisateur et utilise ApiClient pour 
 * les communications avec le serveur backend.</p>
 * 
 * @author ESP-DIC3
 * @version 1.0
 */
public class ChatGUI extends JFrame {
   
    private static final java.util.logging.Logger LOGGER = LogManager.getLogger(ChatGUI.class);
    
   
    private JPanel chatPanel;
    private JScrollPane scrollPane;
    private JTextField messageField;
    private JButton sendButton;
    private JList<String> userList;
    private DefaultListModel<String> userListModel;
    
   
    private String username;
    private long lastMessageTimestamp = 0;
    private List<Message> displayedMessages = new ArrayList<>();
    private Timer pollTimer;
    
    /**
     * Constructeur de l'interface graphique
     */
    public ChatGUI() {
        LOGGER.info("Initialisation de l'interface graphique");
        
        setTitle("WhatsApp Chat Group -ESP-DIC3");
        setSize(800, 600);
        setMinimumSize(new Dimension(600, 400));
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        
        initUI();
        
        // Gérer la fermeture pour déconnecter l'utilisateur
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
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
        
        JPanel headerPanel = createHeaderPanel();
        mainPanel.add(headerPanel, BorderLayout.NORTH);
        
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        splitPane.setDividerLocation(200);
        splitPane.setDividerSize(5);
        splitPane.setContinuousLayout(true);
        
        userListModel = new DefaultListModel<>();
        userList = new JList<>(userListModel);
        userList.setCellRenderer(new UserListCellRenderer());
        JScrollPane userScrollPane = new JScrollPane(userList);
        userScrollPane.setBorder(new EmptyBorder(5, 5, 5, 5));
        
        chatPanel = new JPanel();
        chatPanel.setLayout(new BoxLayout(chatPanel, BoxLayout.Y_AXIS));
        chatPanel.setBackground(WHATSAPP_BACKGROUND);
        
        scrollPane = new JScrollPane(chatPanel);
        scrollPane.setBorder(null);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        
        splitPane.setLeftComponent(userScrollPane);
        splitPane.setRightComponent(scrollPane);
        
        JPanel inputPanel = createInputPanel();
        
        mainPanel.add(splitPane, BorderLayout.CENTER);
        mainPanel.add(inputPanel, BorderLayout.SOUTH);
        
        setContentPane(mainPanel);
    }
    
    /**
     * Crée le panel d'en-tête avec le logo et le titre
     */
    private JPanel createHeaderPanel() {
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(WHATSAPP_GREEN);
        headerPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
        
        
        JLabel logoLabel = new JLabel("WhatsApp-GROUP-ESP-DIC3");
        logoLabel.setFont(new Font("Arial", Font.BOLD, 18));
        logoLabel.setForeground(Color.WHITE);
        
        headerPanel.add(logoLabel, BorderLayout.WEST);
        
        return headerPanel;
    }
    
    /**
     * Crée le panel de saisie des messages
     */
    private JPanel createInputPanel() {
        JPanel inputPanel = new JPanel(new BorderLayout());
        inputPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
        inputPanel.setBackground(WHATSAPP_LIGHT_GREY);
        
       
        messageField = new JTextField();
        messageField.setFont(new Font("Arial", Font.PLAIN, 14));
        messageField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(WHATSAPP_GREY, 1, true),
            BorderFactory.createEmptyBorder(8, 8, 8, 8)
        ));
        
       
        sendButton = new JButton("Envoyer") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                if (getModel().isPressed()) {
                    g2.setColor(WHATSAPP_BUTTON_HOVER.darker());
                } else if (getModel().isRollover()) {
                    g2.setColor(WHATSAPP_BUTTON_HOVER);
                } else {
                    g2.setColor(WHATSAPP_GREEN);
                }
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        sendButton.setForeground(WHATSAPP_TEXT_COLOR);
        sendButton.setFont(new Font("Arial", Font.BOLD, 14));
        sendButton.setBorder(BorderFactory.createEmptyBorder(8, 15, 8, 15));
        sendButton.setFocusPainted(false);
        sendButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        sendButton.setContentAreaFilled(false);
        sendButton.setOpaque(false); 
        
        // Effet de survol sur le bouton
        sendButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent evt) {
                sendButton.setBackground(WHATSAPP_BUTTON_HOVER);
            }
            
            @Override
            public void mouseExited(MouseEvent evt) {
                sendButton.setBackground(WHATSAPP_GREEN);
            }
        });
        
        // Action du bouton
        sendButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                sendMessage();
            }
        });
        
        // Action sur la touche Entrée
        messageField.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                sendMessage();
            }
        });
        
        // Ajouter les composants
        inputPanel.add(messageField, BorderLayout.CENTER);
        inputPanel.add(sendButton, BorderLayout.EAST);
        
        return inputPanel;
    }
    
    /**
     * Affiche la boîte de dialogue de connexion
     */
    public void showLoginDialog() {
        JDialog loginDialog = new JDialog(this, "Connexion", true);
        loginDialog.setSize(400, 200);
        loginDialog.setLocationRelativeTo(this);
        loginDialog.setResizable(false);
        
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(new EmptyBorder(20, 20, 20, 20));
        
        // Titre
        JLabel titleLabel = new JLabel("Entrez votre nom d'utilisateur");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 16));
        titleLabel.setHorizontalAlignment(JLabel.CENTER);
        titleLabel.setBorder(new EmptyBorder(0, 0, 20, 0));
        
        // Champ de texte
        JTextField usernameField = new JTextField();
        usernameField.setFont(new Font("Arial", Font.PLAIN, 14));
        usernameField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(WHATSAPP_GREY, 1, true),
            BorderFactory.createEmptyBorder(8, 8, 8, 8)
        ));
        
        // Bouton de connexion avec couleur forcée
        JButton connectButton = new JButton("Connexion") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                if (getModel().isPressed()) {
                    g2.setColor(WHATSAPP_BUTTON_HOVER.darker());
                } else if (getModel().isRollover()) {
                    g2.setColor(WHATSAPP_BUTTON_HOVER);
                } else {
                    g2.setColor(WHATSAPP_GREEN);
                }
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        connectButton.setForeground(WHATSAPP_TEXT_COLOR);
        connectButton.setFont(new Font("Arial", Font.BOLD, 14));
        connectButton.setBorder(BorderFactory.createEmptyBorder(8, 15, 8, 15));
        connectButton.setFocusPainted(false);
        connectButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        connectButton.setContentAreaFilled(false);
        connectButton.setOpaque(false);
        
        // Effet de survol
        connectButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent evt) {
                connectButton.setBackground(WHATSAPP_BUTTON_HOVER);
            }
            
            @Override
            public void mouseExited(MouseEvent evt) {
                connectButton.setBackground(WHATSAPP_GREEN);
            }
        });
        
        // Action du bouton
        connectButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String username = usernameField.getText().trim();
                if (!username.isEmpty()) {
                    try {
                        registerUser(username);
                        loginDialog.dispose();
                    } catch (IOException ex) {
                        JOptionPane.showMessageDialog(loginDialog, 
                            ex.getMessage(), 
                            "Erreur de connexion", 
                            JOptionPane.ERROR_MESSAGE);
                    }
                } else {
                    JOptionPane.showMessageDialog(loginDialog, 
                        "Veuillez entrer un nom d'utilisateur valide.", 
                        "Erreur", 
                        JOptionPane.WARNING_MESSAGE);
                }
            }
        });
        
        // Ajouter les composants
        panel.add(titleLabel, BorderLayout.NORTH);
        panel.add(usernameField, BorderLayout.CENTER);
        panel.add(connectButton, BorderLayout.SOUTH);
        
        loginDialog.setContentPane(panel);
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
            // Utiliser ApiClient pour enregistrer l'utilisateur
            ApiClient.registerUser(username);
            
            this.username = username;
            setTitle("WhatsApp Chat Group-ESP-DIC3 - " + username);
            
           
            startPolling();
        } catch (IOException e) {
            LOGGER.warning("Erreur lors de l'inscription: " + e.getMessage());
            throw e;
        }
    }
    
    /**
     * Désinscrit l'utilisateur du serveur
     */
    private void unregisterUser() {
        try {
         
            if (username != null) {
                ApiClient.sendHeartbeat(username); // Dernier signal avant désinscription
            }
            
            if (pollTimer != null) {
                pollTimer.cancel();
            }
        } catch (Exception e) {
            LOGGER.warning("Erreur lors de la désinscription: " + e.getMessage());
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
        if (!content.isEmpty()) {
            try {
               
                Message message = ApiClient.sendMessage(username, content);
                
               
                addMessage(message.getSender(), message.getContent(), message.getSender().equals(username));
                
               
                displayedMessages.add(message);
                
              
                lastMessageTimestamp = Math.max(lastMessageTimestamp, message.getTimestamp());
                
              
                messageField.setText("");
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, 
                    "Erreur d'envoi: " + e.getMessage(), 
                    "Erreur", 
                    JOptionPane.ERROR_MESSAGE);
            }
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
                }
            }
        }, 0, POLLING_INTERVAL_MS); // Polling toutes les secondes
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
        try {
          
            List<Message> messages = ApiClient.getMessages(lastMessageTimestamp);
            
            SwingUtilities.invokeLater(() -> {
                for (Message message : messages) {
                    
                    if (!isMessageDisplayed(message)) {
                       
                        addMessage(message.getSender(), message.getContent(), 
                                   message.getSender().equals(username));
                        
                       
                        displayedMessages.add(message);
                        lastMessageTimestamp = Math.max(lastMessageTimestamp, message.getTimestamp());
                    }
                }
            });
        } catch (Exception e) {
            LOGGER.warning("Erreur lors du polling des messages: " + e.getMessage());
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
        try {
            // Récupérer la liste des utilisateurs connectés
            List<User> users = ApiClient.getUsers();
            
            SwingUtilities.invokeLater(() -> {
              
                userListModel.clear();
                
             
                for (User user : users) {
                    userListModel.addElement(user.getUsername());
                }
                
                
                if (!userListModel.contains(username) && username != null) {
                    userListModel.addElement(username);
                }
            });
        } catch (Exception e) {
            LOGGER.warning("Erreur lors du polling des utilisateurs: " + e.getMessage());
        }
    }
    
    /**
     * Envoie un signal de vie au serveur pour maintenir la session active
     */
    private void sendHeartbeat() throws IOException {
        try {
            ApiClient.sendHeartbeat(username);
        } catch (Exception e) {
            LOGGER.warning("Erreur lors de l'envoi du heartbeat: " + e.getMessage());
        }
    }
    
    /**
     * Ajoute un message à l'interface
     */
    private void addMessage(String sender, String content, boolean isCurrentUser) {
        MessageBubble messageBubble = new MessageBubble(sender, content, isCurrentUser);
        
        chatPanel.add(messageBubble);
        chatPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        
        chatPanel.revalidate();
        
        SwingUtilities.invokeLater(() -> {
            JScrollBar vertical = scrollPane.getVerticalScrollBar();
            vertical.setValue(vertical.getMaximum());
        });
    }
    
    /**
     * Point d'entrée principal
     */
    public static void main(String[] args) {
       
        LogManager.configureLogging();
        
        try {
         
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
