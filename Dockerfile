# Stage 1: Build the multi-module Maven project
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app

# Copy the entire project into the Docker container
COPY . .

# Move into the subfolder where your actual pom.xml lives!
WORKDIR /app/erfinder

# Run the Maven build (skipping tests to speed up Render deployment)
RUN mvn clean package -DskipTests

# Stage 2: Run the compiled Spring Boot application
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

# Copy the built WAR file from the newly updated path
COPY --from=build /app/erfinder/erfinder-server/target/*.war app.war

# Expose the standard Spring Boot port
EXPOSE 8080

# Start the server
ENTRYPOINT ["java", "-jar", "app.war"]
