@echo off
echo Checking for Java...
where java >nul 2>&1
if %ERRORLEVEL% NEQ 0 (
    echo Java not found. Please install Java 16 or later.
    exit /b 1
)

echo Checking for JAR files...
if not exist "modules\cloudsim\target\cloudsim-7.0.0-alpha.jar" (
    echo Building CloudSim...
    call mvn clean package
)

echo Running AutoScalingExample...
java -cp "modules\cloudsim\target\cloudsim-7.0.0-alpha.jar;modules\cloudsim-examples\target\cloudsim-examples-7.0.0-alpha.jar" org.cloudbus.cloudsim.examples.AutoScalingExample 