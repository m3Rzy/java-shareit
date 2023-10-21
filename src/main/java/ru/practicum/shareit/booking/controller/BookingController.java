package ru.practicum.shareit.booking.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.booking.dto.BookingDtoInput;
import ru.practicum.shareit.booking.dto.BookingDtoOutput;
import ru.practicum.shareit.booking.model.BookingState;
import ru.practicum.shareit.booking.service.BookingService;
import ru.practicum.shareit.exception.BadRequestException;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping(path = "/bookings")
public class BookingController {

    public static final String header = "X-Sharer-User-Id";

    private final BookingService bookingService;

    @PostMapping
    public BookingDtoOutput createBooking(@RequestHeader(header) long bookerId,
                                          @Valid @RequestBody BookingDtoInput bookingDtoInput) {
        return bookingService.createBooking(bookerId, bookingDtoInput);
    }

    @GetMapping("/{id}")
    public BookingDtoOutput findBookingById(@RequestHeader(header) long userId,
                                            @PathVariable long id) {
        return bookingService.findBookingById(userId, id);
    }

    @GetMapping
    public List<BookingDtoOutput> findAllBookings(@RequestHeader(header) long userId,
                                                  @RequestParam(defaultValue = "ALL") String state) {
        BookingState.parseFrom(state)
                .orElseThrow(() -> new BadRequestException("Unknown state: " + state));
        return bookingService.findAllBookings(userId, state);
    }

    @GetMapping("/owner")
    public List<BookingDtoOutput> findOwnerBookings(@RequestHeader(header) long ownerId,
                                                    @RequestParam(defaultValue = "ALL") String state) {
        BookingState.parseFrom(state)
                .orElseThrow(() -> new BadRequestException("Unknown state: " + state));
        return bookingService.findOwnerBookings(ownerId, state);
    }

    @PatchMapping("/{id}")
    public BookingDtoOutput updateBooking(@RequestHeader(header) long ownerId,
                                          @PathVariable long id,
                                          @RequestParam("approved") Boolean isApproved) {
        return bookingService.updateApproval(ownerId, id, isApproved);
    }
}