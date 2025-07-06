# hhbot/main.py

import logging
logging.basicConfig(level=logging.DEBUG, format="%(asctime)s %(levelname)s %(name)s: %(message)s")

import threading

from fastapi import FastAPI, HTTPException
from hhbot.config import settings
from hhbot.auth import exchange_code_for_token
from hhbot.bot import build_bot  # ваш модуль, где создаётся Updater/Dispatcher

log = logging.getLogger(__name__)
app = FastAPI()
_bot_instance = None  # глобально хранит ваш бот


@app.get("/callback")
async def callback(code: str, state: int):
    """
    Обработчик OAuth-callback от HH.
    При успехе возвращаем {"status":"ok","msg": "..."},
    при ошибке — HTTP 500 с деталями.
    """
    try:
        msg = await exchange_code_for_token(code, state)
        return {"status": "ok", "msg": msg}
    except Exception as e:
        log.error("Ошибка при обмене code->token:", exc_info=e)
        raise HTTPException(status_code=500, detail="Ошибка привязки профиля")


@app.on_event("startup")
def startup_bot():
    """
    Запускаем Telegram-бота в фоне: создаём поток,
    чтобы .start_polling() не блокировал Uvicorn.
    """
    global _bot_instance
    _bot_instance = build_bot()
    thread = threading.Thread(target=_bot_instance.start_polling, daemon=True)
    thread.start()
    log.info("✅ Telegram-бот запущен в отдельном потоке")


@app.on_event("shutdown")
def shutdown_bot():
    """
    При завершении приложения корректно останавливаем polling.
    """
    global _bot_instance
    if _bot_instance and hasattr(_bot_instance, "stop_polling"):
        _bot_instance.stop_polling()
        log.info("🛑 Telegram-бот остановлен")


if __name__ == "__main__":
    import uvicorn

    uvicorn.run(
        "hhbot.main:app",
        host="0.0.0.0",
        port=settings.server_port,
        reload=True,
        log_level="info",
    )
