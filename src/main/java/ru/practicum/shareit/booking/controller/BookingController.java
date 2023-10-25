package ru.practicum.shareit.booking.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.booking.dto.BookingDtoInput;
import ru.practicum.shareit.booking.dto.BookingDtoOutput;
import ru.practicum.shareit.booking.model.BookingState;
import ru.practicum.shareit.booking.service.BookingService;
import ru.practicum.shareit.exception.BadRequestException;

import javax.validation.Valid;
import javax.validation.constraints.Min;
import java.util.List;

@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping(path = "/bookings")
public class BookingController {

    public static final String header = "X-Sharer-User-Id";

    private final BookingService bookingService;

    @PostMapping
    public BookingDtoOutput createBooking(@RequestHeader(header) long bookerId,
                                          @Valid @RequestBody BookingDtoInput bookingDtoInput) {
        return bookingService.create(bookerId, bookingDtoInput);
    }

    @GetMapping("/{id}")
    public BookingDtoOutput findBookingById(@RequestHeader(header) long userId,
                                            @PathVariable long id) {
        return bookingService.getById(userId, id);
    }

    @GetMapping
    public List<BookingDtoOutput> findAllBookings(@RequestHeader("X-Sharer-User-Id") long userId,
                                                  @RequestParam(defaultValue = "ALL") String state,
                                                  @RequestParam(defaultValue = "0")
                                                  @Min(value = 0, message = "Значение не может быть меньше 0.")
                                                  Integer from,
                                                  @RequestParam(defaultValue = "10")
                                                  @Min(value = 1, message = "Значение не может быть меньше 1.")
                                                  Integer size) {
        BookingState.parseFrom(state)
                .orElseThrow(() -> new BadRequestException("Unknown state: " + state));
        Pageable pageable = PageRequest.of(from / size, size);
        return bookingService.readAllBookerBookings(userId, state, pageable);
    }

    @GetMapping("/owner")
    public List<BookingDtoOutput> findOwnerBookings(@RequestHeader("X-Sharer-User-Id") long ownerId,
                                                    @RequestParam(defaultValue = "ALL") String state,
                                                    @RequestParam(defaultValue = "0")
                                                    @Min(value = 0, message = "Значение не может быть меньше 0.")
                                                    Integer from,
                                                    @RequestParam(defaultValue = "10")
                                                    @Min(value = 1, message = "Значение не может быть меньше 1.")
                                                    Integer size) {
        BookingState.parseFrom(state)
                .orElseThrow(() -> new BadRequestException("Unknown state: " + state));
        Pageable pageable = PageRequest.of(from / size, size);
        return bookingService.readAllOwnerItemBookings(ownerId, state, pageable);
    }

    @PatchMapping("/{id}")
    public BookingDtoOutput updateBooking(@RequestHeader(header) long ownerId,
                                          @PathVariable long id,
                                          @RequestParam("approved") Boolean isApproved) {
        return bookingService.update(ownerId, id, isApproved);
    }
}