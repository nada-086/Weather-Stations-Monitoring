# Use an OpenJDK base image
FROM openjdk:23-slim

# Copy necessary files
COPY Weather_Stations_Monitoring.jar /app/Weather_Stations_Monitoring.jar

# Set the working directory
WORKDIR /app

# Command to run the application
CMD ["java", "-jar", "Weather_Stations_Monitoring.jar"]
