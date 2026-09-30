#!/bin/bash

echo "=============================================="
echo " MUSICHORDS - COMPILADOR E EXECUTOR AUTOMATICO"
echo "=============================================="

JAVAFX_VERSION="17.0.10"
JAVAFX_DIR="lib/javafx-sdk-$JAVAFX_VERSION"
JAVAFX_ZIP="lib/javafx.zip"

mkdir -p lib
mkdir -p bin

# Detect OS for correct JavaFX SDK
OS_NAME=$(uname -s | tr '[:upper:]' '[:lower:]')
case "$OS_NAME" in
    linux*)     OS_SUFFIX="linux-x64";;
    darwin*)    OS_SUFFIX="mac-x64";; # M1/M2 macs might need aarch64, but keeping x64 for simplicity here
    *)          OS_SUFFIX="linux-x64";;
esac

JAVAFX_URL="https://download2.gluonhq.com/openjfx/$JAVAFX_VERSION/openjfx-${JAVAFX_VERSION}_${OS_SUFFIX}_bin-sdk.zip"

if [ ! -d "$JAVAFX_DIR" ]; then
    echo "[INFO] JavaFX SDK nao encontrado."
    echo "[INFO] Baixando JavaFX $JAVAFX_VERSION para $OS_SUFFIX..."
    
    # Try wget or curl
    if command -v wget >/dev/null 2>&1; then
        wget -q -O "$JAVAFX_ZIP" "$JAVAFX_URL"
    elif command -v curl >/dev/null 2>&1; then
        curl -s -L -o "$JAVAFX_ZIP" "$JAVAFX_URL"
    else
        echo "[ERRO] Nem wget nem curl estao instalados. Nao foi possivel baixar o JavaFX."
        exit 1
    fi
    
    echo "[INFO] Extraindo arquivos..."
    unzip -q "$JAVAFX_ZIP" -d lib/
    rm "$JAVAFX_ZIP"
fi

echo "[INFO] Compilando o projeto..."
MODULE_PATH="$JAVAFX_DIR/lib"

find src -name "*.java" > sources.txt
javac -d bin --module-path "$MODULE_PATH" --add-modules javafx.controls,javafx.fxml -encoding UTF-8 @sources.txt
COMPILE_STATUS=$?
rm sources.txt

if [ $COMPILE_STATUS -ne 0 ]; then
    echo "[ERRO] Falha na compilacao."
    exit 1
fi

echo "[INFO] Executando o aplicativo..."
java -cp "bin" --module-path "$MODULE_PATH" --add-modules javafx.controls,javafx.fxml br.mackenzie.musichords.Launcher
