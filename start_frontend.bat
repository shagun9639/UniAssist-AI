@echo off
title UniAssist AI - Web Frontend Client (Port 3000)
echo =====================================================================
echo  Serving UniAssist AI - Responsive Web Frontend
echo =====================================================================
cd /d "%~dp0frontend"

echo [INFO] Opening UniAssist AI in default browser...
start http://localhost:3000/index.html

echo [INFO] Starting static HTTP server on http://localhost:3000 ...
python -m http.server 3000
pause
