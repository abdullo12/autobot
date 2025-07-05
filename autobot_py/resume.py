import requests
from .storage import tokens


def fetch_resume(chat_id: int) -> str:
    info = tokens.get(chat_id)
    if not info:
        raise ValueError("Профиль hh.ru не привязан")
    access_token = info["access_token"]
    resp = requests.get(
        "https://api.hh.ru/resumes/mine",
        headers={"Authorization": f"Bearer {access_token}"},
        timeout=10,
    )
    resp.raise_for_status()
    return resp.text
