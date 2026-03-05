# ---- build stage ----
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app

COPY pom.xml .
COPY src ./src
RUN mvn -DskipTests package

# ---- run stage ----
FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar
EXPOSE 8082
ENTRYPOINT ["java","-jar","/app/app.jar"]
#FROM eclipse-temurin:17-jre
#
#WORKDIR /app
#
## copy the built jar into the container
#COPY target/auth-service-0.0.1-SNAPSHOT.jar app.jar
#
#
#EXPOSE 8082
#
#ENTRYPOINT ["java", "-jar", "app.jar"]
