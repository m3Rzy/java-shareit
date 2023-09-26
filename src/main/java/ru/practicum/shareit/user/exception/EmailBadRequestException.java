package ru.practicum.shareit.user.exception;

public class EmailBadRequestException extends RuntimeException {
    public EmailBadRequestException(String message) {
        super(message);
    }
}
