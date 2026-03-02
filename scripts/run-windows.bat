@echo off
setlocal enabledelayedexpansion

:: HeistClient Windows Launcher
set "LIBS_DIR=%~dp0..\build\libs"

:: Find the HeistClient jar (excludes the -release-shaded variant)
set "JAR_PATH="
for %%f in ("%LIBS_DIR%\HeistClient-*.jar") do (
    echo %%~nf | findstr /v "release" >nul && set "JAR_PATH=%%f"
)

if not defined JAR_PATH (
    echo ERROR: No HeistClient jar found in %LIBS_DIR%
    echo Please build the project first:
    echo     .\gradlew build
    pause
    exit /b 1
)

echo Launching !JAR_PATH!...
java -jar "!JAR_PATH!" %*
pause