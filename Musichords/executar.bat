@echo off
rem Musichords - abre a interface no navegador (precisa apenas do Java 11 ou mais novo)
rem   executar.bat            interface (http://localhost:8080)
rem   executar.bat console    menu de texto da Parte 1
chcp 65001 >nul
cd /d "%~dp0"
where java >nul 2>nul
if errorlevel 1 (
    echo Java nao encontrado. Instale o Java 17 ou mais novo: https://adoptium.net
    pause
    exit /b 1
)
java -Dfile.encoding=UTF-8 -Dstdout.encoding=UTF-8 -jar Musichords.jar %*
if errorlevel 1 pause
