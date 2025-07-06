# Импортируем модуль для работы с локальными переменными
from dotenv import load_dotenv
# Импортируем модуль для сбора данных о вакансиях
import pandas as pd
# Импортируем модуль для работы с запросами
import requests
# Импортируем модуль для обработки json
import json
# Импортируем модуль для работы с операционной системой
import os
# Импортируем локальные переменные
load_dotenv()

# Создадим функцию для построения GET-запроса к вакансиям
def get_info(page=0):
    # Справочник для параметров GET-запроса
    params = {
        'text': 'NAME:Аналитик', # Текст фильтра. В имени должно быть слово "Аналитик"
        'area': 1, # Поиск ощуществляется по вакансиям города Москва
        'page': page, # Индекс страницы поиска на HH
        'per_page': 100 # Кол-во вакансий на 1 странице
    }


    req = requests.get('https://api.hh.ru/vacancies', params) # Посылаем запрос к API
    data = req.content.decode() # Декодируем его ответ, чтобы Кириллица отображалась корректно
    req.close()
    return data

# print(os.getenv("hh_oauth_client_id"))
# print(os.getenv("hh_oauth_client_secret"))

# link = "https://api.hh.ru/vacancies" 

# Создаем список, в котором будут хранится ответы запроса к сервису API hh.ru
js_objs = []

# Считываем первые 2000 вакансий
for page in range(0, 20):

    # Преобразуем текст ответа запроса в словарь Python
    js_obj = json.loads(get_info(page))

    # Добавляем текущий ответ запроса в список
    js_objs.extend(js_obj["items"])

    # Проверка на последнюю страницу, если вакансий меньше 2000
    if (js_obj['pages'] - page) <= 1:
        break

dataframe = pd.DataFrame(js_objs)
dataframe.to_csv("Вакансии_hh_выгрузка.csv", index=False)