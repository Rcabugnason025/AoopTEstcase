@echo off
echo ========================================
echo  MOTORPH JUNIT 5 TEST RUNNER
echo ========================================

echo Checking for JUnit 5 dependencies...
if not exist "lib\junit-jupiter-api-*.jar" (
    echo WARNING: JUnit 5 API JAR not found in lib folder
    echo You need to download JUnit 5 JARs to run these tests
    echo.
    echo For now, running the simple OOP test instead...
    echo.
    call RUN_TEST.bat
    exit /b 0
)

echo Compiling test classes...
javac -cp ".;lib/*;src" test/*.java

if %errorlevel% neq 0 (
    echo ERROR: Test compilation failed!
    echo Make sure you have JUnit 5 JARs in your lib folder
    pause
    exit /b 1
)

echo Running JUnit 5 tests...
java -cp ".;lib/*;src;test" test.TestRunner

echo.
echo JUnit tests completed!
pause