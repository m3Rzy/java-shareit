package ru.practicum.shareit.unit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import ru.practicum.shareit.booking.dto.BookingDtoInput;
import ru.practicum.shareit.booking.mapper.BookingMapper;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.comment.dto.CommentDto;
import ru.practicum.shareit.comment.mapper.CommentMapper;
import ru.practicum.shareit.comment.model.Comment;
import ru.practicum.shareit.comment.repository.CommentRepository;
import ru.practicum.shareit.exception.BadRequestException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.dto.ItemDtoComment;
import ru.practicum.shareit.item.dto.ItemDtoInput;
import ru.practicum.shareit.item.dto.ItemDtoOutput;
import ru.practicum.shareit.item.dto.ItemDtoRequest;
import ru.practicum.shareit.item.mapper.ItemMapper;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.item.service.ItemServiceImpl;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.mapper.ItemRequestMapper;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.request.repository.ItemRequestRepository;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.mapper.UserMapper;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static ru.practicum.shareit.booking.model.Status.APPROVED;

@ExtendWith(MockitoExtension.class)
public class ItemMockTest {
    @Mock
    private ItemRepository itemRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private BookingRepository bookingRepository;
    @Mock
    private ItemRequestRepository itemRequestRepository;
    @Mock
    private CommentRepository commentRepository;
    @InjectMocks
    private ItemServiceImpl itemService;
    User user1;
    User user2;
    ItemDtoInput itemDtoInput1;
    ItemDtoInput itemDtoInput2;
    ItemDtoInput updatedItemDtoInput;
    Item item1;
    Item item2;
    Page<Item> items;
    ItemRequest itemRequest;
    ItemRequestDto itemRequestDtoInput;
    BookingDtoInput lastBooking;
    BookingDtoInput nextBooking;
    List<Booking> bookings;
    Pageable pageable;
    CommentDto commentDto;
    Comment comment;

    @BeforeEach
    void setUp() {
        user1 = UserMapper.mapToUser(new UserDto(1L, "User1", "user1@mail.ru"));

        user2 = UserMapper.mapToUser(new UserDto(2L, "User2", "user2@mail.ru"));

        itemDtoInput1 = new ItemDtoInput(1L, "Item1",
                "Item1 Description", true, 0);
        item1 = ItemMapper.mapToItem(itemDtoInput1, user1, null);

        itemRequestDtoInput = new ItemRequestDto(1L, "I need a Item2");
        itemRequest = ItemRequestMapper.mapToItemRequest(itemRequestDtoInput, user2, LocalDateTime.now());

        itemDtoInput2 = new ItemDtoInput(2L, "Item2", "Item2 Description", true, 1);
        item2 = ItemMapper.mapToItem(itemDtoInput2, user1, itemRequest);

        items = new PageImpl<>(Arrays.asList(item1, item2));

        lastBooking = new BookingDtoInput(1L,
                LocalDateTime.of(2023, 8, 1, 10, 0, 0),
                LocalDateTime.of(2023, 8, 8, 10, 0, 0),
                1);

        nextBooking = new BookingDtoInput(2L, LocalDateTime.now().plusHours(1),
                LocalDateTime.now().plusHours(2), 1);

        bookings = Arrays.asList(BookingMapper.mapToBooking(lastBooking, item1, user2, APPROVED),
                BookingMapper.mapToBooking(nextBooking, item1, user2, APPROVED));

        pageable = PageRequest.of(0, 10);

        commentDto = new CommentDto(1L, "Good item", "User2",
                LocalDateTime.now());
        comment = CommentMapper.mapToComment(commentDto, item1, user2);
    }

    @Test
    void create_whenAllIsOkWithoutRequestId_thenReturnedItem() {
        Mockito.when(userRepository.findById(1L)).thenReturn(Optional.of(user1));
        Mockito.when(itemRepository.save(Mockito.any())).thenReturn(item1);

        ItemDtoRequest createdItem = itemService.create(1, itemDtoInput1);

        Mockito.verify(itemRepository).save(Mockito.any());

        assertEquals(itemDtoInput1.getName(), createdItem.getName());
        assertEquals(itemDtoInput1.getDescription(), createdItem.getDescription());
        assertEquals(itemDtoInput1.getAvailable(), createdItem.getAvailable());
        assertEquals(itemDtoInput1.getRequestId(), createdItem.getRequestId());
    }

