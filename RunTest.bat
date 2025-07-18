@echo off
echo ========================================
echo  MOTORPH OOP TEST RUNNER
echo ========================================

echo Step 1: Compiling TEST_RUNNER.java...
javac -cp ".;lib/*" TEST_RUNNER.java

if %errorlevel% neq 0 (
    echo ERROR: Compilation failed!
    echo Make sure you have the new Employee model files in src/model/
    pause
    exit /b 1
)

echo Step 2: Running the OOP test...
java -cp ".;lib/*" TEST_RUNNER

echo.
echo Test completed! Check the output above.
pause