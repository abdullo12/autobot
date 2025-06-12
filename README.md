# Autobot

A simple Telegram bot that fetches Java vacancies from HH.ru.
The current implementation is written in Python using
`python-telegram-bot`.

## Setup

Install dependencies with pip:

```bash
pip install -r requirements.txt
```

Set environment variables for your bot's credentials (you can copy
`.env.example` to `.env` and edit it):

```bash
export BOT_TOKEN=your_token
export BOT_USERNAME=your_username
```

## Running

Start the bot:

```bash
python bot.py
```

To build the legacy Java version, use the Gradle wrapper:

```bash
./gradlew build
./gradlew fatJar
```
