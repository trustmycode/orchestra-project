# Stage 1: Build the application using Maven
FROM maven:3.9.6-eclipse-temurin-21 AS build
WORKDIR /workspace

# Copy all pom files and download dependencies first to leverage Docker layer caching
COPY pom.xml ./pom.xml
COPY orchestra-domain/pom.xml ./orchestra-domain/pom.xml
COPY orchestra-api/pom.xml ./orchestra-api/pom.xml
COPY orchestra-executor/pom.xml ./orchestra-executor/pom.xml
RUN mvn -B -pl orchestra-api -am dependency:go-offline

# Copy source code
COPY orchestra-domain/src ./orchestra-domain/src
COPY orchestra-api/src ./orchestra-api/src

# Build the executable JAR for the orchestra-api module
RUN mvn -B -pl orchestra-api -am package -DskipTests

# Stage 2: Create the final, smaller image
FROM eclipse-temurin:21-jre-jammy
WORKDIR /app

# Copy the executable JAR from the build stage
COPY --from=build /workspace/orchestra-api/target/*.jar app.jar

EXPOSE 8085
# Run the application using the standard java -jar command
ENTRYPOINT ["java", "-jar", "app.jar"]