    @Test
    void create_whenAllIsOkWithRequestId_thenReturnedItem() {
        Mockito.when(userRepository.findById(1L)).thenReturn(Optional.of(user1));
        Mockito.when(itemRequestRepository.findById(1L)).thenReturn(Optional.of(itemRequest));
        Mockito.when(itemRepository.save(Mockito.any())).thenReturn(item2);

        ItemDtoRequest createdItem = itemService.create(1L, itemDtoInput2);

        Mockito.verify(itemRepository).save(Mockito.any());

        assertEquals(itemDtoInput2.getName(), createdItem.getName());
        assertEquals(itemDtoInput2.getDescription(), createdItem.getDescription());
        assertEquals(itemDtoInput2.getAvailable(), createdItem.getAvailable());
        assertEquals(itemDtoInput2.getRequestId(), createdItem.getRequestId());
    }

    @Test
    void create_whenOwnerNotFound_thenNotFoundExceptionThrown() {
        Mockito.when(userRepository.findById(999L)).thenReturn(Optional.empty());

        NotFoundException notFoundException = assertThrows(NotFoundException.class,
                () -> itemService.create(999, itemDtoInput1));

        assertEquals("Пользователя с id 999 не существует!", notFoundException.getMessage());
    }

    @Test
    void read_whenAllIsOkAndUserIsNotItemOwner_thenReturnedItemWithNullNextAndLastBookings() {
        Mockito.when(userRepository.findById(2L)).thenReturn(Optional.of(user2));
        Mockito.when(commentRepository.findCommentsByItemId(1L)).thenReturn(new ArrayList<>());
        Mockito.when(itemRepository.findById(1L)).thenReturn(Optional.of(item1));

        ItemDtoComment returnedItem = itemService.getById(2, 1);

        Mockito.verify(itemRepository).findById(1L);

        assertEquals(itemDtoInput1.getName(), returnedItem.getName());
        assertEquals(itemDtoInput1.getDescription(), returnedItem.getDescription());
        assertEquals(itemDtoInput1.getAvailable(), returnedItem.getAvailable());
        assertNull(returnedItem.getLastBooking());
        assertNull(returnedItem.getNextBooking());
        assertEquals(0, returnedItem.getComments().size());
    }

    @Test
    void read_whenUserNotFound_thenNotFoundExceptionThrown() {
        Mockito.when(userRepository.findById(999L)).thenReturn(Optional.empty());

        NotFoundException notFoundException = assertThrows(NotFoundException.class,
                () -> itemService.getById(999, 1));

        assertEquals("Пользователя с id 999 не существует!", notFoundException.getMessage());
    }

    @Test
    void read_whenItemNotFound_thenNotFoundExceptionThrown() {
        Mockito.when(userRepository.findById(2L)).thenReturn(Optional.of(user2));
        Mockito.when(itemRepository.findById(999L)).thenReturn(Optional.empty());

        NotFoundException notFoundException = assertThrows(NotFoundException.class,
                () -> itemService.getById(2, 999));

        assertEquals("Предмета с id 999 не существует!", notFoundException.getMessage());
    }

    @Test
    void read_whenUserIsItemOwner_thenReturnedItemWithLastAndNextBookings() {
        Mockito.when(userRepository.findById(1L)).thenReturn(Optional.of(user1));
        Mockito.when(bookingRepository.findAllByItem_Id(1L)).thenReturn(bookings);
        Mockito.when(commentRepository.findCommentsByItemId(1L)).thenReturn(new ArrayList<>());
        Mockito.when(itemRepository.findById(1L)).thenReturn(Optional.of(item1));

        ItemDtoComment returnedItem = itemService.getById(1, 1);

        Mockito.verify(itemRepository).findById(1L);

        assertEquals(itemDtoInput1.getName(), returnedItem.getName());
        assertEquals(itemDtoInput1.getDescription(), returnedItem.getDescription());
        assertEquals(itemDtoInput1.getAvailable(), returnedItem.getAvailable());
        assertEquals(1, returnedItem.getLastBooking().getId());
        assertEquals(2, returnedItem.getLastBooking().getBookerId());
        assertEquals(2, returnedItem.getNextBooking().getId());
        assertEquals(2, returnedItem.getNextBooking().getBookerId());
        assertEquals(0, returnedItem.getComments().size());
    }

