FROM maven:3.9.6-eclipse-temurin-21 AS build
WORKDIR /workspace

COPY pom.xml ./pom.xml
COPY orchestra-domain/pom.xml ./orchestra-domain/pom.xml
COPY orchestra-api/pom.xml ./orchestra-api/pom.xml
COPY orchestra-executor/pom.xml ./orchestra-executor/pom.xml
COPY ai-service/pom.xml ./ai-service/pom.xml

RUN mvn -B -pl ai-service -am dependency:go-offline

COPY ai-service/src ./ai-service/src
COPY orchestra-domain/src ./orchestra-domain/src

RUN mvn -B -pl ai-service -am package -DskipTests

FROM eclipse-temurin:21-jre-jammy
WORKDIR /app
COPY --from=build /workspace/ai-service/target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]

