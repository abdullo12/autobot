# hhbot/config.py
from pydantic_settings import BaseSettings

class Settings(BaseSettings):
    hh_client_id:     str
    hh_client_secret: str
    hh_redirect_uri:  str
    telegram_bot_token: str
    server_port:      int = 8081

    class Config:
        env_file = ".env"      # <-- файл в корне проекта
        env_prefix = ""        # <-- без префикса
        case_sensitive = False

settings = Settings()


