package ru.practicum.shareit.booking.service;

import lombok.AllArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.dto.BookingDtoInput;
import ru.practicum.shareit.booking.dto.BookingDtoOutput;
import ru.practicum.shareit.booking.mapper.BookingMapper;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.exception.BadRequestException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import static ru.practicum.shareit.booking.model.Status.*;

@Service
@AllArgsConstructor
public class BookingServiceImpl implements BookingService {

    private final BookingRepository bookingRepository;
    private final ItemRepository itemRepository;
    private final UserRepository userRepository;


    @Override
    @Transactional
    public BookingDtoOutput create(long userId, BookingDtoInput bookingDtoInput) {

        if (!isBefore(bookingDtoInput)) {
            throw new BadRequestException("Ошибка со временем." +
                    "start: " + bookingDtoInput.getStart() + " end: " + bookingDtoInput.getEnd() + " now: ");
        }

        User booker = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователя с id " + userId + " не существует!"));

        Item item = itemRepository.findById(bookingDtoInput.getItemId())
                .orElseThrow(() -> new NotFoundException("Вещь с id " + bookingDtoInput.getItemId() + " не найдена"));

        if (item.getOwner().getId() == userId) {
            throw new NotFoundException("Повторное бронирование невозможно.");
        }
        System.out.println("до проверки available");
        if (!item.getAvailable()) {
            System.out.println(item.getAvailable());
            throw new BadRequestException("Ошибка бронирования!");
        }
        System.out.println("после проверки");
        System.out.println(item.getAvailable());



        Booking booking = BookingMapper.mapToBooking(bookingDtoInput, item, booker, WAITING);
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
    public BookingDtoOutput update(long userId, long id, Boolean isApproved) {
        userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователя с id " + userId + " не существует!"));

        Booking booking = bookingRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Бронирования с id " + id + " не существует!"));

        if (booking.getItem().getOwner().getId() != userId) {
            throw new NotFoundException("Пользователь с id " + userId
                    + " не может редактировать бронирование с id " + id);
        }

        if (booking.getStatus().equals(APPROVED)) {
            throw new BadRequestException("Бронирование уже подтверждено.");
        }

        if (isApproved) {
            booking.setStatus(APPROVED);
        } else {
            booking.setStatus(REJECTED);
        }

        return BookingMapper.mapToBookingDtoOutput(bookingRepository.save(booking));
    }

    @Override
    @Transactional
    public List<BookingDtoOutput> readAllBookerBookings(long bookerId, String state, Pageable pageable) {
        userRepository.findById(bookerId)
                .orElseThrow(() -> new NotFoundException("Пользователя с id " + bookerId + " не существует!"));

        switch (state) {
            case "CURRENT":
                return bookingRepository.readAllBookerCurrentBookings(pageable, bookerId, LocalDateTime.now())
                        .getContent()
                        .stream()
                        .map(BookingMapper::mapToBookingDtoOutput)
                        .collect(Collectors.toList());
            case "PAST":
                return bookingRepository.readAllBookerPastBookings(pageable, bookerId, LocalDateTime.now())
                        .getContent()
                        .stream()
                        .map(BookingMapper::mapToBookingDtoOutput)
                        .collect(Collectors.toList());
            case "FUTURE":
                return bookingRepository.readAllBookerFutureBookings(pageable, bookerId, LocalDateTime.now())
                        .getContent()
                        .stream()
                        .map(BookingMapper::mapToBookingDtoOutput)
                        .collect(Collectors.toList());
            case "WAITING":
                return bookingRepository
                        .findAllByBooker_IdAndStatusInOrderByStartDesc(pageable, bookerId,
                                List.of(WAITING))
                        .getContent()
                        .stream()
                        .map(BookingMapper::mapToBookingDtoOutput)
                        .collect(Collectors.toList());
            case "REJECTED":
                return bookingRepository
                        .findAllByBooker_IdAndStatusInOrderByStartDesc(pageable, bookerId,
                                List.of(REJECTED))
                        .getContent()
                        .stream()
                        .map(BookingMapper::mapToBookingDtoOutput)
                        .collect(Collectors.toList());
            default:
                return bookingRepository.findAllByBooker_IdOrderByStartDesc(pageable, bookerId)
                        .getContent()
                        .stream()
                        .map(BookingMapper::mapToBookingDtoOutput)
                        .collect(Collectors.toList());
        }
    }

    @Override
    @Transactional
    public List<BookingDtoOutput> readAllOwnerItemBookings(long ownerId, String state, Pageable pageable) {
        userRepository.findById(ownerId)
                .orElseThrow(() -> new NotFoundException("Пользователя с id " + ownerId + " не существует!"));

        List<Long> userItems = itemRepository.findAllByOwnerId(ownerId)
                .stream()
                .map(Item::getId)
                .collect(Collectors.toList());

        switch (state) {
            case "CURRENT":
                return bookingRepository.readAllOwnerItemsCurrentBookings(pageable, userItems, LocalDateTime.now())
                        .getContent()
                        .stream()
                        .map(BookingMapper::mapToBookingDtoOutput)
                        .collect(Collectors.toList());
            case "PAST":
                return bookingRepository.readAllOwnerItemsPastBookings(pageable, userItems, LocalDateTime.now())
                        .getContent()
                        .stream()
                        .map(BookingMapper::mapToBookingDtoOutput)
                        .collect(Collectors.toList());
            case "FUTURE":
                return bookingRepository.readAllOwnerItemsFutureBookings(pageable, userItems, LocalDateTime.now())
                        .getContent()
                        .stream()
                        .map(BookingMapper::mapToBookingDtoOutput)
                        .collect(Collectors.toList());
            case "WAITING":
                return bookingRepository
                        .findAllByItem_IdInAndStatusInOrderByStartDesc(pageable, userItems,
                                List.of(WAITING))
                        .getContent()
                        .stream()
                        .map(BookingMapper::mapToBookingDtoOutput)
                        .collect(Collectors.toList());
            case "REJECTED":
                return bookingRepository
                        .findAllByItem_IdInAndStatusInOrderByStartDesc(pageable, userItems,
                                List.of(REJECTED))
                        .getContent()
                        .stream()
                        .map(BookingMapper::mapToBookingDtoOutput)
                        .collect(Collectors.toList());
            default:
                return bookingRepository.findAllByItem_IdInOrderByStartDesc(pageable, userItems)
                        .getContent()
                        .stream()
                        .map(BookingMapper::mapToBookingDtoOutput)
                        .collect(Collectors.toList());
        }
    }

    private boolean isBefore(BookingDtoInput dto) {
        return dto.getStart().isBefore(dto.getEnd());
    }
}