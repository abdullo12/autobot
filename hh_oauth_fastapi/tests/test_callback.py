import sys, pathlib
sys.path.append(str(pathlib.Path(__file__).resolve().parents[2]))
import os
import pytest
import responses
import pytest_asyncio
from fastapi import status
from httpx import AsyncClient
from httpx import ASGITransport

os.environ.setdefault("DATABASE_URL", "sqlite+aiosqlite:///:memory:")
os.environ.setdefault("HH_CLIENT_ID", "client")
os.environ.setdefault("HH_CLIENT_SECRET", "secret")
os.environ.setdefault("HH_REDIRECT_URI", "http://test")

from hh_oauth_fastapi.main import app, init_db, AsyncSessionLocal, Profile
from sqlalchemy import select

@pytest_asyncio.fixture(autouse=True, scope="module")
async def setup_db():
    await init_db()
    yield

@responses.activate
@pytest.mark.asyncio
async def test_callback_success():
    responses.add(
        responses.POST,
        "https://hh.ru/oauth/token",
        json={"access_token": "a", "refresh_token": "r"},
        status=200,
    )
    async with AsyncClient(transport=ASGITransport(app=app), base_url="http://test") as ac:
        response = await ac.get("/callback", params={"code": "123", "state": 1})
    assert response.status_code == status.HTTP_200_OK
    async with AsyncSessionLocal() as session:
        result = await session.execute(select(Profile))
        profiles = result.scalars().all()
        assert len(profiles) == 1
        assert profiles[0].chat_id == 1

@responses.activate
@pytest.mark.asyncio
async def test_callback_failure():
    responses.add(
        responses.POST,
        "https://hh.ru/oauth/token",
        status=400,
    )
    async with AsyncClient(transport=ASGITransport(app=app), base_url="http://test") as ac:
        response = await ac.get("/callback", params={"code": "bad", "state": 2})
    assert response.status_code == status.HTTP_500_INTERNAL_SERVER_ERROR
    async with AsyncSessionLocal() as session:
        result = await session.execute(select(Profile).where(Profile.chat_id == 2))
        profile = result.scalar()
        assert profile is None
