@echo off
rem Compila o projeto e roda os testes automatizados do backend (JUnit 4).
rem E o mesmo que o GitHub Actions executa a cada push.
rem Na primeira vez baixa o JUnit (2 arquivos .jar, ~400 KB) para a pasta lib-teste\.
chcp 65001 >nul
cd /d "%~dp0"
where javac >nul 2>nul || (echo JDK nao encontrado ^(javac^). Instale o JDK 11+: https://adoptium.net & exit /b 1)

set LIB=lib-teste
set MAVEN=https://repo1.maven.org/maven2
if not exist %LIB% mkdir %LIB%
if not exist %LIB%\junit-4.13.2.jar (
    echo Baixando junit-4.13.2.jar...
    curl -fsSL -o %LIB%\junit-4.13.2.jar %MAVEN%/junit/junit/4.13.2/junit-4.13.2.jar || exit /b 1
)
if not exist %LIB%\hamcrest-core-1.3.jar (
    echo Baixando hamcrest-core-1.3.jar...
    curl -fsSL -o %LIB%\hamcrest-core-1.3.jar %MAVEN%/org/hamcrest/hamcrest-core/1.3/hamcrest-core-1.3.jar || exit /b 1
)

set CP=%LIB%\junit-4.13.2.jar;%LIB%\hamcrest-core-1.3.jar
if exist out-teste rmdir /s /q out-teste
mkdir out-teste\static
javac --release 11 -encoding UTF-8 -cp "%CP%" -d out-teste src\backend\*.java src\dominio\*.java src\web\*.java test\*.java || exit /b 1
copy /y src\web\static\* out-teste\static\ >nul
java -Dfile.encoding=UTF-8 -Dstdout.encoding=UTF-8 -cp "out-teste;%CP%" ExecutarTestes
exit /b %ERRORLEVEL%
