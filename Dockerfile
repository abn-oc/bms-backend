# Use OpenJDK 21 as base image
FROM eclipse-temurin:21-jdk-jammy

# Set working directory inside container
WORKDIR /app

# Copy the built JAR file into the container
COPY build/libs/*.jar app.jar

# Expose the application port (optional)
EXPOSE 8080

# Set the command to run the JAR
ENTRYPOINT ["java", "-jar", "app.jar"]
