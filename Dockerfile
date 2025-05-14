# Use Eclipse Temurin OpenJDK 21 as base image
FROM eclipse-temurin:21-jre

# Set working directory
WORKDIR /app

# Copy the built jar into the container
COPY target/isp-quiz-1.0-SNAPSHOT.jar app.jar

# Expose the port the app runs on
EXPOSE 8888

# Run the application
ENTRYPOINT ["java", "-jar", "app.jar"]
