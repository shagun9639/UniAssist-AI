@echo off
title UniAssist AI - Python AI/ML Microservice (Port 8000)
echo =====================================================================
echo  Starting UniAssist AI - Python AI/ML Service
echo =====================================================================
cd /d "%~dp0ai_service"

if not exist "venv\Scripts\activate.bat" (
    echo [INFO] Virtual environment not found. Creating venv...
    python -m venv venv
    call venv\Scripts\activate.bat
    echo [INFO] Installing required dependencies...
    pip install -r requirements.txt
) else (
    call venv\Scripts\activate.bat
)

if not exist "models\model_pipeline.joblib" (
    echo [INFO] Model pipeline not found. Training model now...
    python training\train_model.py
)

echo [INFO] Launching FastAPI uvicorn server on http://127.0.0.1:8000 ...
python -m uvicorn app.main:app --host 127.0.0.1 --port 8000 --reload
pause
