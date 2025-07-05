import sys
from pathlib import Path
import os

sys.path.append(str(Path(__file__).resolve().parents[1]))

os.environ.setdefault("TELEGRAM_BOT_TOKEN", "t")
os.environ.setdefault("HH_CLIENT_ID", "c")
os.environ.setdefault("HH_CLIENT_SECRET", "s")
os.environ.setdefault("HH_REDIRECT_URI", "http://test")

from hhbot.auth import build_auth_url


def test_build_auth_url():
    url = build_auth_url(123)
    assert "state=123" in url
