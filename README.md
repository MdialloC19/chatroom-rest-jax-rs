# Chatroom REST avec JAX-RS

Une application de chatroom en temps réel utilisant une API RESTful avec JAX-RS et une interface graphique Java Swing style WhatsApp. Cette application permet aux utilisateurs de s'enregistrer, d'envoyer des messages et de voir en temps réel les autres utilisateurs connectés.

## Structure du projet

```
chatroom-rest/
│── compile_run.sh         # Script de compilation et démarrage
│── src/
    └── main/
        └── java/
            └── com/
                └── chatroom/
                    │── ChatRoomApp.java  # Point d'entrée de l'application
                    │── model/            # Classes de modèle de données
                    │   │── User.java
                    │   │── Message.java
                    │   └── ChatManager.java
                    │── rest/             # Services REST
                    │   │── ChatResource.java
                    │   │── ChatApplication.java
                    │   └── CORSFilter.java
                    │── server/           # Serveur d'application
                    │   └── RestServer.java
                    │── client/           # Client avec interface graphique
                    │   │── ChatGUI.java
                    │   └── UserListCellRenderer.java
                    └── util/             # Utilitaires
                        └── LogManager.java
├── pom.xml                 # Configuration Maven et dépendances
├── target/
├── logs/                # Dossier de logs configurable
└── ...
```

## Dépendances principales

- JAX-RS API 2.1.1 - API pour les services RESTful
- Jersey 2.35 - Implémentation de référence de JAX-RS
- Grizzly - Serveur HTTP léger et performant
- Jackson - Bibliothèque pour la manipulation de JSON

## Architecture du projet

Le projet est construit selon une architecture client-serveur avec une API REST, suivant les principes suivants :

### Améliorations récentes

1. **Amélioration de l'UI :**
   - Adoption d'un style WhatsApp avec couleur verte caractéristique (`WHATSAPP_GREEN`)
   - Personnalisation des boutons avec rendu graphique optimisé
   - Suppression des utilisateurs fictifs de test

2. **Organisation du code :**
   - Création de la classe `Constants` pour centraliser les paramètres de configuration
   - Mise en place de la classe `ApiClient` pour toutes les interactions avec le serveur
   - Nettoyage des commentaires superflus pour un code plus lisible

3. **Gestion des logs :**
   - Configuration du dossier des logs personnalisable
   - Amélioration du formatage des logs avec des couleurs en console

### Architecture générale

```
+-------------------+        REST API       +-------------------+
|                   |  <---------------->  |                   |
|  Client (GUI)     |     HTTP/JSON        |  Serveur REST     |
|  Java Swing       |                      |  JAX-RS + Grizzly  |
|                   |                      |                   |
+-------------------+                      +-------------------+
                                                  |
                                                  |
                                           +-------------------+
                                           |                   |
                                           |  Modèles de données |
                                           |  Gestion état     |
                                           |                   |
                                           +-------------------+
```

### Pattern architecturaux utilisés

1. **Modèle-Vue-Contrôleur (MVC)**
   - **Modèle** : Classes dans `com.chatroom.model` (User, Message, ChatManager)
   - **Vue** : Interface graphique dans `com.chatroom.client` (ChatGUI, MessageBubble)
   - **Contrôleur** : Services REST dans `com.chatroom.rest` (ChatResource)

2. **Client-Serveur**
   - Communication via API REST
   - Séparation claire des responsabilités

3. **Singleton**
   - Utilisé pour ChatManager pour maintenir l'état global du chat

4. **Factory**
   - Création d'objets pour les utilisateurs et messages

### Couches logicielles

1. **Couche présentation** (Frontend)
   - Interface graphique Swing qui imite WhatsApp avec la couleur verte authentique
   - Composants graphiques personnalisés (MessageBubble, UserListCellRenderer)
   - Affichage des bulles de messages avec distinction utilisateur/autres

2. **Couche services** (API)
   - Endpoints REST (ChatResource)
   - Configuration JAX-RS (ChatApplication)
   - Sécurité CORS (CORSFilter)

3. **Couche métier** (Backend)
   - Gestion des utilisateurs et messages (ChatManager)
   - Modèles de données (User, Message)

