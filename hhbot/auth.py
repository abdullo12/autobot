# hhbot/auth.py
import logging, asyncio
import httpx  # вместо requests
from .config import settings
from .storage import tokens

logger = logging.getLogger(__name__)

def build_auth_url(chat_id: int) -> str:
    params = {
        "response_type": "code",
        "client_id":    settings.hh_client_id,
        "state":        chat_id,
        "redirect_uri": settings.hh_redirect_uri,
    }
    from urllib.parse import urlencode
    return f"https://hh.ru/oauth/authorize?{urlencode(params)}"

async def exchange_code_for_token(code: str, chat_id: int) -> str:
    """
    Обмен кода на токен с помощью HTTP Basic Auth.
    Сохраняем полученные токены в tokens[chat_id].
    Возвращаем сообщение для пользователя.
    """
    url = "https://api.hh.ru/oauth/token"
    auth = (settings.hh_client_id, settings.hh_client_secret)
    data = {
        "grant_type":    "authorization_code",
        "code":          code,
        "redirect_uri":  settings.hh_redirect_uri,
    }

    def request_token():
        with httpx.Client(timeout=10) as client:
            resp = client.post(url, data=data, auth=auth)
            resp.raise_for_status()
            return resp.json()

    try:
        payload = await asyncio.to_thread(request_token)
    except httpx.HTTPStatusError as e:
        # залогируем тело ответа HH
        logger.error("HH /oauth/token status=%s body=%s",
                     e.response.status_code, e.response.text)
        raise

    # сохраняем
    tokens[chat_id] = {
        "access_token":  payload["access_token"],
        "refresh_token": payload.get("refresh_token", ""),
    }
    logger.info("Saved HH profile for chat %s", chat_id)
    return "Профиль успешно привязан"
