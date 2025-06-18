FROM eclipse-temurin:21-jdk
WORKDIR /app

COPY build/libs/autobot-1.0.jar app.jar

ENV SPRING_OUTPUT_ANSI_ENABLED=ALWAYS
ENV JAVA_OPTS=""

CMD java $JAVA_OPTS -jar app.jar
