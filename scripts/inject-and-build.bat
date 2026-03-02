@echo off
echo ============================================================
echo  Heist Injector Pipeline
echo  Step 1: Regenerating patches.zip from current mixin sources
echo ============================================================
call gradlew injectPatches
if %ERRORLEVEL% NEQ 0 (
    echo.
    echo [ERROR] Injector failed. patches.zip was NOT updated.
    pause
    exit /b %ERRORLEVEL%
)

echo.
echo ============================================================
echo  Step 2: Rebuilding client jar with new patches embedded
echo ============================================================
call gradlew shadowJar
if %ERRORLEVEL% NEQ 0 (
    echo.
    echo [ERROR] shadowJar failed.
    pause
    exit /b %ERRORLEVEL%
)

echo.
echo ============================================================
echo  Done! patches.zip regenerated and client jar rebuilt.
echo  Run the client normally with build-client.bat
echo ============================================================
pause
