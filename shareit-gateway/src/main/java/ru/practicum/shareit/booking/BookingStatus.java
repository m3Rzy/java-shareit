package ru.practicum.shareit.booking;

import java.util.Optional;

public enum BookingStatus {
    ALL,
    CURRENT,
    PAST,
    FUTURE,
    WAITING,
    REJECTED;

    public static Optional<BookingStatus> from(String strState) {
        for (BookingStatus state : values()) {
            if (state.name().equalsIgnoreCase(strState)) {
                return Optional.of(state);
            }
        }
        return Optional.empty();
    }
}
