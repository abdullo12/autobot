import logging
from telegram import Update
from telegram.ext import ApplicationBuilder, CommandHandler, ContextTypes

from .config import settings
from .auth import build_auth_url
from .vacancies import fetch_and_format_vacancies
from .resume import fetch_resume

logger = logging.getLogger(__name__)


async def start_cmd(
    update: Update, context: ContextTypes.DEFAULT_TYPE
) -> None:
    text = (
        "\U0001F44B Привет! Я бот для автоматизации откликов на hh.ru.\n"
        "• /vacancies — поиск свежих вакансий\n"
        "• /auth — привязать ваш профиль hh.ru\n"
        "• /getresume — получить ваше резюме\n"
        "• /help — список команд"
    )
    await update.message.reply_text(text)


auth_help = start_cmd  # alias


async def auth_cmd(update: Update, context: ContextTypes.DEFAULT_TYPE) -> None:
    url = build_auth_url(update.effective_chat.id)
    await update.message.reply_text(
        f"\U0001F517 Перейдите по ссылке и авторизуйтесь:\n{url}"
    )


async def vacancies_cmd(
    update: Update, context: ContextTypes.DEFAULT_TYPE
) -> None:
    text = fetch_and_format_vacancies()
    await update.message.reply_markdown(text)


async def resume_cmd(
    update: Update, context: ContextTypes.DEFAULT_TYPE
) -> None:
    try:
        resume = fetch_resume(update.effective_chat.id)
        await update.message.reply_text(resume)
    except Exception:
        await update.message.reply_text(
            "\u274C Не могу получить резюме. Сначала выполните /auth"
        )


def create_bot() -> ApplicationBuilder:
    app = ApplicationBuilder().token(settings.telegram_bot_token).build()
    app.add_handler(CommandHandler("start", start_cmd))
    app.add_handler(CommandHandler("help", auth_help))
    app.add_handler(CommandHandler("auth", auth_cmd))
    app.add_handler(CommandHandler("vacancies", vacancies_cmd))
    app.add_handler(CommandHandler("getresume", resume_cmd))
    return app
