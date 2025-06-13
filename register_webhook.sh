#!/bin/bash

# Register Telegram webhook and show current configuration.
# Usage: BOT_TOKEN=token WEBHOOK_URL=https://example.com/path ./register_webhook.sh

set -euo pipefail
TOKEN=${BOT_TOKEN:-$(grep '^telegram.bot.token' src/main/resources/application.properties | cut -d'=' -f2)}
URL=${WEBHOOK_URL:-"https://fifty-webs-think.loca.lt/telegram/webhook/${TOKEN}"}

curl -s -X POST "https://api.telegram.org/bot${TOKEN}/setWebhook" -d "url=${URL}" | tee /tmp/set_webhook_response.json

curl -s -X POST "https://api.telegram.org/bot${TOKEN}/getWebhookInfo" | tee /tmp/get_webhook_info.json
