import random
import requests
from datetime import datetime


def fetch_and_format_vacancies() -> str:
    page = random.randint(0, 19)
    url = (
        "https://api.hh.ru/vacancies?text=python"
        f"&per_page=5&page={page}&only_with_salary=true&search_field=name"
    )
    resp = requests.get(url, headers={"User-Agent": "Mozilla/5.0"}, timeout=10)
    if resp.status_code != 200:
        return "\u2757 Ошибка при получении вакансий."
    data = resp.json()
    items = data.get("items", [])
    if not items:
        return "\u2757 Не найдено подходящих вакансий."

    lines = [
        f"\ud83d\udcc4 Страница {page + 1} • {datetime.now()}\n"
    ]
    for vac in items:
        title = vac.get("name")
        url = vac.get("alternate_url")
        employer = vac.get("employer", {})
        company = employer.get("name", "Не указано")
        area = vac.get("area", {})
        city = area.get("name", "Не указано")
        lines.append(
            f"\ud83d\udccc *{title}*\n"
            f"\ud83c\udfe2 {company}\n"
            f"\ud83d\uded1 {city}\n"
            f"\ud83d\udd17 {url}\n"
        )
    return "\n".join(lines)
