package com.chatroom.client;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Composant graphique personnalisé pour afficher les messages sous forme de bulles style WhatsApp.
 * <p>
 * Cette classe étend JPanel pour créer un composant visuel représentant un message
 * dans une bulle de dialogue similaire à l'interface WhatsApp. Les messages de l'utilisateur
 * actuel sont affichés à droite avec un fond vert clair, tandis que les messages des autres
 * utilisateurs sont affichés à gauche avec un fond gris.
 * </p>
 * <p>
 * Chaque bulle affiche le nom de l'expéditeur (sauf pour l'utilisateur actuel), le contenu 
 * du message et l'horodatage. La bulle a des coins arrondis et une apparence moderne.
 * </p>
 *
 * @author ESP-DIC3
 * @version 1.0
 */
public class MessageBubble extends JPanel {
    private static final int ARC_SIZE = 15;
    private static final Color WHATSAPP_LIGHT_GREEN = new Color(220, 248, 198);
    private static final Color WHATSAPP_GREY = new Color(160    , 160, 160);
    private static final Color SENDER_NAME_COLOR = new Color(0, 92, 75);
    private static final Color TIME_COLOR = new Color(120, 120, 120);
    
    private String sender;
    private String content;
    private long timestamp;
    private boolean isCurrentUser;
    
    /**
     * Crée une bulle de message
     * @param sender Expéditeur du message
     * @param content Contenu du message
     * @param isCurrentUser Si l'expéditeur est l'utilisateur actuel
     */
    public MessageBubble(String sender, String content, boolean isCurrentUser) {
        this.sender = sender;
        this.content = content;
        this.timestamp = System.currentTimeMillis();
        this.isCurrentUser = isCurrentUser;
        
        setOpaque(false);
        setLayout(new BorderLayout());
        setMaximumSize(new Dimension(400, 1000));
        
        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setOpaque(false);
        
        JPanel bubblePanel = createBubblePanel();
        
        if (isCurrentUser) {
            wrapper.add(bubblePanel, BorderLayout.EAST);
        } else {
            wrapper.add(bubblePanel, BorderLayout.WEST);
        }
        
        add(wrapper, BorderLayout.CENTER);
    }
    
    /**
     * Crée le panel de la bulle avec le contenu du message
     */
    private JPanel createBubblePanel() {
        JPanel bubblePanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                g2d.setColor(isCurrentUser ? WHATSAPP_LIGHT_GREEN : WHATSAPP_GREY);
                
                RoundRectangle2D.Double shape = new RoundRectangle2D.Double(
                        0, 0, getWidth(), getHeight(), ARC_SIZE, ARC_SIZE);
                g2d.fill(shape);
                g2d.dispose();
                
                super.paintComponent(g);
            }
        };
        
        bubblePanel.setOpaque(false);
        bubblePanel.setBorder(new EmptyBorder(8, 12, 8, 12));
        bubblePanel.setLayout(new BoxLayout(bubblePanel, BoxLayout.Y_AXIS));
        
        if (!isCurrentUser) {
            JLabel senderLabel = new JLabel(sender);
            senderLabel.setFont(new Font("Arial", Font.BOLD, 12));
            senderLabel.setForeground(SENDER_NAME_COLOR);
            senderLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
            bubblePanel.add(senderLabel);
        }
        
        JTextArea contentArea = new JTextArea(content);
        contentArea.setEditable(false);
        contentArea.setWrapStyleWord(true);
        contentArea.setLineWrap(true);
        contentArea.setOpaque(false);
        contentArea.setBorder(null);
        contentArea.setFont(new Font("Arial", Font.PLAIN, 14));
        contentArea.setAlignmentX(Component.LEFT_ALIGNMENT);
        bubblePanel.add(contentArea);
        
        JLabel timeLabel = new JLabel(formatTime(timestamp));
        timeLabel.setFont(new Font("Arial", Font.PLAIN, 11));
        timeLabel.setForeground(TIME_COLOR);
        timeLabel.setAlignmentX(Component.RIGHT_ALIGNMENT);
        bubblePanel.add(timeLabel);
        
        return bubblePanel;
    }
    
    /**
     * Formate l'horodatage pour l'affichage
     */
    private String formatTime(long timestamp) {
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");
        return sdf.format(new Date(timestamp));
    }
}
