import os
import random
from datetime import datetime

import requests
from dotenv import load_dotenv
from telegram import Update
from telegram.ext import ApplicationBuilder, CommandHandler, ContextTypes

load_dotenv()

BOT_TOKEN = os.getenv("BOT_TOKEN")
BOT_USERNAME = os.getenv("BOT_USERNAME")


def fetch_vacancies() -> str:
    page = random.randrange(20)
    url = (
        "https://api.hh.ru/vacancies?text=java"
        f"&per_page=5&page={page}&only_with_salary=true&search_field=name"
    )
    headers = {"User-Agent": "Mozilla/5.0"}
    try:
        resp = requests.get(url, headers=headers, timeout=10)
        resp.raise_for_status()
    except requests.RequestException:
        return "❗ Ошибка при получении вакансий."
    data = resp.json()
    items = data.get("items", [])
    if not items:
        return "❗ Не найдено подходящих вакансий."

    lines = [f"📄 Страница {page + 1} • {datetime.now()}\n"]
    for vac in items:
        title = vac.get("name")
        link = vac.get("alternate_url")
        company = vac.get("employer", {}).get("name", "Не указано")
        city = vac.get("area", {}).get("name", "Не указано")
        lines.append(
            f"📌 *{title}*\n" f"🏢 {company}\n" f"📍 {city}\n" f"🔗 {link}\n"
        )
    return "\n".join(lines)


async def send_vacancies(update: Update, context: ContextTypes.DEFAULT_TYPE) -> None:
    await update.message.reply_text(fetch_vacancies())


def main() -> None:
    if not BOT_TOKEN:
        raise RuntimeError("BOT_TOKEN is not set")

    app = ApplicationBuilder().token(BOT_TOKEN).build()
    app.add_handler(CommandHandler(["start", "vacancies"], send_vacancies))
    app.run_polling()


if __name__ == "__main__":
    main()
