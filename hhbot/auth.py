"""OAuth helper functions for HeadHunter integration."""

from urllib.parse import urlencode
import logging
import asyncio
import requests

from .config import settings
from .storage import tokens

logger = logging.getLogger(__name__)


def build_auth_url(chat_id: int) -> str:
    params = {
        "response_type": "code",
        "client_id": settings.hh_client_id,
        "state": chat_id,
        "redirect_uri": settings.hh_redirect_uri,
    }
    return f"https://hh.ru/oauth/authorize?{urlencode(params)}"


async def exchange_code_for_token(code: str, chat_id: int) -> None:
    data = {
        "grant_type": "authorization_code",
        "client_id": settings.hh_client_id,
        "client_secret": settings.hh_client_secret,
        "code": code,
        "redirect_uri": settings.hh_redirect_uri,
    }

    def request_token() -> dict:
        resp = requests.post("https://hh.ru/oauth/token", data=data, timeout=10)
        resp.raise_for_status()
        return resp.json()

    payload = await asyncio.to_thread(request_token)
    tokens[chat_id] = {
        "access_token": payload["access_token"],
        "refresh_token": payload.get("refresh_token", ""),
    }
    logger.info("Saved HH profile for chat %s", chat_id)
