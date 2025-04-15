FROM eclipse-temurin:8-jdk

# Install Maven
RUN apt-get update && \
    apt-get install -y maven && \
    apt-get clean && \
    rm -rf /var/lib/apt/lists/*

# Set JAVA_HOME explicitly
ENV JAVA_HOME=/opt/java/openjdk
ENV PATH="${JAVA_HOME}/bin:${PATH}"

WORKDIR /app

# Copy the project files
COPY . .

# Build the root project first
RUN mvn clean install -N

# Build the CloudSim module
RUN cd modules/cloudsim && \
    mvn clean install

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