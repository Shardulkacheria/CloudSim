FROM eclipse-temurin:16-jdk

WORKDIR /app

# Copy the Maven POM files
COPY pom.xml ./
COPY modules/cloudsim/pom.xml ./modules/cloudsim/
COPY modules/cloudsim-examples/pom.xml ./modules/cloudsim-examples/
COPY modules/cloudsim-examples/src/main/resources/ ./modules/cloudsim-examples/src/main/resources/

# Download all required dependencies
RUN apt-get update && \
    apt-get install -y maven && \
    mvn dependency:go-offline

# Copy the source code
COPY modules/cloudsim/src ./modules/cloudsim/src
COPY modules/cloudsim-examples/src ./modules/cloudsim-examples/src

# Build the application
RUN mvn clean package

# Set up working directory for running examples
WORKDIR /app/modules/cloudsim-examples

# Command to run the AutoScalingExample by default
CMD ["java", "-cp", "target/cloudsim-examples-7.0.0-alpha.jar:../cloudsim/target/cloudsim-7.0.0-alpha.jar", "org.cloudbus.cloudsim.examples.AutoScalingExample"]

# Usage example:
# Build:  docker build -t cloudsim:latest .
# Run:    docker run --rm cloudsim:latest
# Run different example: docker run --rm cloudsim:latest java -cp target/cloudsim-examples-7.0.0-alpha.jar:../cloudsim/target/cloudsim-7.0.0-alpha.jar org.cloudbus.cloudsim.examples.CloudSimExample1 