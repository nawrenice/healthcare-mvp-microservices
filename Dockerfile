# One build definition serves all four independently deployable Java services.
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /workspace
ARG SERVICE
COPY . .
RUN mvn -B -pl ${SERVICE} -am package -DskipTests && cp ${SERVICE}/target/${SERVICE}-0.0.1-SNAPSHOT.jar /app.jar

FROM eclipse-temurin:21-jre
WORKDIR /app
RUN groupadd --system healthcare && useradd --system --gid healthcare healthcare
COPY --from=build /app.jar app.jar
USER healthcare
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
