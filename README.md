# URL Shortener

Микросервисное приложение для сокращения ссылок. Написано как учебный.

**Живой сервис** — публичный адрес пока не подтверждён. Формат адреса после деплоя на VPS через Nginx — `http://<IP>/app`.

## Стек технологий

- Java 21, Spring Boot 4.1.0
- PostgreSQL, Liquibase
- Redis для кэша и rate limiting
- Apache Kafka для асинхронной аналитики и DLQ
- Docker, Docker Compose
- Prometheus, Grafana
- Kubernetes (Minikube)

## Архитектура

Два сервиса работают с событиями переходов.

- `shortener-service` создаёт короткие ссылки, выполняет редиректы и обновляет счётчик переходов. Событие сохраняется в Outbox в одной транзакции со счётчиком, затем `OutboxRelay` отправляет его в Kafka.
- `analytics-service` читает события из Kafka, проверяет `eventId` и считает статистику переходов. Аналитика доступна через HTTP API.

## Как запустить локально

Для запуска нужны Docker и Docker Compose. Команды используют адрес из `origin`, для клонирования нужен доступ к репозиторию.

```bash
git clone https://github.com/inyuri/shortener-service.git
cd shortener-service
docker compose -f shortener-service/docker-compose.yml up --build
```

- [Swagger UI](http://localhost:8080/swagger-ui/index.html)
- [Grafana](http://localhost:3000) — `admin` / `admin` при первом входе в новую установку. [Документация Grafana](https://grafana.com/docs/grafana/latest/setup-grafana/sign-in-to-grafana/)
- Форма создания ссылок — [shortener-service/frontend/index.html](shortener-service/frontend/index.html). После запуска API откройте файл в браузере.

## API

| Метод | Эндпоинт | Описание |
| --- | --- | --- |
| POST | `/api/links` | Создать короткую ссылку |
| GET | `/{shortCode}` | Перейти по ссылке (`302`) |
| GET | `/api/links/{shortCode}/stats` | Получить счётчик переходов |
| GET | `/api/links/{shortCode}/analytics` | Получить детальную аналитику |

## Что реализовано

- ✅ Валидация URL с понятными ошибками
- ✅ Истечение срока ссылки (410 Gone)
- ✅ Redis-кэш для быстрых редиректов
- ✅ Rate limiting (10 ссылок в минуту с одного IP)
- ✅ Kafka для аналитики, DLQ для сообщений, которые не удалось обработать
- ✅ Structured logging с correlationId
- ✅ Grafana дашборд с метриками
- ✅ Transactional Outbox с повторной отправкой и дедупликацией в аналитике
