package ru.practicum.shareit.booking.service;

import org.springframework.data.domain.Pageable;
import ru.practicum.shareit.booking.dto.BookingDtoInput;
import ru.practicum.shareit.booking.dto.BookingDtoOutput;

import java.util.List;

public interface BookingService {

    BookingDtoOutput create(long userId, BookingDtoInput bookingDtoInput);

    BookingDtoOutput getById(long userId, long id);

    List<BookingDtoOutput> readAllBookerBookings(long bookerId, String state, Pageable pageable);

    List<BookingDtoOutput> readAllOwnerItemBookings(long ownerId, String state, Pageable pageable);

    BookingDtoOutput update(long userId, long id, Boolean isApproved);
}