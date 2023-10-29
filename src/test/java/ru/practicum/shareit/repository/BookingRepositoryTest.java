package ru.practicum.shareit.repository;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static ru.practicum.shareit.booking.model.Status.WAITING;

@DataJpaTest
public class BookingRepositoryTest {
    public Item item;
    public User booker;
    public Booking booking;
    public Pageable pageable;

    @Autowired
    private BookingRepository bookingRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private ItemRepository itemRepository;


    @AfterEach
    void clear() {
        bookingRepository.deleteAll();
        itemRepository.deleteAll();
        userRepository.deleteAll();
    }

    @BeforeEach
    void setUp() {
        new User();
        User user = User
                .builder()
                .name("Юзер1")
                .email("test@test.test")
                .build();
        userRepository.save(user);

        new Item();
        item = Item
                .builder()
                .name("Предмет1")
                .description("Описание предмета1")
                .available(true)
                .owner(user)
                .itemRequest(null)
                .build();
        itemRepository.save(item);

        new User();
        booker = User
                .builder()
                .name("Владелец1")
                .email("test2@test.test")
                .build();
        userRepository.save(booker);

        new Booking();
        booking = Booking
                .builder()
                .start(LocalDateTime.now().plusHours(1))
                .end(LocalDateTime.now().plusHours(2))
                .item(item)
                .booker(booker)
                .status(WAITING)
                .build();
        bookingRepository.save(booking);

        pageable = PageRequest.of(0, 10);
    }

    @Test
    void shouldGetAllOwnerItemsCurrentBookings() {
        booking.setStart(LocalDateTime.now().minusHours(2));
        bookingRepository.save(booking);

        Page<Booking> currentBookings = bookingRepository
                .readAllOwnerItemsCurrentBookings(pageable, List.of(item.getId()), LocalDateTime.now());

        assertEquals(1, currentBookings.getContent().size());
        assertEquals(booking.getStart(), currentBookings.getContent().get(0).getStart());
        assertEquals(booking.getEnd(), currentBookings.getContent().get(0).getEnd());
        assertEquals(booking.getItem().getId(), currentBookings.getContent().get(0).getItem().getId());
        assertEquals(booking.getBooker().getId(), currentBookings.getContent().get(0).getBooker().getId());
        assertEquals(booking.getStatus(), currentBookings.getContent().get(0).getStatus());
    }

    @Test
    void shouldGetAllOwnerItemsPastBookings() {
        booking.setStart(LocalDateTime.now().minusHours(3));
        booking.setEnd(LocalDateTime.now().minusHours(3));
        bookingRepository.save(booking);

        Page<Booking> pastBookings = bookingRepository
                .readAllOwnerItemsPastBookings(pageable, List.of(item.getId()), LocalDateTime.now());

        assertEquals(1, pastBookings.getContent().size());
        assertEquals(booking.getStart(), pastBookings.getContent().get(0).getStart());
        assertEquals(booking.getEnd(), pastBookings.getContent().get(0).getEnd());
        assertEquals(booking.getItem().getId(), pastBookings.getContent().get(0).getItem().getId());
        assertEquals(booking.getBooker().getId(), pastBookings.getContent().get(0).getBooker().getId());
        assertEquals(booking.getStatus(), pastBookings.getContent().get(0).getStatus());
    }

    @Test
    void shouldGetAllOwnerItemsFutureBookings() {
        Page<Booking> futureBookings = bookingRepository
                .readAllOwnerItemsFutureBookings(pageable, List.of(item.getId()), LocalDateTime.now());

        assertEquals(1, futureBookings.getContent().size());
        assertEquals(booking.getStart(), futureBookings.getContent().get(0).getStart());
        assertEquals(booking.getEnd(), futureBookings.getContent().get(0).getEnd());
        assertEquals(booking.getItem().getId(), futureBookings.getContent().get(0).getItem().getId());
        assertEquals(booking.getBooker().getId(), futureBookings.getContent().get(0).getBooker().getId());
        assertEquals(booking.getStatus(), futureBookings.getContent().get(0).getStatus());
    }

    @Test
    void shouldGetAllBookerCurrentBookings() {
        booking.setStart(LocalDateTime.now().minusHours(2));
        bookingRepository.save(booking);

        Page<Booking> currentBookings = bookingRepository
                .readAllBookerCurrentBookings(pageable, booker.getId(), LocalDateTime.now());

        assertEquals(1, currentBookings.getContent().size());
        assertEquals(booking.getStart(), currentBookings.getContent().get(0).getStart());
        assertEquals(booking.getEnd(), currentBookings.getContent().get(0).getEnd());
        assertEquals(booking.getItem().getId(), currentBookings.getContent().get(0).getItem().getId());
        assertEquals(booking.getBooker().getId(), currentBookings.getContent().get(0).getBooker().getId());
        assertEquals(booking.getStatus(), currentBookings.getContent().get(0).getStatus());
    }

    @Test
    void shouldGetAllBookerPastBookings() {
        booking.setStart(LocalDateTime.now().minusHours(3));
        booking.setEnd(LocalDateTime.now().minusHours(3));
        bookingRepository.save(booking);

        Page<Booking> pastBookings = bookingRepository
                .readAllBookerPastBookings(pageable, booker.getId(), LocalDateTime.now());

        assertEquals(1, pastBookings.getContent().size());
        assertEquals(booking.getStart(), pastBookings.getContent().get(0).getStart());
        assertEquals(booking.getEnd(), pastBookings.getContent().get(0).getEnd());
        assertEquals(booking.getItem().getId(), pastBookings.getContent().get(0).getItem().getId());
        assertEquals(booking.getBooker().getId(), pastBookings.getContent().get(0).getBooker().getId());
        assertEquals(booking.getStatus(), pastBookings.getContent().get(0).getStatus());
    }

    @Test
    void shouldGetAllFutureBookings() {
        Page<Booking> futureBookings = bookingRepository
                .readAllBookerFutureBookings(pageable, booker.getId(), LocalDateTime.now());

        assertEquals(1, futureBookings.getContent().size());
        assertEquals(booking.getStart(), futureBookings.getContent().get(0).getStart());
        assertEquals(booking.getEnd(), futureBookings.getContent().get(0).getEnd());
        assertEquals(booking.getItem().getId(), futureBookings.getContent().get(0).getItem().getId());
        assertEquals(booking.getBooker().getId(), futureBookings.getContent().get(0).getBooker().getId());
        assertEquals(booking.getStatus(), futureBookings.getContent().get(0).getStatus());
    }
}
