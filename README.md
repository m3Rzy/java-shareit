# ShareIt - микросервисное приложение для аренды вещей
Java, Spring, PostgreSQL, REST API, Docker, JUnit, Lombok

## О проекте
Сервис, который позволяет пользователям рассказывать, какими вещами они готовы поделиться, а также находить нужную вещь и брать её в аренду на какое-то время

![](https://pictures.s3.yandex.net/resources/352_1690299000.png)

Приложение содержит три микросервиса: 
- Gateway для валидации запросов,
- Server, содержащий бизнес-логику, 
- базу данных PostgreSQL

Каждый микросервис запускается в собственном Docker контейнере.

## Основная функциональность: 
- Регистрация, обновление и получение пользователей
- Добавление, обновление, получение, а также поиск по предметам 
- Управление заявками на аренду вещей 
- Обработка запросов на аренду желаемых вещей
- Комментирование успешно завершённой аренды

## Эндпоинты

- POST /users - добавление пользователя
- PATCH /users/{userId} - обновление данных пользователя
- GET /users/{userId} - получение данных пользователя
- GET /users/ - получение списка пользователей
<br>

- POST /items - добавление вещи
- PATCH /items/{itemId} - обновление данных вещи
- GET /items/{itemId} - получение данных вещи
- GET /items/ - получение списка вещей
- GET /items/search - поиск вещей по тексту в параметре text
- POST /items/{itemId}/comment - добавление отзыва к вещи после завершенного бронирования
<br>

- POST /requests - добавление запроса на бронирование
- GET /requests/{requestId} - получение бронирования
- GET /items/all - получение списка бронирований
- GET /items - получение списка бронирований по id пользователя в заголовке запроса
<br>

- PATCH /bookings/{bookingId} - обновление данных бронирования
- PATCH /bookings/{bookingId} - одобрение или отклонение бронирования по параметру approved
- GET /bookings/{bookingId} - получение данных о бронировании
- GET /bookings/ - получение бронирований по фильтрам state, from, size
- GET /bookings/owner - получение бронирований пользователя по фильтрам state, from, size
<br>

## Как использовать:
Ознакомиться с примерами использования можно в [этой коллекции тестов Postman](https://github.com/yandex-praktikum/java-shareit/blob/add-docker/postman/sprint.json)
