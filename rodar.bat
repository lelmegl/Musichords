@echo off
setlocal enabledelayedexpansion

echo ==============================================
echo MUSICHORDS - COMPILADOR E EXECUTOR AUTOMATICO
echo ==============================================

set JAVAFX_VERSION=17.0.10
set JAVAFX_DIR=lib\javafx-sdk-%JAVAFX_VERSION%
set JAVAFX_ZIP=lib\javafx.zip
set JAVAFX_URL=https://download2.gluonhq.com/openjfx/%JAVAFX_VERSION%/openjfx-%JAVAFX_VERSION%_windows-x64_bin-sdk.zip

if not exist "lib" mkdir lib

if not exist "%JAVAFX_DIR%" (
    echo [INFO] JavaFX SDK nao encontrado.
    echo [INFO] Baixando JavaFX %JAVAFX_VERSION% para Windows... Isso so acontece na primeira vez.
    powershell -Command "Invoke-WebRequest -Uri '%JAVAFX_URL%' -OutFile '%JAVAFX_ZIP%'"
    
    if exist "%JAVAFX_ZIP%" (
        echo [INFO] Extraindo arquivos...
        powershell -Command "Expand-Archive -Path '%JAVAFX_ZIP%' -DestinationPath 'lib\' -Force"
        del "%JAVAFX_ZIP%"
    ) else (
        echo [ERRO] Falha ao baixar o JavaFX. Verifique sua conexao.
        pause
        exit /b 1
    )
)

echo [INFO] Compilando o projeto...
if not exist "bin" mkdir bin

set MODULE_PATH=%JAVAFX_DIR%\lib

dir /s /B src\*.java > sources.txt
javac -d bin --module-path "%MODULE_PATH%" --add-modules javafx.controls,javafx.fxml -encoding UTF-8 @sources.txt
del sources.txt

if %ERRORLEVEL% neq 0 (
    echo [ERRO] Falha na compilacao.
    pause
    exit /b 1
)

echo [INFO] Executando o aplicativo...
java -cp "bin" --module-path "%MODULE_PATH%" --add-modules javafx.controls,javafx.fxml br.mackenzie.musichords.Launcher

pause
