@echo off
:: =====================================================
:: Order File Importer - Interaktiv körning
:: =====================================================

cd /d %~dp0

echo.
echo [INFO] Startar orderimport...
echo [INFO] Använder Maven för att köra programmet.
echo.

:: Kör programmet – där du får välja fil interaktivt
"C:\Program Files\Maven\apache-maven-3.9.11\bin\mvn.cmd" exec:java

pause