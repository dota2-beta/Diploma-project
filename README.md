Система управления новостным контентом вуза с автоматической классификацией на базе машинного обучения. Система построена на микросервисной архитектуре.

## 🚀 Стек технологий

*   **Backend:** Java 17/21 (Spring Boot 3), Spring Security (Basic Auth), Spring Data JPA, Micrometer.
*   **ML Service:** Python 3.10, FastAPI, TensorFlow, Sentence-Transformers (SBERT), aio-pika.
*   **Frontend:** React, Material UI, Axios.
*   **Infrastructure:** PostgreSQL (БД), Redis (Кэширование), RabbitMQ (Очереди сообщений).
*   **Observability:** Prometheus (Сбор метрик), Grafana (Визуализация).
*   **DevOps:** Docker, Docker Compose.

## 📦 Запуск проекта

### 1. Клонирование репозитория
```bash
git clone https://github.com/dota2-beta/Diploma-project.git
cd Diploma-project
```

### 2. Сборка и запуск
Убедитесь, что у вас установлен Docker Desktop. В корне проекта выполните:
```bash
docker-compose up -d --build
```

### ⚠️ ВАЖНОЕ ПРИМЕЧАНИЕ ПО СБОРКЕ
Этап сборки контейнера **`ml-service`** может занять **от 30 минут до 2 часов**. Это связано с необходимостью загрузки предобученной модели SBERT (размером более 1 ГБ).

### 3. Доступ к интерфейсам
*   **Frontend (UI):** `http://localhost:3000`
*   **Backend API:** `http://localhost:8080/api/news`
*   **Grafana (Мониторинг):** `http://localhost:3001` (логин/пароль: `admin/admin`)
*   **RabbitMQ UI:** `http://localhost:15672` (логин/пароль: `guest/guest`)
*   **Prometheus:** `http://localhost:9090`

## 🔑 Авторизация (Admin)
Для доступа к функциям добавления и импорта новостей:
*   **Логин:** `admin`
*   **Пароль:** `admin123`

## 🏗 Архитектурная схема
https://drive.google.com/file/d/1JkcX_YttOWcnxrARSsUT8vB6-SA02JYk/view?usp=sharing
*(Здесь описывается взаимодействие между React, Spring Boot, FastAPI, PostgreSQL, Redis и RabbitMQ)*

## Примечание
В корне проекта для тестирования функциональности загрузки файла (для администратора) приложен файл news_test.xlsx
