package ru.practicum.shareit.booking.dto;


import lombok.AllArgsConstructor;
import lombok.Data;
import ru.practicum.shareit.booking.model.Status;
import ru.practicum.shareit.item.dto.ItemDtoOutput;
import ru.practicum.shareit.user.dto.UserDto;

import java.time.LocalDateTime;


@Data
@AllArgsConstructor
public class BookingDtoOutput {
    private long id;
    private LocalDateTime start;
    private LocalDateTime end;
    private ItemDtoOutput item;
    private UserDto booker;
    private Status status;
}