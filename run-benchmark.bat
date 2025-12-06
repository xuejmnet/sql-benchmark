@echo off
REM Easy-Query Benchmark Runner Script for Windows

echo ======================================
echo Easy-Query vs JOOQ Benchmark Suite
echo ======================================
echo.

REM Check if Maven is installed
where mvn >nul 2>nul
if %ERRORLEVEL% NEQ 0 (
    echo Maven is not installed. Please install Maven first.
    exit /b 1
)

REM Build the project
echo [1/3] Building project...
call mvn clean package -q

if %ERRORLEVEL% NEQ 0 (
    echo Build failed. Please check the errors above.
    exit /b 1
)

echo [SUCCESS] Build successful
echo.

REM Run benchmarks
echo [2/3] Running benchmarks...
echo This may take several minutes...
echo.

REM Create results directory if not exists
if not exist results mkdir results

REM Run all benchmarks and save results
java -jar target/benchmarks.jar -rf json -rff results/benchmark-results.json

if %ERRORLEVEL% NEQ 0 (
    echo Benchmark execution failed.
    exit /b 1
)

echo.
echo [SUCCESS] Benchmarks completed
echo.

REM Display results summary
echo [3/3] Results saved to:
echo   - results/benchmark-results.json
echo.
echo To view detailed results:
echo   1. Visit http://jmh.morethan.io/
echo   2. Upload results/benchmark-results.json
echo.
echo Done!



