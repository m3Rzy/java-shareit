package ru.practicum.shareit.booking;

import lombok.AllArgsConstructor;
import lombok.Data;
import ru.practicum.shareit.annotation.DateBookingValidation;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@DateBookingValidation
public class BookingDtoInput {
    private long id;
    private LocalDateTime start;
    private LocalDateTime end;
    private long itemId;
}
