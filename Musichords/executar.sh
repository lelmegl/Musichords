#!/usr/bin/env sh
# Musichords - abre a interface no navegador (precisa apenas do Java 11 ou mais novo)
#   ./executar.sh            interface (http://localhost:8080)
#   ./executar.sh console    menu de texto da Parte 1
cd "$(dirname "$0")" || exit 1
if ! command -v java >/dev/null 2>&1; then
    echo "Java não encontrado. Instale o Java 17 ou mais novo: https://adoptium.net"
    exit 1
fi
exec java -Dfile.encoding=UTF-8 -Dstdout.encoding=UTF-8 -jar Musichords.jar "$@"
