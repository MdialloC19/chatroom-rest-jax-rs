#!/bin/bash

# Dossier pour les dépendances
LIB_DIR="lib"
TARGET_DIR="target/classes"
SOURCE_DIR="src/main/java"

# Créer les dossiers nécessaires
mkdir -p $LIB_DIR
mkdir -p $TARGET_DIR

# Liste des dépendances avec leurs URLs (versions spécifiques)
DEPENDENCIES=(
  "https://repo1.maven.org/maven2/org/glassfish/jersey/containers/jersey-container-grizzly2-http/2.35/jersey-container-grizzly2-http-2.35.jar"
  "https://repo1.maven.org/maven2/org/glassfish/jersey/core/jersey-server/2.35/jersey-server-2.35.jar"
  "https://repo1.maven.org/maven2/org/glassfish/jersey/core/jersey-common/2.35/jersey-common-2.35.jar"
  "https://repo1.maven.org/maven2/org/glassfish/jersey/core/jersey-client/2.35/jersey-client-2.35.jar"
  "https://repo1.maven.org/maven2/org/glassfish/jersey/inject/jersey-hk2/2.35/jersey-hk2-2.35.jar"
  "https://repo1.maven.org/maven2/org/glassfish/jersey/media/jersey-media-json-jackson/2.35/jersey-media-json-jackson-2.35.jar"
  "https://repo1.maven.org/maven2/org/glassfish/jersey/ext/jersey-entity-filtering/2.35/jersey-entity-filtering-2.35.jar"
  "https://repo1.maven.org/maven2/org/glassfish/jersey/media/jersey-media-jaxb/2.35/jersey-media-jaxb-2.35.jar"
  "https://repo1.maven.org/maven2/com/fasterxml/jackson/module/jackson-module-jaxb-annotations/2.12.2/jackson-module-jaxb-annotations-2.12.2.jar"
  "https://repo1.maven.org/maven2/com/fasterxml/jackson/jaxrs/jackson-jaxrs-base/2.12.2/jackson-jaxrs-base-2.12.2.jar"
  "https://repo1.maven.org/maven2/com/fasterxml/jackson/jaxrs/jackson-jaxrs-json-provider/2.12.2/jackson-jaxrs-json-provider-2.12.2.jar"
  "https://repo1.maven.org/maven2/org/glassfish/grizzly/grizzly-http-server/2.4.4/grizzly-http-server-2.4.4.jar"
  "https://repo1.maven.org/maven2/org/glassfish/grizzly/grizzly-http/2.4.4/grizzly-http-2.4.4.jar"
  "https://repo1.maven.org/maven2/org/glassfish/grizzly/grizzly-framework/2.4.4/grizzly-framework-2.4.4.jar"
  "https://repo1.maven.org/maven2/jakarta/ws/rs/jakarta.ws.rs-api/2.1.6/jakarta.ws.rs-api-2.1.6.jar"
  "https://repo1.maven.org/maven2/javax/ws/rs/javax.ws.rs-api/2.1.1/javax.ws.rs-api-2.1.1.jar"
  "https://repo1.maven.org/maven2/com/fasterxml/jackson/core/jackson-databind/2.12.2/jackson-databind-2.12.2.jar"
  "https://repo1.maven.org/maven2/com/fasterxml/jackson/core/jackson-core/2.12.2/jackson-core-2.12.2.jar"
  "https://repo1.maven.org/maven2/com/fasterxml/jackson/core/jackson-annotations/2.12.2/jackson-annotations-2.12.2.jar"
  "https://repo1.maven.org/maven2/org/glassfish/hk2/hk2-api/2.6.1/hk2-api-2.6.1.jar"
  "https://repo1.maven.org/maven2/org/glassfish/hk2/hk2-locator/2.6.1/hk2-locator-2.6.1.jar"
  "https://repo1.maven.org/maven2/org/glassfish/hk2/hk2-utils/2.6.1/hk2-utils-2.6.1.jar"
  "https://repo1.maven.org/maven2/org/glassfish/hk2/external/aopalliance-repackaged/2.6.1/aopalliance-repackaged-2.6.1.jar"
  "https://repo1.maven.org/maven2/org/javassist/javassist/3.25.0-GA/javassist-3.25.0-GA.jar"
  "https://repo1.maven.org/maven2/javax/inject/javax.inject/1/javax.inject-1.jar"
  "https://repo1.maven.org/maven2/org/glassfish/hk2/osgi-resource-locator/1.0.3/osgi-resource-locator-1.0.3.jar"
  "https://repo1.maven.org/maven2/jakarta/annotation/jakarta.annotation-api/1.3.5/jakarta.annotation-api-1.3.5.jar"
  "https://repo1.maven.org/maven2/jakarta/validation/jakarta.validation-api/2.0.2/jakarta.validation-api-2.0.2.jar"
  "https://repo1.maven.org/maven2/jakarta/xml/bind/jakarta.xml.bind-api/2.3.3/jakarta.xml.bind-api-2.3.3.jar"
  "https://repo1.maven.org/maven2/com/sun/activation/jakarta.activation/1.2.2/jakarta.activation-1.2.2.jar"
  "https://repo1.maven.org/maven2/org/glassfish/jersey/bundles/repackaged/jersey-guava/2.25.1/jersey-guava-2.25.1.jar"
)

# Télécharger les dépendances
echo "=== Téléchargement des dépendances ==="
for DEP_URL in "${DEPENDENCIES[@]}"; do
  FILENAME=$(basename "$DEP_URL")
  if [ ! -f "$LIB_DIR/$FILENAME" ]; then
    echo "Téléchargement de $FILENAME..."
    curl -# -L "$DEP_URL" -o "$LIB_DIR/$FILENAME"
  else 
    echo "$FILENAME déjà téléchargé"
  fi
done

# Construire le classpath
CLASSPATH="$TARGET_DIR"
for JAR in "$LIB_DIR"/*.jar; do
  CLASSPATH="$CLASSPATH:$JAR"
done

# Compiler le code
echo ""
echo "=== Compilation du code ==="
find $SOURCE_DIR -name "*.java" > sources.txt
javac -d $TARGET_DIR -cp "$CLASSPATH" @sources.txt
if [ $? -ne 0 ]; then
  echo "Erreur lors de la compilation"
  exit 1
fi
rm sources.txt

# Exécuter l'application
echo ""
echo "=== Démarrage de l'application ==="
java -cp "$CLASSPATH" com.chatroom.ChatRoomApp
