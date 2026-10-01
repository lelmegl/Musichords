#!/usr/bin/env sh
# Recompila o Musichords.jar a partir de src/ (necessário apenas depois de editar o código)
set -e
cd "$(dirname "$0")"
command -v javac >/dev/null 2>&1 || { echo "JDK não encontrado (javac). Instale o JDK 17+: https://adoptium.net"; exit 1; }
rm -rf out
mkdir -p out/static
javac --release 11 -encoding UTF-8 -d out src/backend/*.java src/dominio/*.java src/web/*.java
cp src/web/static/* out/static/
jar --create --file Musichords.jar --main-class Servidor -C out .
echo "Musichords.jar atualizado."
