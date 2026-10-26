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

echo.
echo [NOTE] By default, Spring Boot connects to MySQL on localhost:3306 (database: uniassist_db).
echo [NOTE] To run with zero-setup H2 in-memory mode, launch with: set SPRING_PROFILES_ACTIVE=h2
echo.

call .\mvnw.cmd spring-boot:run
pause
