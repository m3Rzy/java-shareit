package ru.practicum.shareit.integration;


import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.annotation.DirtiesContext;
import ru.practicum.shareit.booking.dto.BookingDtoInput;
import ru.practicum.shareit.booking.dto.BookingDtoOutput;
import ru.practicum.shareit.booking.service.BookingService;
import ru.practicum.shareit.comment.dto.CommentDto;
import ru.practicum.shareit.exception.BadRequestException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.dto.ItemDtoComment;
import ru.practicum.shareit.item.dto.ItemDtoInput;
import ru.practicum.shareit.item.dto.ItemDtoOutput;
import ru.practicum.shareit.item.dto.ItemDtoRequest;
import ru.practicum.shareit.item.service.ItemService;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.dto.ItemRequestDtoOutput;
import ru.practicum.shareit.request.service.ItemRequestService;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.service.UserService;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
public class ItemIntegrationTest {
    @Autowired
    private ItemService itemService;
    @Autowired
    private UserService userService;
    @Autowired
    private BookingService bookingService;
    @Autowired
    private ItemRequestService itemRequestService;

    private UserDto userDto1;
    private UserDto userDto2;
    private ItemDtoInput itemDtoInput1;
    private ItemDtoInput itemDtoInput2;
    private ItemDtoInput updatedItemDtoInput;
    private ItemDtoInput itemDtoInputWithoutName;
    private ItemDtoInput itemDtoInputWithoutDescription;
    private ItemDtoInput itemDtoInputWithoutAvailable;
    private ItemDtoInput itemDtoInputNothingToUpdate;
    private BookingDtoInput lastBooking;
    private BookingDtoInput nextBooking;
    private Pageable pageable;
    private CommentDto commentDto;

    @BeforeEach
    void setUp() {
        userDto1 = new UserDto(1, "Тестовый пользователь 1", "test1@yandex.ru");
        userDto2 = new UserDto(2, "Тестовый пользователь 2", "test2@yandex.ru");

        itemDtoInput1 = new ItemDtoInput(1, "Тестовый предмет 1",
                "Описание 1", true, 0);
        itemDtoInput2 = new ItemDtoInput(2, "Тестовый предмет 2",
                "Описание 2", true, 0);
        updatedItemDtoInput = new ItemDtoInput(1, "Item1 Updated",
                "Описание 1 обновлено", false, 0);

        lastBooking = new BookingDtoInput(1,
                LocalDateTime.of(2023, 8, 1, 10, 0, 0),
                LocalDateTime.of(2023, 8, 8, 10, 0, 0),
                1);
        nextBooking = new BookingDtoInput(2, LocalDateTime.now().plusHours(1),
                LocalDateTime.now().plusHours(2), 1);

        pageable = PageRequest.of(0, 10);

        commentDto = new CommentDto(1, "10/10", "Тестовый пользователь 2",
                LocalDateTime.of(2023, 8, 8, 12, 0, 0));
    }

    @Test
    void shouldCreateItem() {
        UserDto savedOwner = userService.create(userDto1);

        ItemDtoRequest savedItem = itemService.create(savedOwner.getId(), itemDtoInput1);

        assertEquals(itemDtoInput1.getName(), savedItem.getName());
        assertEquals(itemDtoInput1.getDescription(), savedItem.getDescription());
        assertEquals(itemDtoInput1.getAvailable(), savedItem.getAvailable());
        assertEquals(itemDtoInput1.getRequestId(), savedItem.getRequestId());
    }

    @Test
    void shouldCreateItemWithRequest() {
        UserDto savedOwner = userService.create(userDto1);
        UserDto savedRequestor = userService.create(userDto2);
        ItemRequestDto itemRequestDtoInput = new ItemRequestDto(1, "I need a description");
        ItemRequestDtoOutput itemRequestDtoOutput = itemRequestService.create(savedRequestor.getId(),
                itemRequestDtoInput);

        itemDtoInput1.setRequestId(itemRequestDtoOutput.getId());

        ItemDtoRequest savedItem = itemService.create(savedOwner.getId(), itemDtoInput1);

        assertEquals(itemDtoInput1.getName(), savedItem.getName());
        assertEquals(itemDtoInput1.getDescription(), savedItem.getDescription());
        assertEquals(itemDtoInput1.getAvailable(), savedItem.getAvailable());

        assertEquals(itemRequestDtoOutput.getId(), savedItem.getRequestId());
    }

    @Test
    void shouldNotCreateItem_ownerNotFound() {
        Throwable thrown = assertThrows(NotFoundException.class, () -> itemService.create(9999, itemDtoInput1));
        assertEquals("Пользователя с id 9999 не существует!", thrown.getMessage());
    }

