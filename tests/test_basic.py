import sys
from pathlib import Path
import os

sys.path.append(str(Path(__file__).resolve().parents[1]))

os.environ.setdefault("TELEGRAM_BOT_TOKEN", "t")
os.environ.setdefault("HH_CLIENT_ID", "c")
os.environ.setdefault("HH_CLIENT_SECRET", "s")
os.environ.setdefault("HH_REDIRECT_URI", "http://test")

from hhbot.auth import build_auth_url
from hhbot.vacancies import (
    filter_by_keywords,
    filter_by_city,
    filter_by_salary,
    raw_vacancies,
)
from hhbot.bot import user_states, _on_filter_choice, _on_text
import pytest


@pytest.fixture(autouse=True)
def sample_vacancies():
    raw_vacancies[:] = [
        {
            "name": "Python разработчик",
            "alternate_url": "http://e/1",
            "area": {"name": "Москва"},
            "salary": {"from": 150000, "currency": "RUR"},
        },
        {
            "name": "Java developer",
            "alternate_url": "http://e/2",
            "area": {"name": "Санкт-Петербург"},
            "salary": {"to": 120000, "currency": "RUR"},
        },
        {
            "name": "Data Scientist",
            "alternate_url": "http://e/3",
            "area": {"name": "Москва"},
            "salary": {"from": 200000, "currency": "RUR"},
        },
        {
            "name": "Python QA engineer",
            "alternate_url": "http://e/4",
            "area": {"name": "Новосибирск"},
            "salary": None,
        },
        {
            "name": "Python разработчик",
            "alternate_url": "http://e/5",
            "area": {"name": "Москва"},
            "salary": {"from": 100000, "currency": "RUR"},
        },
    ]
    yield
    raw_vacancies.clear()


def test_build_auth_url():
    url = build_auth_url(123)
    assert "state=123" in url


def test_filter_by_keywords(sample_vacancies):
    res = filter_by_keywords("Python разработчик")
    assert len(res) == 2
    assert all("Python" in r["title"] for r in res)


def test_filter_by_city(sample_vacancies):
    res = filter_by_city("Москва")
    assert len(res) == 3


def test_filter_by_salary(sample_vacancies):
    res = filter_by_salary(150000)
    assert len(res) == 2


class DummyMessage:
    def __init__(self, text=""):
        self.text = text
        self.replies = []

    def reply_text(self, text, **kwargs):
        self.replies.append(text)


def test_keyword_scenario(sample_vacancies):
    user_states.clear()
    msg = DummyMessage()
    update_cb = type(
        "U",
        (),
        {
            "callback_query": type(
                "CQ",
                (),
                {
                    "data": "keyword",
                    "message": type(
                        "M",
                        (),
                        {"chat_id": 1, "reply_text": msg.reply_text},
                    )(),
                    "answer": lambda *a, **k: None,
                },
            )(),
        },
    )()

    _on_filter_choice(update_cb, None)
    assert user_states[1] == "keyword"

    update_txt = type(
        "U2",
        (),
        {
            "message": DummyMessage("Python разработчик"),
            "effective_chat": type("C", (), {"id": 1})(),
        },
    )()

    _on_text(update_txt, None)
    assert user_states.get(1) is None
    # ответ содержит две ссылки на вакансии
    assert update_txt.message.replies
    assert update_txt.message.replies[0].count("http://") == 2