    @Test
    void readAll_whenAllIsOk_thenReturnedItemCollection() {
        Mockito.when(userRepository.findById(1L)).thenReturn(Optional.of(user1));
        Mockito.when(bookingRepository.findAllByItem_Id(1L)).thenReturn(bookings);
        Mockito.when(commentRepository.findCommentsByItemId(1L)).thenReturn(new ArrayList<>());
        Mockito.when(itemRepository.findAllByOwnerId(1L, pageable)).thenReturn(items);

        List<ItemDtoComment> returnedItems = new ArrayList<>(itemService.getAll(1, pageable));

        Mockito.verify(itemRepository).findAllByOwnerId(1L, pageable);

        assertEquals(items.getContent().get(0).getId(), returnedItems.get(0).getId());
        assertEquals(items.getContent().get(0).getName(), returnedItems.get(0).getName());
        assertEquals(items.getContent().get(0).getDescription(), returnedItems.get(0).getDescription());
        assertEquals(items.getContent().get(0).getAvailable(), returnedItems.get(0).getAvailable());
        assertEquals(1, returnedItems.get(0).getLastBooking().getId());
        assertEquals(2, returnedItems.get(0).getLastBooking().getBookerId());
        assertEquals(2, returnedItems.get(0).getNextBooking().getId());
        assertEquals(2, returnedItems.get(0).getNextBooking().getBookerId());

        assertEquals(items.getContent().get(1).getId(), returnedItems.get(1).getId());
        assertEquals(items.getContent().get(1).getName(), returnedItems.get(1).getName());
        assertEquals(items.getContent().get(1).getDescription(), returnedItems.get(1).getDescription());
        assertEquals(items.getContent().get(1).getAvailable(), returnedItems.get(1).getAvailable());
        assertNull(returnedItems.get(1).getLastBooking());
        assertNull(returnedItems.get(1).getNextBooking());
    }

    @Test
    void readAll_whenOwnerNotFound_thenNotFoundExceptionThrown() {
        Mockito.when(userRepository.findById(999L)).thenReturn(Optional.empty());

        NotFoundException notFoundException = assertThrows(NotFoundException.class,
                () -> itemService.getAll(999, pageable));

        assertEquals("Пользователя с id 999 не существует!", notFoundException.getMessage());
    }

    @Test
    void update_whenAllIsOk_thenUpdatedItem() {
        updatedItemDtoInput = new ItemDtoInput(1L, "Item1 Updated",
                "Item1 Description Updated", false, 0);

        Mockito.when(itemRepository.findById(1L)).thenReturn(Optional.of(item1));
        Mockito.when(itemRepository.save(Mockito.any())).thenReturn(ItemMapper
                .mapToItem(updatedItemDtoInput, user1, null));

        ItemDtoRequest updatedItem = itemService.update(1, updatedItemDtoInput, 1);

        Mockito.verify(itemRepository).save(Mockito.any());

        assertEquals(updatedItemDtoInput.getName(), updatedItem.getName());
        assertEquals(updatedItemDtoInput.getDescription(), updatedItem.getDescription());
        assertEquals(updatedItemDtoInput.getAvailable(), updatedItem.getAvailable());
    }

    @Test
    void update_whenItemNotFound_thenNotFoundExceptionThrown() {
        Mockito.when(itemRepository.findById(999L)).thenReturn(Optional.empty());

        NotFoundException notFoundException = assertThrows(NotFoundException.class,
                () -> itemService.update(1, updatedItemDtoInput, 999));

        assertEquals("Пользователя с id 1 не существует!", notFoundException.getMessage());
    }

