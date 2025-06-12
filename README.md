# Autobot

A simple Telegram bot that fetches Java vacancies from HH.ru.

## Building

Use the included Gradle wrapper:

```bash
./gradlew build
./gradlew fatJar
```

The fat JAR will be located in `build/libs/app-1.0.jar`.

## Running

Set environment variables for your bot's credentials:

```bash
export BOT_TOKEN=your_token
export BOT_USERNAME=your_username
```

Then run the JAR:

```bash
java -jar build/libs/app-1.0.jar
```

You can also build a Docker image:

```bash
docker build -t autobot .
```

Run it with the same environment variables:

```bash
docker run -e BOT_TOKEN -e BOT_USERNAME autobot
```
