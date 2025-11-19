FROM maven:3.9.6-eclipse-temurin-21 AS build
WORKDIR /workspace

COPY pom.xml ./pom.xml
COPY orchestra-domain/pom.xml ./orchestra-domain/pom.xml
COPY orchestra-api/pom.xml ./orchestra-api/pom.xml
COPY orchestra-executor/pom.xml ./orchestra-executor/pom.xml
RUN mvn -B -pl orchestra-executor -am dependency:go-offline

COPY orchestra-domain/src ./orchestra-domain/src
COPY orchestra-executor/src ./orchestra-executor/src
RUN mvn -B -pl orchestra-executor -am package dependency:copy-dependencies -DskipTests -DincludeScope=runtime -DoutputDirectory=target/dependency

FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /workspace/orchestra-executor/target/classes ./target/classes
COPY --from=build /workspace/orchestra-executor/target/dependency ./target/dependency
ENTRYPOINT ["java","-cp","target/classes:target/dependency/*","com.orchestra.executor.OrchestraExecutorApplication"]
