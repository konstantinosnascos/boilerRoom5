@echo off
:: =====================================================
:: Order File Importer - Interaktiv start
:: =====================================================

cd /d %~dp0

echo.
echo [INFO] Startar programmet...
echo =====================================================================

:: Kör Maven - det här körs samtidigt som ditt program
call "C:\Program Files\Maven\apache-maven-3.9.11\bin\mvn.cmd" exec:java

:: När programmet är klart - PAUSA
echo.
echo =====================================================================
echo [INFO] Programmet har körts klart.
echo [INFO] Granska loggarna ovan.
echo [INFO] Tryck valfri tangent för att stänga fönstret.
echo =====================================================================
pause >nul