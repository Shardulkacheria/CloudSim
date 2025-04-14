# CloudSim CI/CD Pipeline

This project includes a Continuous Integration and Continuous Deployment (CI/CD) pipeline implemented with GitHub Actions.

## Pipeline Overview

The pipeline automates the following processes:

1. **Building** the project
2. **Testing** the code
3. **Running the AutoScalingExample** simulation
4. **Building and publishing Docker images**
5. **Creating releases** for successful builds

## Workflow Configuration

The CI/CD pipeline is defined in `.github/workflows/ci-cd.yml` and consists of three main jobs:

### Build Job

- Triggered on pushes and pull requests to main/master branches
- Sets up JDK 11
- Builds the project with Maven
- Runs all tests
- Executes the AutoScalingExample
- Archives the build artifacts (JAR files)

### Docker Job

- Only runs after a successful build job
- Only triggered on pushes to main/master (not on pull requests)
- Builds a Docker image from the Dockerfile
- Publishes the image to GitHub Container Registry (ghcr.io)
- Tags the image with appropriate metadata

### Deploy Job

- Runs after successful build and docker jobs
- Only triggered on pushes to main/master (not on pull requests)
- Downloads the build artifacts
- Creates a GitHub release with an incremental version number
- Uploads the CloudSim examples JAR as a release asset

## Docker Container

The project includes a Dockerfile that containerizes the CloudSim application:

- Based on openjdk:11-jdk-slim
- Includes all necessary dependencies
- Compiles the application during build
- Set up to run the AutoScalingExample by default

### Using the Docker Container

You can run the CloudSim simulations using Docker:

```bash
# Pull the container image
docker pull ghcr.io/USERNAME/cloudsim:latest

# Run the default AutoScalingExample
docker run --rm ghcr.io/USERNAME/cloudsim:latest

# Run a different example
docker run --rm ghcr.io/USERNAME/cloudsim:latest mvn exec:java -Dexec.mainClass=org.cloudbus.cloudsim.examples.CloudSimExample1
```

Replace `USERNAME` with your GitHub username.

## Setting Up the Pipeline

To enable this pipeline in your own fork of the CloudSim project:

1. Ensure your GitHub repository has the `.github/workflows/ci-cd.yml` file
2. Make sure your repository has appropriate permissions for GitHub Actions
3. Enable GitHub Packages for your repository
4. Ensure GitHub Actions has permission to write packages

## Using the Pipeline

The pipeline runs automatically on:

- Every push to the main/master branch
- Every pull request against the main/master branch

You can view the pipeline's progress and results in the "Actions" tab of your GitHub repository.

## Benefits

This CI/CD pipeline offers several advantages:

- **Automated Testing**: Ensures code changes don't break existing functionality
- **Consistent Builds**: Builds code in a standardized environment
- **Sample Execution**: Verifies that the AutoScalingExample runs correctly
- **Container Images**: Provides ready-to-use Docker containers
- **Automatic Releases**: Creates versioned releases for successful builds
- **Artifact Storage**: Archives JAR files for easy download and deployment

## Extending the Pipeline

To extend this pipeline, you might consider:

- Adding code coverage reporting
- Implementing static code analysis
- Adding deployment to a Maven repository
- Setting up notification systems for build results
- Creating a Kubernetes deployment configuration 