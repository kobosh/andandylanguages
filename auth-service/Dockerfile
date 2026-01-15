FROM eclipse-temurin:17-jre

WORKDIR /app

# copy the built jar into the container
COPY target/auth-service-0.0.1-SNAPSHOT.jar app.jar


EXPOSE 8082

ENTRYPOINT ["java", "-jar", "app.jar"]
