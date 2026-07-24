# 🎓 University Management System API

**University API** — это бэкенд-сервис (RESTful API) для автоматизации ключевых процессов высшего учебного заведения. Система объединяет управление абитуриентами, студентами, преподавателями, оргструктурой университета (факультеты, кафедры, специальности, группы), академическим процессом (дисциплины, ведомости оценок) и расписанием занятий.

Проект разработан на актуальном стеке Java с упором на реализацию чистого кода, безопасной аутентификации и современной инфраструктурной обвязки (Docker Compose).

---

## 🛠 Технологический стек

* **Language:** Java 21
* **Framework:** Spring Boot 3.4.x (Spring WebMVC, Spring Data JPA, Spring Security)
* **Security:** JWT (JSON Web Tokens)
* **Database:** PostgreSQL 16
* **Database Migrations:** Flyway
* **Documentation:** OpenAPI 3.0 / Swagger UI (springdoc-openapi)
* **Build System:** Apache Maven
* **Containerization:** Docker, Multi-stage Dockerfile, Docker Compose
* **Utility Tools:** Lombok

---

## 🏛 Структура и Модули Системы

База данных проектировалась с учетом нормализации и четкого разделения базовой авторизационной сущности пользователя и его доменных ролей.

<img width="1253" height="762" alt="image" src="https://github.com/user-attachments/assets/be3bef46-8ef6-47e4-86a0-f05974b7c149" />

### Основные модули:

1. **User Identity & Roles:**
    * `users` — центральная сущность пользователей. Расширяется связями 1-к-1 с узкоспециализированными ролями: `students`, `teachers`, `applicants`.
2. **Организационная структура:**
    * `faculties` (Факультеты) -> `specialties` (Специальности) -> `groups` (Учебные группы).
    * `faculties` -> `departments` (Кафедры) -> `department_positions` (Должности) -> `teachers_positions` (Связка преподавателей и их должностей).
3. **Учебный процесс:**
    * `subjects` (Дисциплины).
    * `group_subjects` (Предметный план группы: привязка преподавателя, семестра, академических часов и формы контроля).
    * `score_sheets` (Зачётные и экзаменационные ведомости оценок студентов).
4. **Система Расписания:**
    * `schedule` (Занятия: дата, время, аудитория, предмет, преподаватель).
    * `schedule_groups` (Связь «Многие-ко-многим» для совместных лекций нескольких учебных групп).

---

## 🚀 Быстрый запуск

### Предварительные требования

* **Docker** и **Docker Compose**
* **Git**

### 1. Клонирование репозитория

git clone [https://github.com/your-username/university-api.git](https://github.com/your-username/university-api.git)
cd university-api

### 2. Настройка переменных окружения (.env)

Создайте файл .env в корневом каталоге проекта:

# База данных PostgreSQL
POSTGRES_USER=postgres
POSTGRES_PASSWORD=postgres_secure_pass
POSTGRES_DB=university_db
DB_PORT=5432

# Настройки JWT
JWT_SECRET=YourSuperLongAndVerySecureSecretKeyForJWTAuth2026_Min32Chars!
JWT_EXPIRATION_MS=2160000

### 3. Запуск через Docker Compose

Выполните команду для сборки проекта и развертывания контейнеров:

docker compose up -d --build

Docker Compose автоматически:
1. Поднимет контейнер PostgreSQL 16 и дождётся состояния healthy.
2. Соберёт Docker-образ Spring Boot приложения.
3. Запустит Flyway-миграции для создания всей структуры таблиц.
4. Запустит API-сервис на порту 8080.

---

## 📖 Swagger / API Документация

После успешного запуска приложения интерактивная документация Swagger доступна по адресу:

* **Swagger UI:** http://localhost:8080/swagger-ui.html
* **OpenAPI Schema (JSON):** http://localhost:8080/v3/api-docs

---

## ⚙️ Переменные окружения (Configuration)

Настройки приложения динамически параметризуются через application.yaml и переменные окружения:

| Переменная | Описание | Значение по умолчанию |
| :--- | :--- | :--- |
| `DB_HOST` | Хост базы данных | `localhost` |
| `DB_PORT` | Порт PostgreSQL | `5432` |
| `DB_NAME` | Имя базы данных | `university_db` |
| `DB_USERNAME` | Имя пользователя БД | `postgres` |
| `DB_PASSWORD` | Пароль пользователя БД | `postgres` |
| `JWT_SECRET` | Секретный ключ для подписи JWT | *Дефолтный ключ* |
| `JWT_EXPIRATION_MS` | Время жизни токена (мс) | `2160000` (36 мин) |

---

## 🔧 Локальная сборка без Docker

Для локального запуска или тестирования:

1. Убедитесь, что установлен JDK 21.
2. Выполните сборку через Maven Wrapper:

# Сборка без прогона тестов
./mvnw clean package -DskipTests

# Запуск JAR файла
java -jar target/university-api-0.0.1-SNAPSHOT.jar