    @Test
    void shouldGetItem_userIsNotOwner() {
        UserDto savedOwner = userService.create(userDto1);
        UserDto savedUser = userService.create(userDto2);
        ItemDtoRequest savedItem = itemService.create(savedOwner.getId(), itemDtoInput1);

        ItemDtoComment returnedItem = itemService.getById(savedUser.getId(), savedItem.getId());

        assertEquals(itemDtoInput1.getName(), returnedItem.getName());
        assertEquals(itemDtoInput1.getDescription(), returnedItem.getDescription());
        assertEquals(itemDtoInput1.getAvailable(), returnedItem.getAvailable());
        assertNull(returnedItem.getLastBooking());
        assertNull(returnedItem.getNextBooking());
    }

    @Test
    void shouldNotGetUser_userNotFound() {
        UserDto savedOwner = userService.create(userDto1);
        ItemDtoRequest savedItem = itemService.create(savedOwner.getId(), itemDtoInput1);

        Throwable thrown = assertThrows(NotFoundException.class, () -> itemService.getById(9999, savedItem.getId()));
        assertEquals("Пользователя с id 9999 не существует!", thrown.getMessage());
    }

    @Test
    void shouldNotGetItem_itemNotFound() {
        UserDto savedOwner = userService.create(userDto1);

        Throwable thrown = assertThrows(NotFoundException.class, () -> itemService.getById(savedOwner.getId(), 9999));
        assertEquals("Предмета с id 9999 не существует!", thrown.getMessage());
    }

    @Test
    void shouldGetItem_userIsOwner() {
        UserDto savedOwner = userService.create(userDto1);
        UserDto savedBooker = userService.create(userDto2);
        ItemDtoRequest savedItem = itemService.create(savedOwner.getId(), itemDtoInput1);
        BookingDtoOutput savedLastBooking = bookingService.create(savedBooker.getId(), lastBooking);
        BookingDtoOutput savedNextBooking = bookingService.create(savedBooker.getId(), nextBooking);

        bookingService.update(savedOwner.getId(), savedLastBooking.getId(), true);
        bookingService.update(savedOwner.getId(), savedNextBooking.getId(), true);

        ItemDtoComment returnedItem = itemService.getById(savedOwner.getId(), savedItem.getId());

        assertEquals(itemDtoInput1.getName(), returnedItem.getName());
        assertEquals(itemDtoInput1.getDescription(), returnedItem.getDescription());
        assertEquals(itemDtoInput1.getAvailable(), returnedItem.getAvailable());
        assertEquals(1, returnedItem.getLastBooking().getId());
        assertEquals(2, returnedItem.getLastBooking().getBookerId());
        assertEquals(2, returnedItem.getNextBooking().getId());
        assertEquals(2, returnedItem.getNextBooking().getBookerId());
    }

    @Test
    void shouldGetItems() {
        UserDto savedOwner = userService.create(userDto1);
        UserDto savedBooker = userService.create(userDto2);
        ItemDtoRequest savedItem1 = itemService.create(savedOwner.getId(), itemDtoInput1);
        ItemDtoRequest savedItem2 = itemService.create(savedOwner.getId(), itemDtoInput2);
        BookingDtoOutput savedLastBooking = bookingService.create(savedBooker.getId(), lastBooking);
        BookingDtoOutput savedNextBooking = bookingService.create(savedBooker.getId(), nextBooking);

        bookingService.update(savedOwner.getId(), savedLastBooking.getId(), true);
        bookingService.update(savedOwner.getId(), savedNextBooking.getId(), true);

        List<ItemDtoRequest> expectedItems = Stream.of(savedItem1, savedItem2)
                .sorted(Comparator.comparingLong(ItemDtoRequest::getId))
                .collect(Collectors.toList());

        List<ItemDtoComment> returnedItems = new ArrayList<>(itemService.getAll(savedOwner.getId(), pageable));

        assertEquals(expectedItems.size(), returnedItems.size());

        assertEquals(expectedItems.get(0).getId(), returnedItems.get(0).getId());
        assertEquals(expectedItems.get(0).getName(), returnedItems.get(0).getName());
        assertEquals(expectedItems.get(0).getDescription(), returnedItems.get(0).getDescription());
        assertEquals(expectedItems.get(0).getAvailable(), returnedItems.get(0).getAvailable());
        assertEquals(1, returnedItems.get(0).getLastBooking().getId());
        assertEquals(2, returnedItems.get(0).getLastBooking().getBookerId());
        assertEquals(2, returnedItems.get(0).getNextBooking().getId());
        assertEquals(2, returnedItems.get(0).getNextBooking().getBookerId());

        assertEquals(expectedItems.get(1).getId(), returnedItems.get(1).getId());
        assertEquals(expectedItems.get(1).getName(), returnedItems.get(1).getName());
        assertEquals(expectedItems.get(1).getDescription(), returnedItems.get(1).getDescription());
        assertEquals(expectedItems.get(1).getAvailable(), returnedItems.get(1).getAvailable());
        assertNull(returnedItems.get(1).getLastBooking());
        assertNull(returnedItems.get(1).getNextBooking());
    }

