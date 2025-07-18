@echo off
echo ========================================
echo  MOTORPH OOP TEST - SIMPLE VERSION
echo ========================================

echo Compiling SimpleTest.java...
javac -cp ".;lib/*;src" SimpleTest.java

if %errorlevel% neq 0 (
    echo.
    echo ERROR: Compilation failed!
    echo.
    echo Make sure you have these files:
    echo 1. SimpleTest.java (in main folder)
    echo 2. Employee.java (in src/model/)
    echo 3. RegularEmployee.java (in src/model/)
    echo 4. ProbationaryEmployee.java (in src/model/)
    echo 5. EmployeeFactory.java (in src/model/)
    echo.
    pause
    exit /b 1
)

echo.
echo Running the test...
echo.
java -cp ".;lib/*;src" SimpleTest

echo.
echo Test completed!
pause