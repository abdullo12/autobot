import os
import logging

from fastapi import FastAPI, Query, HTTPException
from fastapi.responses import JSONResponse
import httpx
from dotenv import load_dotenv

from .database import AsyncSessionLocal, init_db
from .models import Profile

load_dotenv()

logging.basicConfig(level=logging.INFO)
logger = logging.getLogger(__name__)

HH_CLIENT_ID = os.getenv("HH_CLIENT_ID")
HH_CLIENT_SECRET = os.getenv("HH_CLIENT_SECRET")
HH_REDIRECT_URI = os.getenv("HH_REDIRECT_URI")

app = FastAPI()

@app.on_event("startup")
async def on_startup():
    await init_db()

@app.get("/callback")
async def callback(code: str = Query(...), state: int = Query(...)):
    try:
        async with httpx.AsyncClient() as client:
            resp = await client.post(
                "https://hh.ru/oauth/token",
                data={
                    "grant_type": "authorization_code",
                    "client_id": HH_CLIENT_ID,
                    "client_secret": HH_CLIENT_SECRET,
                    "code": code,
                    "redirect_uri": HH_REDIRECT_URI,
                },
            )
        resp.raise_for_status()
        data = resp.json()
        access_token = data["access_token"]
        refresh_token = data["refresh_token"]

        async with AsyncSessionLocal() as session:
            profile = Profile(
                chat_id=state,
                access_token=access_token,
                refresh_token=refresh_token,
            )
            session.add(profile)
            await session.commit()

        return JSONResponse({"status": "ok", "msg": "Профиль успешно привязан"})
    except Exception as e:
        logger.error(
            "Ошибка при обмене code→token для chat_id=%s: %s", state, e
        )
        raise HTTPException(
            status_code=500,
            detail={"status": "error", "msg": "Ошибка привязки профиля"},
        )
__all__ = ["app", "init_db", "AsyncSessionLocal", "Profile"]
