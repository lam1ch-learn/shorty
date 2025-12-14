# Shorty URL

**Shorty URL** — консольное приложение для создания, управления и сокращения URL с поддержкой TTL, лимитов переходов и пользовательских аккаунтов. Хранит данные в Redis.

## ✨ Основные функции

*   ✅ Создание коротких ссылок (`short.com/abc123`)
*   ✅ Лимит переходов (неограниченный или фиксированный)
*   ✅ TTL (время жизни ссылки в днях)
*   ✅ Пользовательские аккаунты (автоматическое сохранение UUID)
*   🔄 Автоочистка просроченных/исчерпанных ссылок
*   📱 Открытие ссылок в браузере
*   👑 Управление своими ссылками (список, редактирование, удаление)

## 🛠 Сборка и запуск

### Локально

```bash
# Клонировать репозиторий
git clone <https://github.com/lam1ch-learn/shorty.git>
cd shorty

# Запустить Redis (docker или локально на localhost:6379)
docker run -d -p 6379:6379 redis:alpine

# Собрать
mvn clean package

# Запустить
java -jar target/shorty-1.0-SNAPSHOT.jar

```

### CI/CD (GitHub Actions)

*   **Автоматическая сборка/тесты** при push/PR
*   **Code Quality**: Spotless + Checkstyle
*   **Release**: JAR публикуется при коммите с тегом `release`

## 🚀 Быстрый старт

```plaintext
1. Создать короткую ссылку → https://example.com → short.com/abc123
2. Перейти по короткой ссылке
3. Мои ссылки (список с TTL/лимитами)
4. Редактировать (TTL/лимит)
5. Удалить ссылку
6. Выход

```

## 📋 Требования

*   **Java 17**
*   **Redis** (localhost:6379)
*   **Maven 3.8+**

## 📁 Структура проекта

```plaintext
src/main/java/
├── cliService/     # CLI команды и сервис
├── config/         # Конфигурация Redis
├── models/         # UrlData, User
├── request/        # RedirectService
└── storage/        # RedisStore + интерфейс

```

**Готово к использованию!** 🎉
