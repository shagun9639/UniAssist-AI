@echo off
title UniAssist AI - Java Spring Boot Backend (Port 8080)
echo =====================================================================
echo  Starting UniAssist AI - Java Spring Boot Backend
echo =====================================================================
cd /d "%~dp0backend"

echo [INFO] Verifying Java environment...
java -version
if %ERRORLEVEL% NEQ 0 (
    echo [ERROR] Java 17 or higher was not detected in PATH. Please install Java JDK 17+.
    pause
    exit /b 1
)

if "%SPRING_PROFILES_ACTIVE%"=="" (
    echo [INFO] Checking if local MySQL is reachable on port 3306...
    powershell -Command "if (!(Test-NetConnection -ComputerName 127.0.0.1 -Port 3306 -InformationLevel Quiet -WarningAction SilentlyContinue)) { exit 1 }" >nul 2>&1
    if errorlevel 1 (
        echo [INFO] MySQL not detected on port 3306. Activating embedded H2 database mode.
        set SPRING_PROFILES_ACTIVE=h2
    ) else (
        echo [INFO] MySQL detected on port 3306. Using default MySQL profile.
    )
)

echo [INFO] Launching Spring Boot with active profile: %SPRING_PROFILES_ACTIVE%
call .\mvnw.cmd spring-boot:run
pause
