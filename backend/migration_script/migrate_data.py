import pandas as pd
import requests
import time
from datetime import datetime

# URL твоего Java-бекенда
API_URL = "http://localhost:8080/api/news"
EXCEL_FILE = "backend/migration_script/news_re_labeled.xlsx"

# ДАННЫЕ ДЛЯ АВТОРИЗАЦИИ (те, что мы прописали в AppConfig)
ADMIN_USER = "admin"
ADMIN_PASS = "admin123"

def format_date(raw_date):
    """Приводит дату из Excel к формату dd.MM.yyyy для Java"""
    if pd.isna(raw_date):
        return ""
    
    if isinstance(raw_date, (pd.Timestamp, datetime)):
        return raw_date.strftime('%d.%m.%Y')

    return str(raw_date).strip()

def migrate():
    print(f"--- ЗАПУСК МИГРАЦИИ ---")
    
    try:
        # Читаем Excel
        df = pd.read_excel(EXCEL_FILE)
    except FileNotFoundError:
        print(f"ОШИБКА: Файл {EXCEL_FILE} не найден!")
        return

    total = len(df)
    print(f"Найдено {total} записей. Начинаем отправку...")

    success_count = 0
    error_count = 0

    for index, row in df.iterrows():
        title = str(row['News_Title']) if pd.notna(row['News_Title']) else ""
        content = str(row['News_Text']) if pd.notna(row['News_Text']) else ""
        link = str(row['News_Link']) if pd.notna(row['News_Link']) else ""
        
        date_str = format_date(row['News_Date'])

        if len(title) < 2 and len(content) < 2:
            continue

        payload = {
            "title": title,
            "content": content,
            "link": link,
            "dateStr": date_str
        }

        try:
            # ДОБАВЛЯЕМ ПАРАМЕТР auth ДЛЯ АВТОРИЗАЦИИ
            response = requests.post(
                API_URL, 
                json=payload, 
                auth=(ADMIN_USER, ADMIN_PASS)
            )

            if response.status_code == 200:
                success_count += 1
                if success_count % 10 == 0:
                    print(f"[{success_count}/{total}] Загружено: {title[:40]}...")
            elif response.status_code == 401:
                print("ОШИБКА: Неверный логин или пароль админа!")
                return
            else:
                error_count += 1
                print(f"ERROR {response.status_code} на строке {index}: {response.text}")

        except Exception as e:
            error_count += 1
            print(f"EXCEPTION на строке {index}: {e}")
        
        # Небольшая пауза, чтобы не перегрузить ML-сервис (т.к. Java будет его дергать)
        time.sleep(0.05)

    print(f"\n--- МИГРАЦИЯ ЗАВЕРШЕНА ---")
    print(f"Успешно: {success_count}")
    print(f"Ошибок: {error_count}")

if __name__ == "__main__":
    migrate()