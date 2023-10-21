package ru.practicum.shareit.booking.service;

import ru.practicum.shareit.booking.dto.BookingDtoInput;
import ru.practicum.shareit.booking.dto.BookingDtoOutput;

import java.util.List;

public interface BookingService {

    BookingDtoOutput createBooking(long userId, BookingDtoInput bookingDtoInput);

    BookingDtoOutput findBookingById(long userId, long id);

    List<BookingDtoOutput> findAllBookings(long userId, String state);

    List<BookingDtoOutput> findOwnerBookings(long userId, String state);

    BookingDtoOutput updateApproval(long userId, long id, Boolean isApproved);
}