FROM eclipse-temurin:16-jdk

WORKDIR /app

# Copy the project files
COPY . .

# Build the CloudSim module
RUN cd modules/cloudsim && \
    mvn clean install -DskipTests

# Build the CloudSim examples module
RUN cd modules/cloudsim-examples && \
    mvn clean package && \
    mvn assembly:single -DdescriptorId=jar-with-dependencies

# Set the entry point to run the AutoScalingExample by default
ENTRYPOINT ["java", "-jar", "modules/cloudsim-examples/target/cloudsim-examples-7.0.0-alpha-jar-with-dependencies.jar"]

# Usage example:
# Build:  docker build -t cloudsim:latest .
# Run:    docker run --rm cloudsim:latest
# Run different example: docker run --rm cloudsim:latest java -cp target/cloudsim-examples-7.0.0-alpha.jar:../cloudsim/target/cloudsim-7.0.0-alpha.jar org.cloudbus.cloudsim.examples.CloudSimExample1 