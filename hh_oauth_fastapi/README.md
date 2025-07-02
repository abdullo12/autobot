# HH OAuth FastAPI Service

Сервис выполняет привязку профиля HeadHunter через OAuth и сохраняет токены в БД.

## Запуск

1. Установите зависимости:

```bash
pip install -r requirements.txt
```

2. Создайте файл `.env` на основе `.env.example` и заполните значения.

3. База данных создаётся автоматически при запуске приложения.

4. Запустите сервер:

```bash
uvicorn hh_oauth_fastapi.main:app --reload
```

Endpoint для редиректа: `/callback`.
