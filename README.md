### 1. Клонирование репозитория
```bash
git clone https://github.com/dota2-beta/Diploma-project.git
cd diploma-project
```

### 2. Предварительная загрузка модели (Опционально)
Чтобы не скачивать нейросеть (1.5 ГБ) каждый раз при сборке контейнера, можно загрузить её локально один раз.

```bash
cd backend/ml_service
pip install sentence-transformers
python download_model.py
```
*После завершения вернитесь в корень проекта (`cd ../..`).*

### 3. Запуск приложения
Запустите сборку и старт всех сервисов через Docker:

```bash
docker-compose up --build
```

После успешного запуска сервисы будут доступны по адресам:
*   **Frontend (Сайт):** http://localhost:3000
*   **Backend API:** http://localhost:8080/api/news
*   **ML Swagger (Документация нейросети):** http://localhost:5000/docs

### 4. Наполнение бд
При первом запуске база данных пуста. Чтобы загрузить в неё 1000 размеченных новостей, выполните скрипт миграции (в новом окне терминала):

```bash
cd backend/migration_script
pip install pandas openpyxl requests  # Если библиотеки еще не установлены
python migrate_data.py
```
