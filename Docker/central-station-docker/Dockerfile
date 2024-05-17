# Use an official OpenJDK runtime as a parent image
FROM openjdk:23-slim

# Set the working directory
WORKDIR /app

# Copy the application JAR file and any other necessary files
COPY Weather_Stations_Monitoring.jar /app/Weather_Stations_Monitoring.jar

# Copy the policy file to the container
COPY java.policy /app/java.policy

# Set the Java options to use the policy file
ENV JAVA_OPTS="-Djava.security.manager -Djava.security.policy=/app/java.policy"

# Run the application
CMD ["sh", "-c", "java $JAVA_OPTS -jar Weather_Stations_Monitoring.jar"]
