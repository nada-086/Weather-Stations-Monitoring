# Use an OpenJDK base image
FROM openjdk:23-slim

# Copy necessary files
COPY Weather-Station-Mock.jar /app/weather_station.jar

# Set the working directory
WORKDIR /app

# Command to run the application
CMD ["java", "-jar", "weather_station.jar"]
