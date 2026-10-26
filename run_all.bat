@echo off
title UniAssist AI - Master Orchestrator
echo =====================================================================
echo  UniAssist AI - Launching Full End-to-End Application Stack
echo =====================================================================
echo.

echo [1/3] Launching Python AI/ML Service (Port 8000)...
start "UniAssist AI - Python AI Service" cmd /k "start_ai.bat"

echo Waiting 5 seconds for AI service to initialize...
timeout /t 5 /nobreak > nul

echo [2/3] Launching Java Spring Boot Backend (Port 8080)...
start "UniAssist AI - Spring Boot Backend" cmd /k "start_backend.bat"

echo Waiting 8 seconds for Spring Boot to initialize...
timeout /t 8 /nobreak > nul

echo [3/3] Launching Frontend Web Client (Port 3000)...
start "UniAssist AI - Frontend" cmd /k "start_frontend.bat"

echo.
echo =====================================================================
echo  UniAssist AI is booting up!
echo   - AI Service:   http://localhost:8000/docs
echo   - Backend API:  http://localhost:8080/swagger-ui.html
echo   - Web Frontend: http://localhost:3000/index.html
echo =====================================================================
echo.
pause
