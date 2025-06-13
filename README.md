# Autobot

Autobot is a Spring Boot application that fetches Java vacancies from HH.ru and
serves them via a Telegram bot.

## Setup

Set environment variables for your bot's credentials:

```bash
export BOT_TOKEN=your_token
export BOT_USERNAME=your_username
```

## Running

Start the bot using the Gradle wrapper:

```bash
./gradlew bootRun
```

To build a runnable jar:

```bash
./gradlew bootJar
```
