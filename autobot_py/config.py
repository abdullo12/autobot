from pydantic import BaseSettings
from dotenv import load_dotenv

load_dotenv()


class Settings(BaseSettings):
    telegram_bot_token: str
    telegram_bot_username: str
    hh_client_id: str
    hh_client_secret: str
    hh_redirect_uri: str
    server_port: int = 8000

    class Config:
        env_file = ".env"


settings = Settings()