    @Test
    void update_whenItemNameIsNull_thenUpdateOnlyDescriptionAndAvailable() {
        ItemDtoInput itemDtoInputWithoutName = new ItemDtoInput();

        itemDtoInputWithoutName.setDescription("Item1 Description Updated");
        itemDtoInputWithoutName.setAvailable(false);

        ItemDtoInput updatedItemDto = new ItemDtoInput(1, "Item1",
                "Item1 Description Updated", false, 0);

        Mockito.when(itemRepository.findById(1L)).thenReturn(Optional.of(item1));
        Mockito.when(itemRepository.save(Mockito.any()))
                .thenReturn(ItemMapper.mapToItem(updatedItemDto, user1, null));

        ItemDtoRequest updatedItem = itemService.update(1, itemDtoInputWithoutName, 1);

        Mockito.verify(itemRepository).save(Mockito.any());

        assertEquals(item1.getName(), updatedItem.getName());
        assertEquals(itemDtoInputWithoutName.getDescription(), updatedItem.getDescription());
        assertEquals(itemDtoInputWithoutName.getAvailable(), updatedItem.getAvailable());
    }

    @Test
    void update_whenItemDescriptionIsNull_thenUpdateOnlyNameAndAvailable() {
        ItemDtoInput itemDtoInputWithoutDescription = new ItemDtoInput();

        itemDtoInputWithoutDescription.setName("Item1 Updated");
        itemDtoInputWithoutDescription.setAvailable(false);

        ItemDtoInput updatedItemDto = new ItemDtoInput(1, "Item1 Updated",
                "Item1 Description", false, 0);

        Mockito.when(itemRepository.findById(1L)).thenReturn(Optional.of(item1));
        Mockito.when(itemRepository.save(Mockito.any())).thenReturn(ItemMapper
                .mapToItem(updatedItemDto, user1, null));

        ItemDtoRequest updatedItem = itemService.update(1, itemDtoInputWithoutDescription, 1);

        Mockito.verify(itemRepository).save(Mockito.any());

        assertEquals(itemDtoInputWithoutDescription.getName(), updatedItem.getName());
        assertEquals(item1.getDescription(), updatedItem.getDescription());
        assertEquals(itemDtoInputWithoutDescription.getAvailable(), updatedItem.getAvailable());
    }

    @Test
    void update_whenItemAvailableIsNull_thenUpdateOnlyNameAndDescription() {
        ItemDtoInput itemDtoInputWithoutAvailable = new ItemDtoInput();

        itemDtoInputWithoutAvailable.setName("Item1 Updated");
        itemDtoInputWithoutAvailable.setDescription("Item1 Description Updated");

        ItemDtoInput updatedItemDto = new ItemDtoInput(1, "Item1 Updated",
                "Item1 Description Updated", true, 0);

        Mockito.when(itemRepository.findById(1L)).thenReturn(Optional.of(item1));
        Mockito.when(itemRepository.save(Mockito.any())).thenReturn(ItemMapper
                .mapToItem(updatedItemDto, user1, null));

        ItemDtoRequest updatedItem = itemService.update(1, itemDtoInputWithoutAvailable, 1);

        Mockito.verify(itemRepository).save(Mockito.any());

        assertEquals(itemDtoInputWithoutAvailable.getName(), updatedItem.getName());
        assertEquals(itemDtoInputWithoutAvailable.getDescription(), updatedItem.getDescription());
        assertEquals(item1.getAvailable(), updatedItem.getAvailable());
    }

    @Test
    void update_whenItemNameAndDescriptionAndAvailableAreNull_thenNothingToUpdate() {
        ItemDtoInput itemDtoInputNothingToUpdate = new ItemDtoInput();

        Mockito.when(itemRepository.findById(1L)).thenReturn(Optional.of(item1));
        Mockito.when(itemRepository.save(Mockito.any())).thenReturn(item1);

        ItemDtoRequest updatedItem = itemService.update(1, itemDtoInputNothingToUpdate, 1);

        Mockito.verify(itemRepository).save(Mockito.any());

        assertEquals(item1.getName(), updatedItem.getName());
        assertEquals(item1.getDescription(), updatedItem.getDescription());
        assertEquals(item1.getAvailable(), updatedItem.getAvailable());
    }

