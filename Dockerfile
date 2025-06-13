FROM eclipse-temurin:21-jdk
WORKDIR /app
COPY build/libs/autobot-1.0.jar app.jar
CMD ["java", "-jar", "app.jar"]

