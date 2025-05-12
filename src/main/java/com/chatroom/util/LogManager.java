package com.chatroom.util;

import java.io.IOException;
import java.util.logging.ConsoleHandler;
import java.util.logging.FileHandler;
import java.util.logging.Formatter;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Gestionnaire de logs pour l'application ChatRoom afin de voir l'ensemble des actions effectuées par les utilisateurs
 * Fournit des logs formatés et colorés dans la console et dans des fichiers
 * recyclé dans l'un de mes projets, 
 */
public class LogManager {
    
    // Couleurs ANSI pour la console
    private static final String ANSI_RESET = "\u001B[0m";
    private static final String ANSI_RED = "\u001B[31m";
    private static final String ANSI_GREEN = "\u001B[32m";
    private static final String ANSI_YELLOW = "\u001B[33m";
    private static final String ANSI_BLUE = "\u001B[34m";
    
    // Le logger racine
    private static final Logger ROOT_LOGGER = Logger.getLogger("");
    
    // Chemin par défaut pour les fichiers de log
    private static String LOG_DIRECTORY = "./logs/";
    private static String LOG_FILE_NAME = "chatroom.log";
    
    // Formateur personnalisé pour la console (avec couleurs)
    private static class ColorConsoleFormatter extends Formatter {
        private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
        
        @Override
        public String format(LogRecord record) {
            StringBuilder sb = new StringBuilder();
            
            // Date et heure
            sb.append(dateFormat.format(new Date(record.getMillis()))).append(" ");
            
            // Niveau de log avec couleur
            Level level = record.getLevel();
            if (level == Level.SEVERE) {
                sb.append(ANSI_RED).append("ERROR").append(ANSI_RESET);
            } else if (level == Level.WARNING) {
                sb.append(ANSI_YELLOW).append("WARN").append(ANSI_RESET);
            } else if (level == Level.INFO) {
                sb.append(ANSI_GREEN).append("INFO").append(ANSI_RESET);
            } else if (level == Level.CONFIG) {
                sb.append(ANSI_BLUE).append("CONFIG").append(ANSI_RESET);
            } else {
                sb.append(level.getName());
            }
            
            // Nom de la classe et message
            sb.append(" [").append(record.getSourceClassName());
            if (record.getSourceMethodName() != null) {
                sb.append(".").append(record.getSourceMethodName());
            }
            sb.append("] ");
            
            // Message principal
            sb.append(record.getMessage());
            
            // Exception si présente
            if (record.getThrown() != null) {
                sb.append("\n").append(ANSI_RED).append("Exception: ").append(record.getThrown().toString()).append(ANSI_RESET);
                for (StackTraceElement element : record.getThrown().getStackTrace()) {
                    sb.append("\n    at ").append(element.toString());
                }
            }
            
            sb.append("\n");
            return sb.toString();
        }
    }
    
    // Formateur personnalisé pour les fichiers (sans couleurs)
    private static class FileFormatter extends Formatter {
        private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
        
        @Override
        public String format(LogRecord record) {
            StringBuilder sb = new StringBuilder();
            
            // Date et heure
            sb.append(dateFormat.format(new Date(record.getMillis()))).append(" ");
            
            // Niveau de log
            sb.append(record.getLevel().getName()).append(" ");
            
            // Nom de la classe et message
            sb.append("[").append(record.getSourceClassName());
            if (record.getSourceMethodName() != null) {
                sb.append(".").append(record.getSourceMethodName());
            }
            sb.append("] ");
            
            // Message principal
            sb.append(record.getMessage());
            
            // Exception si présente
            if (record.getThrown() != null) {
                sb.append("\nException: ").append(record.getThrown().toString());
                for (StackTraceElement element : record.getThrown().getStackTrace()) {
                    sb.append("\n    at ").append(element.toString());
                }
            }
            
            sb.append("\n");
            return sb.toString();
        }
    }
    
    /**
     * Définit le dossier où seront stockés les fichiers de log
     * @param directory Chemin du dossier pour les logs
     */
    public static void setLogDirectory(String directory) {
        if (directory != null && !directory.isEmpty()) {
            // S'assurer que le chemin se termine par un séparateur
            if (!directory.endsWith("/") && !directory.endsWith("\\")) {
                directory += "/";
            }
            LOG_DIRECTORY = directory;
        }
    }
    
    /**
     * Définit le nom du fichier de log
     * @param fileName Nom du fichier de log (sans le chemin)
     */
    public static void setLogFileName(String fileName) {
        if (fileName != null && !fileName.isEmpty()) {
            LOG_FILE_NAME = fileName;
        }
    }
    
    /**
     * Configure le système de logging pour l'application
     */
    public static void configureLogging() {
        try {
            // Supprimer les handlers par défaut
            for (Handler handler : ROOT_LOGGER.getHandlers()) {
                ROOT_LOGGER.removeHandler(handler);
            }
            
            // Définir le niveau de log global
            ROOT_LOGGER.setLevel(Level.INFO);
            
            // Créer et configurer le handler pour la console
            ConsoleHandler consoleHandler = new ConsoleHandler();
            consoleHandler.setFormatter(new ColorConsoleFormatter());
            consoleHandler.setLevel(Level.INFO);
            ROOT_LOGGER.addHandler(consoleHandler);
            
            // Créer et configurer le handler pour les fichiers
            try {
                // Créer le dossier des logs s'il n'existe pas
                java.io.File logDir = new java.io.File(LOG_DIRECTORY);
                if (!logDir.exists()) {
                    logDir.mkdirs();
                }
                
                String logFilePath = LOG_DIRECTORY + LOG_FILE_NAME;
                FileHandler fileHandler = new FileHandler(logFilePath, 5 * 1024 * 1024, 3, true);
                fileHandler.setFormatter(new FileFormatter());
                fileHandler.setLevel(Level.ALL);
                ROOT_LOGGER.addHandler(fileHandler);
                
                System.out.println("Fichiers de log créés dans: " + new java.io.File(logFilePath).getAbsolutePath());
            } catch (IOException e) {
                // Utiliser la console si impossible de créer le fichier
                System.err.println("Impossible de créer le fichier de log: " + e.getMessage());
                e.printStackTrace();
            }
            
            // Afficher un message de démarrage
            Logger.getLogger(LogManager.class.getName()).info("Système de logging configuré avec succès");
            
        } catch (Exception e) {
            System.err.println("Erreur lors de la configuration du système de logging: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Obtient un logger configuré pour la classe spécifiée
     * @param clazz La classe pour laquelle obtenir un logger
     * @return Le logger configuré
     */
    public static Logger getLogger(Class<?> clazz) {
        return Logger.getLogger(clazz.getName());
    }
    
    /**
     * Formate une exception pour l'affichage
     * @param e L'exception à formater
     * @return Une chaîne formatée contenant les détails de l'exception
     */
    public static String formatException(Throwable e) {
        StringBuilder sb = new StringBuilder();
        sb.append("Exception: ").append(e.getClass().getName());
        if (e.getMessage() != null) {
            sb.append(": ").append(e.getMessage());
        }
        sb.append("\nStack trace:");
        for (StackTraceElement element : e.getStackTrace()) {
            sb.append("\n    at ").append(element.toString());
        }
        return sb.toString();
    }
}
