#!/bin/bash

# Map of supported Java versions and their paths
declare -A JAVA_VERSIONS
JAVA_VERSIONS["11"]="/usr/lib/jvm/java-11-openjdk-amd64"
JAVA_VERSIONS["17"]="/usr/lib/jvm/java-17-openjdk-amd64"
JAVA_VERSIONS["21"]="/usr/lib/jvm/java-21-openjdk-amd64"

# --- 1. Check input argument ---
if [ -z "$1" ]; then
  echo "Usage: $0 {11|17|21}"
  echo "Available versions:"
  for version in "${!JAVA_VERSIONS[@]}"; do
    echo "  $version -> ${JAVA_VERSIONS[$version]}"
  done
  exit 1
fi

VERSION=$1
JAVA_PATH=${JAVA_VERSIONS[$VERSION]}

# --- 2. Validate selection ---
if [ -z "$JAVA_PATH" ] || [ ! -d "$JAVA_PATH" ]; then
  echo "❌ Java version $VERSION is not installed at $JAVA_PATH"
  exit 1
fi

# --- 3. Set update-alternatives ---
echo "🔧 Setting java and javac alternatives..."
sudo update-alternatives --install /usr/bin/java java "$JAVA_PATH/bin/java" 1
sudo update-alternatives --install /usr/bin/javac javac "$JAVA_PATH/bin/javac" 1
sudo update-alternatives --set java "$JAVA_PATH/bin/java"
sudo update-alternatives --set javac "$JAVA_PATH/bin/javac"

# --- 4. Export JAVA_HOME and update PATH for current session ---
export JAVA_HOME="$JAVA_PATH"
export PATH="$JAVA_HOME/bin:$PATH"
export MAVEN_OPTS="--add-modules ALL-SYSTEM"

# --- 5. Confirm environment ---
echo "✅ JAVA_HOME set to: $JAVA_HOME"
echo "✅ java version:"
java -version
echo "✅ javac version:"
javac -version

echo "📦 Maven version and Java it's using:"
JAVA_HOME=$JAVA_HOME mvn -v | grep -E "Maven|Java version|java.home"

echo "✅ Java $VERSION is now active!"


#wget https://dlcdn.apache.org/maven/maven-3/3.9.11/binaries/apache-maven-3.9.11-bin.tar.gz
#tar -xzf apache-maven-3.9.11-bin.tar.gz
#sudo mv apache-maven-3.9.11 /opt/maven
#sudo ln -s /opt/maven/bin/mvn /usr/bin/mvn