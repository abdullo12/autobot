# Dockerfile
FROM eclipse-temurin:21-jdk

WORKDIR /app

# копируем уже собранный Jar (у вас — autobot-1.0.jar)
COPY build/libs/autobot-1.0.jar app.jar

# открываем порт, который слушает Spring Boot
EXPOSE 8081

# запускаем приложение
ENTRYPOINT ["java","-jar","app.jar"]
