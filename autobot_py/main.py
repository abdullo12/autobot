import logging
import threading
from fastapi import FastAPI, HTTPException, Query
from .auth import exchange_code_for_token
from .config import settings
from .bot import create_bot

app = FastAPI()
logging.basicConfig(level=logging.INFO)
logger = logging.getLogger(__name__)


@app.get("/callback")
def callback(code: str = Query(...), state: int = Query(...)):
    try:
        exchange_code_for_token(code, state)
        return {"status": "ok", "msg": "Профиль успешно привязан"}
    except Exception as e:
        logger.error("Ошибка при обмене code->token: %s", e)
        raise HTTPException(status_code=500, detail="Ошибка привязки профиля")


def main() -> None:
    bot_app = create_bot()

    def bot_thread():
        bot_app.run_polling()

    threading.Thread(target=bot_thread, daemon=True).start()

    import uvicorn
    uvicorn.run(app, host="0.0.0.0", port=settings.server_port)


if __name__ == "__main__":
    main()
