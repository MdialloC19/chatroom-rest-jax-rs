package com.chatroom.client;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

/**
 * Renderer personnalisé pour afficher les utilisateurs dans la liste latérale avec un style WhatsApp.
 * <p>
 * Cette classe étend DefaultListCellRenderer pour personnaliser l'apparence des éléments
 * dans la liste des utilisateurs connectés. Elle utilise les mêmes couleurs et le même style
 * que l'interface générale pour maintenir une cohérence visuelle. Chaque utilisateur est
 * affiché avec une icône de profil, son nom, et un style de sélection approprié.
 * </p>
 *
 * @author ESP-DIC3
 * @version 1.0
 */
public class UserListCellRenderer extends DefaultListCellRenderer {
    private static final Color WHATSAPP_GREEN = new Color(18, 140, 126);
    private static final Color SELECTED_COLOR = new Color(229, 242, 255);
    
    @Override
    public Component getListCellRendererComponent(JList<?> list, Object value, 
            int index, boolean isSelected, boolean cellHasFocus) {
        
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(new EmptyBorder(10, 10, 10, 10));
        
        if (isSelected) {
            panel.setBackground(SELECTED_COLOR);
        } else {
            panel.setBackground(Color.WHITE);
        }
        
        JLabel avatarLabel = createAvatarLabel(value.toString());
        
        JPanel infoPanel = new JPanel(new GridLayout(2, 1));
        infoPanel.setOpaque(false);
        
        JLabel nameLabel = new JLabel(value.toString());
        nameLabel.setFont(new Font("Arial", Font.BOLD, 14));
        
        JLabel statusLabel = new JLabel("En ligne");
        statusLabel.setFont(new Font("Arial", Font.PLAIN, 12));
        statusLabel.setForeground(Color.GRAY);
        
        infoPanel.add(nameLabel);
        infoPanel.add(statusLabel);
        
        panel.add(avatarLabel, BorderLayout.WEST);
        panel.add(infoPanel, BorderLayout.CENTER);
        
        return panel;
    }
    
    /**
     * Crée un label pour l'avatar de l'utilisateur
     */
    private JLabel createAvatarLabel(String username) {
        JLabel avatarLabel = new JLabel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                g2.setColor(WHATSAPP_GREEN);
                g2.fillOval(0, 0, getWidth(), getHeight());
                
                g2.setColor(Color.WHITE);
                g2.setFont(new Font("Arial", Font.BOLD, 16));
                
                String initial = username.length() > 0 ? username.substring(0, 1).toUpperCase() : "?";
                FontMetrics fm = g2.getFontMetrics();
                int x = (getWidth() - fm.stringWidth(initial)) / 2;
                int y = (getHeight() + fm.getAscent()) / 2;
                
                g2.drawString(initial, x, y);
                g2.dispose();
            }
        };
        
        avatarLabel.setPreferredSize(new Dimension(40, 40));
        avatarLabel.setMinimumSize(new Dimension(40, 40));
        avatarLabel.setMaximumSize(new Dimension(40, 40));
        avatarLabel.setBorder(new EmptyBorder(0, 0, 0, 10));
        
        return avatarLabel;
    }
}
