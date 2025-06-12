FROM eclipse-temurin:21-jdk

WORKDIR /app
COPY build/libs/app-1.0.jar app.jar

CMD ["java", "-jar", "app.jar"]

