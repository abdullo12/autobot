# Autobot

Autobot — это приложение Spring Boot, которое собирает вакансии Java с HH.ru и
отправляет их через Telegram‑бота.

## Настройка

Заполните файл `src/main/resources/application.properties` своими данными или
передайте их через переменные окружения:

```properties
telegram.bot.token=YOUR_TOKEN
telegram.bot.username=@your_bot
```

Если переменные окружения `telegram.bot.token` и `telegram.bot.username`
установлены, они имеют приоритет над значениями из файла.

## Запуск

Чтобы запустить бота, используйте Gradle Wrapper:

```bash
./gradlew bootRun
```

Создание исполняемого jar:

```bash
./gradlew bootJar
```

## Kafka через Docker

Для работы Kafka в режиме разработки используйте `docker-compose.yml`:

```bash
docker-compose up -d
```

Он запустит Zookeeper и Kafka на стандартных портах `2181` и `9092`.


## Настройка webhook

После деплоя приложения по HTTPS зарегистрируйте webhook в Telegram:

```bash
curl -X POST https://api.telegram.org/bot<YOUR_BOT_TOKEN>/setWebhook \
     -d "url=https://<your-server>/webhook"
```

Для разработки можно открыть локальный порт через
[localtunnel](https://github.com/localtunnel/localtunnel):

```bash
npx localtunnel --port 8080
# обратите внимание на выведенный HTTPS‑адрес
```

Чтобы повторно зарегистрировать webhook с вашим публичным URL,
воспользуйтесь скриптом `register_webhook.sh`:

```bash
BOT_TOKEN=your_token WEBHOOK_URL=https://fifty-webs-think.loca.lt/webhook ./register_webhook.sh
```

Проверка текущей конфигурации:

```bash
curl -X POST https://api.telegram.org/bot<YOUR_BOT_TOKEN>/getWebhookInfo
```

Повторный запуск скрипта перезапишет предыдущий адрес webhook для того же токена.
