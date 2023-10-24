package ru.practicum.shareit.booking.service;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.dto.BookingDtoInput;
import ru.practicum.shareit.booking.dto.BookingDtoOutput;
import ru.practicum.shareit.booking.mapper.BookingMapper;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.Status;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.exception.BadRequestException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.service.ItemService;
import ru.practicum.shareit.user.mapper.UserMapper;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.service.UserService;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class BookingServiceImpl implements BookingService {

    private final BookingRepository bookingRepository;

    private final UserService userService;
    private final ItemService itemService;

    @Override
    @Transactional
    public BookingDtoOutput create(long userId, BookingDtoInput bookingDtoInput) {

        if (!isBefore(bookingDtoInput)) {
            throw new BadRequestException("Ошибка со временем." +
                    "start: " + bookingDtoInput.getStart() + " end: " + bookingDtoInput.getEnd() + " now: ");
        }

        User booker = UserMapper.mapToUser(userService.getById(userId));

        Item item = itemService.getByItemId(bookingDtoInput.getItemId());

        if (item.getOwner().getId() == userId) {
            throw new NotFoundException("Повторное бронирование невозможно.");
        }

        if (!item.getAvailable()) {
            throw new BadRequestException("Возникла ошибка с бронированием предмета " + bookingDtoInput.getItemId()
                    + ".");
        }
        Booking booking = BookingMapper.mapToBooking(bookingDtoInput, item, booker, Status.WAITING);
        return BookingMapper.mapToBookingDtoOutput(bookingRepository.save(booking));
    }

    @Override
    @Transactional
    public BookingDtoOutput getById(long userId, long id) {
        Booking booking = bookingRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Бронирования с id " + id + " не существует!"));

        long bookerId = booking.getBooker().getId();
        long ownerId = booking.getItem().getOwner().getId();

        if (bookerId != userId && ownerId != userId) {
            throw new NotFoundException("Бронирование с id " + id + " не найдено для пользователя с id " + userId + ".");
        }
        return BookingMapper.mapToBookingDtoOutput(booking);
    }

    @Override
    @Transactional
    public List<BookingDtoOutput> getAll(long userId, String state) {
        userService.getById(userId);

        switch (state) {
            case "CURRENT":
                return bookingRepository.readAllBookerCurrentBookings(userId, LocalDateTime.now())
                        .stream()
                        .map(BookingMapper::mapToBookingDtoOutput)
                        .collect(Collectors.toList());
            case "PAST":
                return bookingRepository.readAllBookerPastBookings(userId, LocalDateTime.now())
                        .stream()
                        .map(BookingMapper::mapToBookingDtoOutput)
                        .collect(Collectors.toList());
            case "FUTURE":
                return bookingRepository.readAllBookerFutureBookings(userId, LocalDateTime.now())
                        .stream()
                        .map(BookingMapper::mapToBookingDtoOutput)
                        .collect(Collectors.toList());
            case "WAITING":
                return bookingRepository
                        .findAllByBooker_IdAndStatusInOrderByStartDesc(userId, List.of(Status.WAITING))
                        .stream()
                        .map(BookingMapper::mapToBookingDtoOutput)
                        .collect(Collectors.toList());
            case "REJECTED":
                return bookingRepository
                        .findAllByBooker_IdAndStatusInOrderByStartDesc(userId, List.of(Status.REJECTED))
                        .stream()
                        .map(BookingMapper::mapToBookingDtoOutput)
                        .collect(Collectors.toList());
            default:
                return bookingRepository.findAllByBooker_IdOrderByStartDesc(userId)
                        .stream()
                        .map(BookingMapper::mapToBookingDtoOutput)
                        .collect(Collectors.toList());
        }
    }

    @Override
    @Transactional
    public List<BookingDtoOutput> getOwner(long userId, String state) {
        userService.getById(userId);

        List<Long> userItems = itemService.getAllItemsId(userId);

        switch (state) {
            case "CURRENT":
                return bookingRepository.readAllOwnerItemsCurrentBookings(userItems, LocalDateTime.now())
                        .stream()
                        .map(BookingMapper::mapToBookingDtoOutput)
                        .collect(Collectors.toList());
            case "PAST":
                return bookingRepository.readAllOwnerItemsPastBookings(userItems, LocalDateTime.now())
                        .stream()
                        .map(BookingMapper::mapToBookingDtoOutput)
                        .collect(Collectors.toList());
            case "FUTURE":
                return bookingRepository.readAllOwnerItemsFutureBookings(userItems, LocalDateTime.now())
                        .stream()
                        .map(BookingMapper::mapToBookingDtoOutput)
                        .collect(Collectors.toList());
            case "WAITING":
                return bookingRepository
                        .findAllByItem_IdInAndStatusInOrderByStartDesc(userItems, List.of(Status.WAITING))
                        .stream()
                        .map(BookingMapper::mapToBookingDtoOutput)
                        .collect(Collectors.toList());
            case "REJECTED":
                return bookingRepository
                        .findAllByItem_IdInAndStatusInOrderByStartDesc(userItems, List.of(Status.REJECTED))
                        .stream()
                        .map(BookingMapper::mapToBookingDtoOutput)
                        .collect(Collectors.toList());
            default:
                return bookingRepository.findAllByItem_IdInOrderByStartDesc(userItems)
                        .stream()
                        .map(BookingMapper::mapToBookingDtoOutput)
                        .collect(Collectors.toList());
        }
    }

    @Override
    @Transactional
    public BookingDtoOutput update(long userId, long id, Boolean isApproved) {
        userService.getById(userId);

        Booking booking = bookingRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Бронирования с id " + id + " не существует!"));

        if (booking.getItem().getOwner().getId() != userId) {
            throw new NotFoundException("Пользователь с id " + userId
                    + " не может редактировать бронирование с id " + id);
        }

        if (booking.getStatus().equals(Status.APPROVED)) {
            throw new BadRequestException("Бронирование уже подтверждено.");
        }

        if (isApproved) {
            booking.setStatus(Status.APPROVED);
        } else {
            booking.setStatus(Status.REJECTED);
        }

        return BookingMapper.mapToBookingDtoOutput(bookingRepository.save(booking));
    }

    private boolean isBefore(BookingDtoInput dto) {
        return dto.getStart().isBefore(dto.getEnd());
    }
}