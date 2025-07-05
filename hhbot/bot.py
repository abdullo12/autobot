"""Telegram bot handlers."""

import logging
import asyncio
from typing import Dict
from telegram import Update, InlineKeyboardButton, InlineKeyboardMarkup
from telegram.ext import (
    Updater,
    CommandHandler,
    CallbackContext,
    CallbackQueryHandler,
    MessageHandler,
    Filters,
)

from .config import settings
from .auth import build_auth_url
from .vacancies import (
    filter_by_keywords,
    filter_by_city,
    filter_by_salary,
)
from .resume import fetch_resume

logger = logging.getLogger(__name__)

# Состояние ожидания ввода для каждого пользователя
user_states: Dict[int, str] = {}


def start(update: Update, context: CallbackContext) -> None:
    """Команда /start и /help."""
    text = (
        "\U0001F44B Привет! Я бот для автоматизации откликов на hh.ru.\n"
        "• /vacancies — поиск свежих вакансий\n"
        "• /auth — привязать ваш профиль hh.ru\n"
        "• /resume — получить ваше резюме\n"
        "• /help — список команд"
    )
    update.message.reply_text(text)


help_cmd = start  # alias


def auth(update: Update, context: CallbackContext) -> None:
    """Отправить ссылку для авторизации на hh.ru."""
    url = build_auth_url(update.effective_chat.id)
    update.message.reply_text(
        f"\U0001F517 Перейдите по ссылке и авторизуйтесь:\n{url}"
    )


def vacancies(update: Update, context: CallbackContext) -> None:
    """Показать кнопки для выбора фильтра вакансий."""
    keyboard = [
        [InlineKeyboardButton("По ключевым словам", callback_data="keyword")],
        [InlineKeyboardButton("По городу", callback_data="city")],
        [InlineKeyboardButton("По зарплате", callback_data="salary")],
    ]
    update.message.reply_text(
        "Выберите способ фильтрации вакансий:",
        reply_markup=InlineKeyboardMarkup(keyboard),
    )


def _on_filter_choice(update: Update, context: CallbackContext) -> None:
    """Обработка нажатия на inline-кнопки."""
    query = update.callback_query
    query.answer()
    chat_id = query.message.chat_id
    if query.data == "keyword":
        user_states[chat_id] = "keyword"
        query.message.reply_text("Введите ключевые слова для поиска вакансий:")
    elif query.data == "city":
        user_states[chat_id] = "city"
        query.message.reply_text("Введите город для поиска:")
    elif query.data == "salary":
        user_states[chat_id] = "salary"
        query.message.reply_text("Введите минимальную зарплату в рублях:")


def _on_text(update: Update, context: CallbackContext) -> None:
    """Обработка текстового ответа пользователя после выбора фильтра."""
    chat_id = update.effective_chat.id
    state = user_states.get(chat_id)
    if not state:
        return

    text = update.message.text.strip()
    if state == "keyword":
        items = filter_by_keywords(text)
    elif state == "city":
        items = filter_by_city(text)
    elif state == "salary":
        try:
            value = int(text)
        except ValueError:
            update.message.reply_text("Введите число.")
            return
        items = filter_by_salary(value)
    else:
        items = []

    user_states.pop(chat_id, None)

    if not items:
        update.message.reply_text("Не найдено подходящих вакансий.")
        return

    lines = [f"{vac['title']}\n{vac['url']}" for vac in items[:5]]
    update.message.reply_text(
        "\n\n".join(lines), disable_web_page_preview=True
    )


def resume(update: Update, context: CallbackContext) -> None:
    """Получить резюме пользователя."""
    try:
        text = asyncio.run(fetch_resume(update.effective_chat.id))
        update.message.reply_text(text)
    except Exception:
        update.message.reply_text(
            "\u274C Не могу получить резюме. Сначала выполните /auth"
        )


class BotApp:
    """Обёртка над Updater с методом run_polling для совместимости."""

    def __init__(self, updater: Updater):
        self.updater = updater

    def run_polling(self) -> None:
        self.updater.start_polling()
        self.updater.idle()


def build_bot() -> BotApp:
    """Создать экземпляр бота."""
    updater = Updater(token=settings.telegram_bot_token, use_context=True)
    dp = updater.dispatcher
    dp.add_handler(CommandHandler("start", start))
    dp.add_handler(CommandHandler("help", help_cmd))
    dp.add_handler(CommandHandler("auth", auth))
    dp.add_handler(CommandHandler("vacancies", vacancies))
    dp.add_handler(CommandHandler("resume", resume))
    dp.add_handler(CallbackQueryHandler(_on_filter_choice))
    dp.add_handler(MessageHandler(Filters.text & ~Filters.command, _on_text))
    return BotApp(updater)
