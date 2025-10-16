@echo on
cd /d %~dp0

echo [1] Mapp: %cd%
pause

echo [2] Kontrollerar datafil...
if exist "incoming/orderdata.csv" (
    echo Filen finns – bra!
) else (
    echo ERROR: incoming/orderdata.csv finns INTE!
    pause
    exit /b 1
)
pause

echo [3] Kontrollerar Maven...
if exist "C:\Program Files\Maven\apache-maven-3.9.11\bin\mvn.cmd" (
    echo Maven hittad! Kör import...
    call "C:\Program Files\Maven\apache-maven-3.9.11\bin\mvn.cmd" exec:java "-Dexec.args=incoming/orderdata.csv"
) else (
    echo FEL: mvn.cmd finns inte där!
    pause
    exit /b 1
)

:: ========== SLUT ==========
echo.
echo [INFO] Programmet har körts klart.
echo [INFO] Tryck valfri tangent för att stänga.
pause