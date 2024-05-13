# Use an OpenJDK base image
FROM openjdk:23-slim

# Copy necessary files
COPY Weather_Station.jar /app/Weather_Station.jar

# Set the working directory
WORKDIR /app

# Command to run the application
CMD ["java", "-jar", "Weather_Station.jar"]
