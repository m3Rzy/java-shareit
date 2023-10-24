package ru.practicum.shareit.booking.service;

import ru.practicum.shareit.booking.dto.BookingDtoInput;
import ru.practicum.shareit.booking.dto.BookingDtoOutput;

import java.util.List;

public interface BookingService {

    BookingDtoOutput create(long userId, BookingDtoInput bookingDtoInput);

    BookingDtoOutput getById(long userId, long id);

    List<BookingDtoOutput> getAll(long userId, String state);

    List<BookingDtoOutput> getOwner(long userId, String state);

    BookingDtoOutput update(long userId, long id, Boolean isApproved);
}