@echo off
:: some computer not able open browser after start the application, open ahead time
start http://localhost:8080

java -jar simpleinputform-0.0.1-SNAPSHOT.jar
:: Check if the simpleinputform started successfully
if %errorlevel% neq 0 (
    echo Failed to start the application.
    pause
    exit /b %errorlevel%
)

:: Add a delay 10s to ensure the application has time to start
timeout /t 10 /nobreak >nul

:: Open the default web browser to the application's URL
start http://localhost:8080
pause