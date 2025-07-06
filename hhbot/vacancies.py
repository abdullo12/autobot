"""Vacancy search helpers."""

import asyncio
import random
from datetime import datetime
import requests
from typing import Dict, List

# Список вакансий, который можно переопределить в тестах
raw_vacancies: List[Dict] = []


async def fetch_and_format_vacancies() -> str:
    page = random.randint(0, 19)
    url = (
        "https://api.hh.ru/vacancies?text=python"
        f"&per_page=5&page={page}&only_with_salary=true&search_field=name"
    )
    def request_vacancies() -> requests.Response:
        return requests.get(
            url, headers={"User-Agent": "Mozilla/5.0"}, timeout=10
        )

    resp = await asyncio.to_thread(request_vacancies)
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


def filter_by_keywords(keywords: str) -> List[Dict]:
    """Возвращает вакансии, содержащие все слова из keywords."""
    words = [w.lower() for w in keywords.split()]
    result: List[Dict] = []
    for vac in raw_vacancies:
        title = vac.get("name", "")
        title_l = title.lower()
        if all(word in title_l for word in words):
            result.append({"title": title, "url": vac.get("alternate_url", "")})
    return result


def filter_by_city(city: str) -> List[Dict]:
    """Возвращает вакансии по указанному городу."""
    city_l = city.lower()
    result: List[Dict] = []
    for vac in raw_vacancies:
        area = vac.get("area", {})
        name = area.get("name", "").lower()
        if city_l in name:
            result.append({"title": vac.get("name", ""), "url": vac.get("alternate_url", "")})
    return result


def filter_by_salary(min_salary: int) -> List[Dict]:
    """Возвращает вакансии с оплатой >= min_salary (в рублях)."""
    result: List[Dict] = []
    for vac in raw_vacancies:
        salary = vac.get("salary") or {}
        currency = salary.get("currency")
        amount = salary.get("from") or salary.get("to")
        if currency == "RUR" and isinstance(amount, (int, float)) and amount >= min_salary:
            result.append({"title": vac.get("name", ""), "url": vac.get("alternate_url", "")})
    return result