    @Test
    void shouldNotGetItems_ownerNotFound() {
        Throwable thrown = assertThrows(NotFoundException.class, () -> itemService.getAll(9999, pageable));
        assertEquals("Пользователя с id 9999 не существует!", thrown.getMessage());
    }

    @Test
    void shouldUpdateItem() {
        UserDto savedOwner = userService.create(userDto1);
        ItemDtoRequest savedItem = itemService.create(savedOwner.getId(), itemDtoInput1);

        ItemDtoRequest updatedItem = itemService
                .update(savedOwner.getId(), updatedItemDtoInput, savedItem.getId());

        assertEquals(updatedItemDtoInput.getName(), updatedItem.getName());
        assertEquals(updatedItemDtoInput.getDescription(), updatedItem.getDescription());
        assertEquals(updatedItemDtoInput.getAvailable(), updatedItem.getAvailable());
    }

    @Test
    void shouldNotUpdateItem_itemNotFound() {
        UserDto savedOwner = userService.create(userDto1);

        Throwable thrown = assertThrows(NotFoundException.class,
                () -> itemService.update(savedOwner.getId(), updatedItemDtoInput, 9999));
        assertEquals("Пользователя с id 1 не существует!", thrown.getMessage());
    }

    @Test
    void shouldNotUpdateItem_notFound() {
        UserDto savedOwner = userService.create(userDto1);
        UserDto savedUser = userService.create(userDto2);
        ItemDtoRequest savedItem = itemService.create(savedOwner.getId(), itemDtoInput1);

        assertThrows(NotFoundException.class,
                () -> itemService.update(savedUser.getId(), updatedItemDtoInput, savedItem.getId()));
    }

    @Test
    void shouldUpdateItem_nameIsNull() {
        UserDto savedOwner = userService.create(userDto1);
        ItemDtoRequest savedItem = itemService.create(savedOwner.getId(), itemDtoInput1);
        itemDtoInputWithoutName = new ItemDtoInput();

        itemDtoInputWithoutName.setDescription("Item1 Description Updated");
        itemDtoInputWithoutName.setAvailable(false);

        ItemDtoRequest updatedItem = itemService.update(savedOwner.getId(),
                itemDtoInputWithoutName, savedItem.getId());

        assertEquals(itemDtoInput1.getName(), updatedItem.getName());
        assertEquals(itemDtoInputWithoutName.getDescription(), updatedItem.getDescription());
        assertEquals(itemDtoInputWithoutName.getAvailable(), updatedItem.getAvailable());
    }

    @Test
    void shouldUpdateItem_descIsNull() {
        UserDto savedOwner = userService.create(userDto1);
        ItemDtoRequest savedItem = itemService.create(savedOwner.getId(), itemDtoInput1);
        itemDtoInputWithoutDescription = new ItemDtoInput();

        itemDtoInputWithoutDescription.setName("Item1 Updated");
        itemDtoInputWithoutDescription.setAvailable(false);

        ItemDtoRequest updatedItem = itemService.update(savedOwner.getId(),
                itemDtoInputWithoutDescription, savedItem.getId());

        assertEquals(itemDtoInputWithoutDescription.getName(), updatedItem.getName());
        assertEquals(itemDtoInput1.getDescription(), updatedItem.getDescription());
        assertEquals(itemDtoInputWithoutDescription.getAvailable(), updatedItem.getAvailable());
    }

    @Test
    void shouldUpdateItem_availableIsNull() {
        UserDto savedOwner = userService.create(userDto1);
        ItemDtoRequest savedItem = itemService.create(savedOwner.getId(), itemDtoInput1);
        itemDtoInputWithoutAvailable = new ItemDtoInput();

        itemDtoInputWithoutAvailable.setName("Item1 Updated");
        itemDtoInputWithoutAvailable.setDescription("Item1 Description Updated");

        ItemDtoRequest updatedItem = itemService.update(savedOwner.getId(),
                itemDtoInputWithoutAvailable, savedItem.getId());

        assertEquals(itemDtoInputWithoutAvailable.getName(), updatedItem.getName());
        assertEquals(itemDtoInputWithoutAvailable.getDescription(), updatedItem.getDescription());
        assertEquals(itemDtoInput1.getAvailable(), updatedItem.getAvailable());
    }

