"""Entry point for the bot and FastAPI application."""

import logging
import threading
import asyncio
from fastapi import FastAPI, HTTPException, Query
from .auth import exchange_code_for_token
from .config import settings
from .bot import build_bot

app = FastAPI()
logging.basicConfig(level=logging.INFO)
logger = logging.getLogger(__name__)


@app.get("/callback")
async def callback(code: str = Query(...), state: int = Query(...)):
    try:
        await exchange_code_for_token(code, state)
        return {"status": "ok", "msg": "Профиль успешно привязан"}
    except Exception as e:
        logger.error("Ошибка при обмене code->token: %s", e)
        raise HTTPException(status_code=500, detail="Ошибка привязки профиля")


def main() -> None:
    bot_app = build_bot()

    def run_bot() -> None:
        bot_app.run_polling()

    threading.Thread(target=run_bot, daemon=True).start()

    import uvicorn
    uvicorn.run(app, host=settings.server_host, port=settings.server_port)


if __name__ == "__main__":
    main()
