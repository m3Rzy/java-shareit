package ru.practicum.shareit.unit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import ru.practicum.shareit.booking.dto.BookingDtoInput;
import ru.practicum.shareit.booking.dto.BookingDtoOutput;
import ru.practicum.shareit.booking.mapper.BookingMapper;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.Status;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.booking.service.BookingServiceImpl;
import ru.practicum.shareit.item.dto.ItemDtoInput;
import ru.practicum.shareit.item.mapper.ItemMapper;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.mapper.UserMapper;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(MockitoExtension.class)
public class BookingServiceUnitTest {
    @Mock
    private BookingRepository bookingRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private ItemRepository itemRepository;
    @InjectMocks
    private BookingServiceImpl bookingService;
    User user1;
    User user2;
    UserDto userDto1;
    UserDto userDto2;
    Item item1;
    Item item2;
    ItemDtoInput itemDtoInput1;
    ItemDtoInput itemDtoInput2;
    Booking booking1;
    Booking booking2;
    Booking booking3;
    BookingDtoInput currentBooking;
    BookingDtoInput pastBooking;
    BookingDtoInput futureBooking;
    Pageable pageable;

    @BeforeEach
    void setUp() {
        userDto1 = new UserDto(1, "User1", "user1@mail.ru");
        user1 = UserMapper.mapToUser(userDto1);

        userDto2 = new UserDto(2, "User2", "user2@mail.ru");
        user2 = UserMapper.mapToUser(userDto2);

        itemDtoInput1 = new ItemDtoInput(1, "Item1", "Item1 Description", true, 0);
        item1 = ItemMapper.mapToItem(itemDtoInput1, user1, null);

        itemDtoInput2 = new ItemDtoInput(2, "Item2", "Item2 Description", true, 0);
        item2 = ItemMapper.mapToItem(itemDtoInput2, user1, null);

        currentBooking = new BookingDtoInput(1, LocalDateTime.now(), LocalDateTime.now().plusHours(2), 1);
        booking1 = BookingMapper.mapToBooking(currentBooking, item1, user2, Status.WAITING);

        pastBooking = new BookingDtoInput(2,
                LocalDateTime.of(2023, 8, 1, 10, 0, 0),
                LocalDateTime.of(2023, 8, 8, 10, 0, 0),
                1);
        booking2 = BookingMapper.mapToBooking(pastBooking, item1, user2, Status.WAITING);

        futureBooking = new BookingDtoInput(3, LocalDateTime.now().plusHours(1),
                LocalDateTime.now().plusHours(2), 2);
        booking3 = BookingMapper.mapToBooking(futureBooking, item2, user2, Status.REJECTED);

        pageable = PageRequest.of(0, 10);
    }

    @Test
    void create_whenAllIsOk_thenReturnedBooking() {
        Mockito.when(userRepository.findById(2L)).thenReturn(Optional.of(user2));
        System.out.println(userRepository.findById(2L));

        Mockito.when(itemRepository.findById(1L)).thenReturn(Optional.of(item1));
        System.out.println(itemRepository.findById(1L));

        Mockito.when(bookingRepository.save(Mockito.any())).thenReturn(booking1);
        System.out.println(booking1);

        BookingDtoOutput createdBooking = bookingService.create(user2.getId(), new BookingDtoInput(1, LocalDateTime.now(), LocalDateTime.now().plusHours(2), 1));

        Mockito.verify(bookingRepository).save(Mockito.any());

        assertEquals(currentBooking.getStart(), createdBooking.getStart());
        assertEquals(currentBooking.getEnd(), createdBooking.getEnd());
        assertEquals(currentBooking.getItemId(), createdBooking.getItem().getId());
    }
}