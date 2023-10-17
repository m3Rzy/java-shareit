package ru.practicum.shareit.booking.dto;

import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BookingItemDto {
    private Long id;
    private Long bookerId;
}
