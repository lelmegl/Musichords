#!/usr/bin/env sh
# Compila o projeto e roda os testes automatizados do backend (JUnit 4).
# É o mesmo comando que o GitHub Actions executa a cada push.
# Na primeira vez baixa o JUnit (2 arquivos .jar, ~400 KB) para a pasta lib-teste/.
set -e
cd "$(dirname "$0")"
command -v javac >/dev/null 2>&1 || { echo "JDK não encontrado (javac). Instale o JDK 11+: https://adoptium.net"; exit 1; }

LIB=lib-teste
MAVEN=https://repo1.maven.org/maven2
mkdir -p "$LIB"

# baixar <arquivo> <url> <sha256>
baixar() {
    if [ ! -f "$LIB/$1" ]; then
        echo "Baixando $1..."
        if command -v curl >/dev/null 2>&1; then
            curl -fsSL -o "$LIB/$1.tmp" "$2"
        else
            wget -q -O "$LIB/$1.tmp" "$2"
        fi
        mv "$LIB/$1.tmp" "$LIB/$1"
    fi
    if command -v sha256sum >/dev/null 2>&1; then
        echo "$3  $LIB/$1" | sha256sum -c --quiet - || { rm -f "$LIB/$1"; echo "Checksum inválido: $1"; exit 1; }
    elif command -v shasum >/dev/null 2>&1; then
        echo "$3  $LIB/$1" | shasum -a 256 -c --quiet - || { rm -f "$LIB/$1"; echo "Checksum inválido: $1"; exit 1; }
    fi
}
baixar junit-4.13.2.jar "$MAVEN/junit/junit/4.13.2/junit-4.13.2.jar" \
    8e495b634469d64fb8acfa3495a065cbacc8a0fff55ce1e31007be4c16dc57d3
baixar hamcrest-core-1.3.jar "$MAVEN/org/hamcrest/hamcrest-core/1.3/hamcrest-core-1.3.jar" \
    66fdef91e9739348df7a096aa384a5685f4e875584cce89386a7a47251c4d8e9

CP="$LIB/junit-4.13.2.jar:$LIB/hamcrest-core-1.3.jar"
rm -rf out-teste
mkdir -p out-teste/static
javac --release 11 -encoding UTF-8 -cp "$CP" -d out-teste \
    src/backend/*.java src/dominio/*.java src/web/*.java test/*.java
cp src/web/static/* out-teste/static/
exec java -Dfile.encoding=UTF-8 -Dstdout.encoding=UTF-8 -cp "out-teste:$CP" ExecutarTestes
