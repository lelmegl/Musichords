@echo off
rem Recompila o Musichords.jar a partir de src\ (necessario apenas depois de editar o codigo)
chcp 65001 >nul
cd /d "%~dp0"
where javac >nul 2>nul
if errorlevel 1 (
    echo JDK nao encontrado ^(javac^). Instale o JDK 17 ou mais novo: https://adoptium.net
    pause
    exit /b 1
)
if exist out rmdir /s /q out
mkdir out\static
javac --release 11 -encoding UTF-8 -d out src\backend\*.java src\dominio\*.java src\web\*.java
if errorlevel 1 ( pause & exit /b 1 )
copy /y src\web\static\*.* out\static\ >nul
jar --create --file Musichords.jar --main-class Servidor -C out .
echo Musichords.jar atualizado.
