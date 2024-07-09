# Use the official Gradle image to build the project
FROM gradle:6.7.0-jdk11 AS build

# Copy the project files to the Docker container
COPY --chown=gradle:gradle . /home/gradle/project

# Set the working directory
WORKDIR /home/gradle/project

# Build the project
RUN gradle build --no-daemon

# Use the official OpenJDK image to run the application
FROM openjdk:11-jre-slim

# Copy the built JAR file from the build stage
COPY --from=build /home/gradle/project/build/libs/Kotlin-AI-Cloud-Platform.jar /app/Kotlin-AI-Cloud-Platform.jar

# Set the entry point for the Docker container
ENTRYPOINT ["java", "-jar", "/app/Kotlin-AI-Cloud-Platform.jar"]
