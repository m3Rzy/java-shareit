package ru.practicum.shareit.integration;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.annotation.DirtiesContext;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.dto.ItemRequestDtoOutput;
import ru.practicum.shareit.request.service.ItemRequestService;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.service.UserService;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
public class ItemRequestIntegrationTest {
    @Autowired
    private ItemRequestService itemRequestService;

    @Autowired
    private UserService userService;

    private UserDto userDto1;
    private UserDto userDto2;
    private ItemRequestDto itemRequestDtoInput1;
    private ItemRequestDto itemRequestDtoInput2;
    private Pageable pageable;

    @BeforeEach
    void setUp() {
        userDto1 = new UserDto(1, "Тестовый пользователь 1", "test1@yandex.ru");
        userDto2 = new UserDto(2, "Тестовый пользователь 2", "test2@yandex.ru");

        itemRequestDtoInput1 = new ItemRequestDto(1, "Тестовое описание 1");
        itemRequestDtoInput2 = new ItemRequestDto(2, "Тестовое описание 2");

        pageable = PageRequest.of(0, 10);
    }

    @Test
    void shouldCreateItemRequest() {
        UserDto savedRequestor = userService.create(userDto2);

        ItemRequestDtoOutput savedRequest = itemRequestService.create(savedRequestor.getId(), itemRequestDtoInput1);

        assertEquals(itemRequestDtoInput1.getDescription(), savedRequest.getDescription());
        assertEquals(0, savedRequest.getItems().size());

        LocalDateTime expectedCreated = LocalDateTime.now();
        LocalDateTime actualCreated = savedRequest.getCreated();

        assertTrue(ChronoUnit.MILLIS.between(expectedCreated, actualCreated) < 1000);
    }

    @Test
    void shouldNotCreateItemRequest_requestorNotFound() {
        Throwable thrown = assertThrows(NotFoundException.class,
                () -> itemRequestService.create(9999, itemRequestDtoInput1));
        assertEquals("Такого пользователя не существует!", thrown.getMessage());
    }

    @Test
    void shouldGetAllRequestorRequests() {
        UserDto savedRequestor = userService.create(userDto2);

        ItemRequestDtoOutput savedRequest1 = itemRequestService.create(savedRequestor.getId(), itemRequestDtoInput1);
        ItemRequestDtoOutput savedRequest2 = itemRequestService.create(savedRequestor.getId(), itemRequestDtoInput2);

        List<ItemRequestDtoOutput> requestorRequests = new ArrayList<>(itemRequestService
                .getAllByRequestor(savedRequestor.getId()));

        assertEquals(2, requestorRequests.size());

        assertEquals(savedRequest2.getDescription(), requestorRequests.get(0).getDescription());
        assertEquals(savedRequest2.getItems(), requestorRequests.get(0).getItems());

        LocalDateTime expectedCreated1 = savedRequest2.getCreated();
        LocalDateTime actualCreated1 = requestorRequests.get(0).getCreated();

        assertTrue(ChronoUnit.MILLIS.between(expectedCreated1, actualCreated1) < 1000);

        assertEquals(savedRequest1.getDescription(), requestorRequests.get(1).getDescription());
        assertEquals(savedRequest1.getItems(), requestorRequests.get(1).getItems());

        LocalDateTime expectedCreated2 = savedRequest1.getCreated();
        LocalDateTime actualCreated2 = requestorRequests.get(1).getCreated();

        assertTrue(ChronoUnit.MILLIS.between(expectedCreated2, actualCreated2) < 1000);
    }

    @Test
    void shouldNotGetAllRequestorRequests_requestorNotFound() {
        Throwable thrown = assertThrows(NotFoundException.class,
                () -> itemRequestService.getAllByRequestor(9999));
        assertEquals("Такого пользователя не существует!", thrown.getMessage());
    }

    @Test
    void shouldGetAllUsersRequests() {
        UserDto savedRequestor = userService.create(userDto2);
        ItemRequestDtoOutput savedRequest = itemRequestService.create(savedRequestor.getId(), itemRequestDtoInput1);
        UserDto savedUser = userService.create(userDto1);

        ItemRequestDtoOutput returnedRequest = itemRequestService.getById(savedUser.getId(), savedRequest.getId());

        assertEquals(itemRequestDtoInput1.getDescription(), returnedRequest.getDescription());
        assertEquals(0, returnedRequest.getItems().size());

        LocalDateTime expectedCreated = LocalDateTime.now();
        LocalDateTime actualCreated = returnedRequest.getCreated();

        assertTrue(ChronoUnit.MILLIS.between(expectedCreated, actualCreated) < 1000);
    }

    @Test
    void shouldNotGetAllUsersRequests_userNotFound() {
        Throwable thrown = assertThrows(NotFoundException.class,
                () -> itemRequestService.getAllUsersRequests(9999, pageable));
        assertEquals("Такого пользователя не существует!", thrown.getMessage());
    }

    @Test
    void shouldNotGetById_userNotFound() {
        Throwable thrown = assertThrows(NotFoundException.class, () -> itemRequestService.getById(9999, 1));
        assertEquals("Такого пользователя не существует!", thrown.getMessage());
    }

    @Test
    void shouldNotGetById_itemNotFound() {
        UserDto savedRequestor = userService.create(userDto2);

        Throwable thrown = assertThrows(NotFoundException.class,
                () -> itemRequestService.getById(savedRequestor.getId(), 999));
        assertEquals("Такого запроса не существует.", thrown.getMessage());
    }
}