4. **Couche infrastructure**
   - Serveur HTTP Grizzly (RestServer)
   - Utilitaires (LogManager, ApiClient, Constants)
   - Gestion configurable des logs

## Exécution du projet

Le projet peut être exécuté de deux façons :

1. **Compilation et exécution avec le script :**
   ```
   ./compile_run.sh
   ```

2. **Avec Maven :**
   ```
   mvn clean compile exec:java
   ```

Une fois l'application démarrée, le serveur REST s'initialise automatiquement et l'interface graphique s'ouvre. Vous pouvez alors créer plusieurs instances du client pour simuler une conversation de groupe.

## Fonctionnalités implémentées

### Côté serveur

- Gestion des utilisateurs (connexion, déconnexion, heartbeat)
- Envoi et réception de messages
- Support du format JSON
- Support CORS pour les clients web
- Nettoyage automatique des utilisateurs inactifs

### Côté client

- Interface graphique inspirée de WhatsApp
- Affichage en temps réel des utilisateurs connectés
- Envoi et réception de messages avec bulles stylisées
- Auto-déconnexion propre à la fermeture de l'application
- Nettoyage automatique des utilisateurs inactifs

## Démarrage manuel de l'application

Le projet utilise un script shell pour la compilation et l'exécution sans avoir besoin de Maven avec les IDE souvent les serveurs refuse de démarré correctement.  Donc soit on fias une compilation avec le terminal avec la commande :

```bash
javac -d target src/main/java/com/chatroom/*.java src/main/java/com/chatroom/model/*.java src/main/java/com/chatroom/rest/*.java src/main/java/com/chatroom/server/*.java src/main/java/com/chatroom/client/*.java src/main/java/com/chatroom/util/*.java Pour démarrer l'application manuellement :
```

puis on peut démarrer l'application avec la commande : 

```bash
java -cp target com.chatroom.ChatRoomApp
```

ou bien soit on peut utiliser le script compile_run.sh pour compiler et démarrer l'application

```bash
# Rendre le script exécutable (si ce n'est pas déjà fait)
chmod +x compile_run.sh

# Exécuter le script pour compiler et démarrer l'application
./compile_run.sh
```

Le script compile_run.sh :
1. Télécharge automatiquement les dépendances nécessaires
2. Compile le code source
3. Démarre le serveur REST sur le port 8081
4. Lance l'interface graphique client

## Endpoints REST disponibles

- `GET /chat/users` - Récupérer la liste des utilisateurs connectés
- `POST /chat/users` - Inscrire un nouvel utilisateur
- `DELETE /chat/users/{username}` - Déconnecter un utilisateur
- `PUT /chat/users/{username}/heartbeat` - Garder un utilisateur actif
- `GET /chat/messages` - Récupérer les messages (avec paramètre optionnel `since`)
- `POST /chat/messages` - Envoyer un nouveau message

## Tester le serveur REST manuellement

Le serveur démarre sur http://localhost:8081/chat

### Exemples de requêtes avec cURL

1. **Inscrire un utilisateur**
```bash
curl -X POST -H "Content-Type: application/json" -d '{"username":"alice"}' http://localhost:8081/chat/users
```

2. **Récupérer la liste des utilisateurs**
```bash
curl -X GET http://localhost:8081/chat/users
```

3. **Envoyer un message**
```bash
curl -X POST -H "Content-Type: application/json" -d '{"sender":"alice","content":"Bonjour tout le monde!"}' http://localhost:8081/chat/messages
```

4. **Récupérer tous les messages**
```bash
curl -X GET http://localhost:8081/chat/messages
```

## Utilisation de l'interface graphique

L'application inclut une interface graphique inspirée de WhatsApp :

1. Démarrez l'application avec `./compile_run.sh`
2. L'application démarrera le serveur et le client GUI automatiquement
3. Saisissez votre nom d'utilisateur pour vous connecter
4. Vous pouvez maintenant envoyer et recevoir des messages

## Prochaines étapes

- Ajouter des fonctionnalités comme les messages privés
- Ajouter des notifications en temps réel avec Server-Sent Events (SSE)
- Améliorer la persistance des messages et des utilisateurs
- Ajouter une gestion des statuts utilisateur (en ligne, absent, etc.)
