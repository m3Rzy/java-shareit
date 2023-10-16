package ru.practicum.shareit.exception;

public class DeniedAccessException extends RuntimeException {
    public DeniedAccessException(String message) {
        super(message);
    }
}