    @Test
    void search_whenTextIsNotEmpty_thenReturnedSuitableItem() {
        Mockito.when(itemRepository.search("description", pageable)).thenReturn(items);

        List<ItemDtoOutput> returnedItems = new ArrayList<>(itemService.search("description", pageable));

        Mockito.verify(itemRepository).search("description", pageable);

        assertEquals(items.getContent().get(0).getId(), returnedItems.get(0).getId());
        assertEquals(items.getContent().get(0).getName(), returnedItems.get(0).getName());
        assertEquals(items.getContent().get(0).getDescription(), returnedItems.get(0).getDescription());
        assertEquals(items.getContent().get(0).getAvailable(), returnedItems.get(0).getAvailable());

        assertEquals(items.getContent().get(1).getId(), returnedItems.get(1).getId());
        assertEquals(items.getContent().get(1).getName(), returnedItems.get(1).getName());
        assertEquals(items.getContent().get(1).getDescription(), returnedItems.get(1).getDescription());
        assertEquals(items.getContent().get(1).getAvailable(), returnedItems.get(1).getAvailable());
    }

    @Test
    void search_whenTextIsEmpty_thenReturnedEmptyCollection() {
        List<ItemDtoOutput> returnedItems = new ArrayList<>(itemService.search(" ", pageable));

        assertEquals(0, returnedItems.size());
    }

    @Test
    void createComment_whenAllIsOk_thenReturnedComment() {
        Mockito.when(userRepository.findById(2L)).thenReturn(Optional.of(user2));
        Mockito.when(itemRepository.findById(1L)).thenReturn(Optional.of(item1));
        Mockito.when(bookingRepository.findAllByItem_Id(1L)).thenReturn(bookings);
        Mockito.when(commentRepository.save(Mockito.any())).thenReturn(comment);

        CommentDto createdComment = itemService.comment(2, commentDto, 1);

        assertEquals(commentDto.getText(), createdComment.getText());
        assertEquals(commentDto.getAuthorName(), createdComment.getAuthorName());

        LocalDateTime expectedDate = commentDto.getCreated();
        LocalDateTime actualDate = createdComment.getCreated();

        assertTrue(ChronoUnit.MILLIS.between(expectedDate, actualDate) < 1000);
    }

    @Test
    void createComment_whenAuthorNotFound_thenNotFoundExceptionThrown() {
        Mockito.when(userRepository.findById(999L)).thenReturn(Optional.empty());

        NotFoundException notFoundException = assertThrows(NotFoundException.class,
                () -> itemService.comment(999, commentDto, 1));

        assertEquals("Пользователя с id 999 не существует!", notFoundException.getMessage());
    }

    @Test
    void createComment_whenItemNotFound_thenNotFoundExceptionThrown() {
        Mockito.when(userRepository.findById(2L)).thenReturn(Optional.of(user2));
        Mockito.when(itemRepository.findById(999L)).thenReturn(Optional.empty());

        NotFoundException notFoundException = assertThrows(NotFoundException.class,
                () -> itemService.comment(2, commentDto, 999));

        assertEquals("Предмета с id 999 не существует!", notFoundException.getMessage());
    }

    @Test
    void createComment_whenAuthorDidNotBookItem_thenItemAvailabilityExceptionThrown() {
        Mockito.when(userRepository.findById(2L)).thenReturn(Optional.of(user2));
        Mockito.when(itemRepository.findById(1L)).thenReturn(Optional.of(item1));
        Mockito.when(bookingRepository.findAllByItem_Id(1L)).thenReturn(new ArrayList<>());

        BadRequestException itemAvailabilityException = assertThrows(BadRequestException.class,
                () -> itemService.comment(2, commentDto, 1));

        assertEquals("Пользователь с id " + 2 + " не бронировал вещь с id " + 1 + ".",
                itemAvailabilityException.getMessage());
    }

    @Test
    void createComment_whenBookingNotOver_thenItemAvailabilityExceptionThrown() {
        Mockito.when(userRepository.findById(2L)).thenReturn(Optional.of(user2));
        Mockito.when(itemRepository.findById(1L)).thenReturn(Optional.of(item1));
        Mockito.when(bookingRepository.findAllByItem_Id(1L)).thenReturn(Collections.singletonList(BookingMapper
                .mapToBooking(nextBooking, item1, user2, APPROVED)));

        BadRequestException itemAvailabilityException = assertThrows(BadRequestException.class,
                () -> itemService.comment(2, commentDto, 1));

        assertEquals("Ошибка бронирования!",
                itemAvailabilityException.getMessage());
    }
}
