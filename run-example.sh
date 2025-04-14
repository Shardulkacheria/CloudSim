#!/bin/bash

# Check if Java is installed
if ! command -v java &> /dev/null; then
    echo "Java not found. Please install Java 16 or later."
    exit 1
fi

# Check Java version
JAVA_VERSION=$(java -version 2>&1 | awk -F '"' '/version/ {print $2}')
echo "Using Java version: $JAVA_VERSION"

# Build the project if JARs don't exist
if [ ! -f "modules/cloudsim/target/cloudsim-7.0.0-alpha.jar" ] || [ ! -f "modules/cloudsim-examples/target/cloudsim-examples-7.0.0-alpha.jar" ]; then
    echo "Building CloudSim..."
    mvn clean package
fi

# Run the AutoScalingExample
echo "Running AutoScalingExample..."
java -cp "modules/cloudsim/target/cloudsim-7.0.0-alpha.jar:modules/cloudsim-examples/target/cloudsim-examples-7.0.0-alpha.jar" org.cloudbus.cloudsim.examples.AutoScalingExample 