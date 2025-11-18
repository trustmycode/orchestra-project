FROM maven:3.9.6-eclipse-temurin-21 AS build
WORKDIR /workspace
COPY pom.xml .
RUN mvn -B dependency:go-offline
COPY src ./src
RUN mvn -B package -DskipTests
RUN mvn -B dependency:copy-dependencies -DincludeScope=runtime -DoutputDirectory=target/dependency

FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /workspace/target/classes ./target/classes
COPY --from=build /workspace/target/dependency ./target/dependency
ENTRYPOINT ["java","-cp","target/classes:target/dependency/*","com.orchestra.executor.OrchestraExecutorApplication"]
