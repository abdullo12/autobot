"""Telegram bot handlers."""

import logging
from telegram import Update
from telegram.ext import Application, ApplicationBuilder, CommandHandler, ContextTypes

from .config import settings
from .auth import build_auth_url
from .vacancies import fetch_and_format_vacancies
from .resume import fetch_resume

logger = logging.getLogger(__name__)


async def start(
    update: Update, context: ContextTypes.DEFAULT_TYPE
) -> None:
    text = (
        "\U0001F44B Привет! Я бот для автоматизации откликов на hh.ru.\n"
        "• /vacancies — поиск свежих вакансий\n"
        "• /auth — привязать ваш профиль hh.ru\n"
        "• /resume — получить ваше резюме\n"
        "• /help — список команд"
    )
    await update.message.reply_text(text)


help_cmd = start  # alias


async def auth(update: Update, context: ContextTypes.DEFAULT_TYPE) -> None:
    url = build_auth_url(update.effective_chat.id)
    await update.message.reply_text(
        f"\U0001F517 Перейдите по ссылке и авторизуйтесь:\n{url}"
    )


async def vacancies(update: Update, context: ContextTypes.DEFAULT_TYPE) -> None:
    text = await fetch_and_format_vacancies()
    await update.message.reply_markdown(text)


async def resume(update: Update, context: ContextTypes.DEFAULT_TYPE) -> None:
    try:
        text = await fetch_resume(update.effective_chat.id)
        await update.message.reply_text(text)
    except Exception:
        await update.message.reply_text(
            "\u274C Не могу получить резюме. Сначала выполните /auth"
        )


def build_bot() -> Application:
    app = ApplicationBuilder().token(settings.telegram_bot_token).build()
    app.add_handler(CommandHandler("start", start))
    app.add_handler(CommandHandler("help", help_cmd))
    app.add_handler(CommandHandler("auth", auth))
    app.add_handler(CommandHandler("vacancies", vacancies))
    app.add_handler(CommandHandler("resume", resume))
    return app