    @Test
    void shouldUpdateItem_allIsNull() {
        UserDto savedOwner = userService.create(userDto1);
        ItemDtoRequest savedItem = itemService.create(savedOwner.getId(), itemDtoInput1);
        itemDtoInputNothingToUpdate = new ItemDtoInput();

        ItemDtoRequest updatedItem = itemService.update(savedOwner.getId(),
                itemDtoInputNothingToUpdate, savedItem.getId());

        assertEquals(itemDtoInput1.getName(), updatedItem.getName());
        assertEquals(itemDtoInput1.getDescription(), updatedItem.getDescription());
        assertEquals(itemDtoInput1.getAvailable(), updatedItem.getAvailable());
    }

    @Test
    void shouldSearchWithText() {
        UserDto savedOwner = userService.create(userDto1);
        ItemDtoRequest savedItem = itemService.create(savedOwner.getId(), itemDtoInput1);

        List<ItemDtoOutput> returnedItems = new ArrayList<>(itemService.search("тестовый", pageable));
        System.out.println(returnedItems);
        assertEquals(savedItem.getName(), returnedItems.get(0).getName());
        assertEquals(savedItem.getDescription(), returnedItems.get(0).getDescription());
        assertEquals(savedItem.getAvailable(), returnedItems.get(0).getAvailable());
    }

    @Test
    void shouldSearchWithoutText() {
        UserDto savedOwner = userService.create(userDto1);

        itemService.create(savedOwner.getId(), itemDtoInput1);

        Collection<ItemDtoOutput> returnedItems = itemService.search("", pageable);

        assertEquals(0, returnedItems.size());
    }

    @Test
    void shouldCreateComment() {
        UserDto savedOwner = userService.create(userDto1);
        UserDto savedAuthor = userService.create(userDto2);
        ItemDtoRequest savedItem = itemService.create(savedOwner.getId(), itemDtoInput1);
        BookingDtoOutput savedLastBooking = bookingService.create(savedAuthor.getId(), lastBooking);

        bookingService.update(savedOwner.getId(), savedLastBooking.getId(), true);

        CommentDto savedComment = itemService.comment(savedAuthor.getId(), commentDto, savedItem.getId());

        assertEquals(commentDto.getText(), savedComment.getText());
        assertEquals(commentDto.getAuthorName(), savedComment.getAuthorName());
        assertEquals(commentDto.getCreated(), savedComment.getCreated());
    }

    @Test
    void shouldNotCreateComment_authorNotFound() {
        UserDto savedOwner = userService.create(userDto1);
        ItemDtoRequest savedItem = itemService.create(savedOwner.getId(), itemDtoInput1);

        Throwable thrown = assertThrows(NotFoundException.class,
                () -> itemService.comment(9999, commentDto, savedItem.getId()));
        assertEquals("Пользователя с id 9999 не существует!", thrown.getMessage());
    }

    @Test
    void shouldNotCreateComment_itemNotFound() {
        UserDto savedAuthor = userService.create(userDto2);

        Throwable thrown = assertThrows(NotFoundException.class,
                () -> itemService.comment(savedAuthor.getId(), commentDto, 9999));
        assertEquals("Предмета с id 9999 не существует!", thrown.getMessage());
    }

    @Test
    void shouldNotCreateComment() {
        UserDto savedOwner = userService.create(userDto1);
        UserDto savedAuthor = userService.create(userDto2);
        ItemDtoRequest savedItem = itemService.create(savedOwner.getId(), itemDtoInput1);

        Throwable thrown = assertThrows(BadRequestException.class,
                () -> itemService.comment(savedAuthor.getId(), commentDto, savedItem.getId()));
        assertEquals("Пользователь с id " + savedAuthor.getId() +
                " не бронировал вещь с id " + savedItem.getId() + ".", thrown.getMessage());
    }

    @Test
    void shouldNotCreateComment_bookingNotOver() {
        UserDto savedOwner = userService.create(userDto1);
        UserDto savedAuthor = userService.create(userDto2);
        ItemDtoRequest savedItem = itemService.create(savedOwner.getId(), itemDtoInput1);
        BookingDtoOutput savedNextBooking = bookingService.create(savedAuthor.getId(), nextBooking);

        bookingService.update(savedOwner.getId(), savedNextBooking.getId(), true);

        Throwable thrown = assertThrows(BadRequestException.class,
                () -> itemService.comment(savedAuthor.getId(), commentDto, savedItem.getId()));
        assertEquals("Ошибка бронирования!", thrown.getMessage());
    }
}
