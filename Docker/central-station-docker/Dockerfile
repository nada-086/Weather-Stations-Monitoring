# Use an OpenJDK base image
FROM openjdk:23-slim

# Copy necessary files
COPY Central_Station.jar /app/Central_Station.jar

# Set the working directory
WORKDIR /app

# Command to run the application
CMD ["java", "-jar", "Central_Station.jar"]
