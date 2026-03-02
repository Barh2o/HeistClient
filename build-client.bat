@echo off
echo Building HeistClient...
cd /d %~dp0
call .\gradlew shadowJar
echo.
if %ERRORLEVEL% EQU 0 (
    echo [SUCCESS] Build complete! You can now run the client.
) else (
    echo [ERROR] Build failed. Check the output above for details.
)
pause
