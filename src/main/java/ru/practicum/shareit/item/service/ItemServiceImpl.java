package ru.practicum.shareit.item.service;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.dto.BookingDtoForOwner;
import ru.practicum.shareit.booking.mapper.BookingMapper;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.Status;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.exception.BadRequestException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.comment.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.comment.mapper.CommentMapper;
import ru.practicum.shareit.item.mapper.ItemMapper;
import ru.practicum.shareit.comment.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.comment.repository.CommentRepository;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
@Slf4j
public class ItemServiceImpl implements ItemService {
    private final ItemRepository itemRepository;
    private final UserRepository userRepository;
    private final BookingRepository bookingRepository;
    private final CommentRepository commentRepository;

    @Override
    @Transactional
    public ItemDto getItemById(long userId, long itemId) {
        userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователя с id " + userId + " не существует!"));

        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new NotFoundException("Вещи с id " + itemId + " не существует!"));

        BookingDtoForOwner lastBooking = null;
        BookingDtoForOwner nextBooking = null;
        long ownerId = item.getOwner().getId();

        if (ownerId == userId) {
            lastBooking = BookingMapper.maptoBookingDtoForOwner(findLastBooking(itemId));
            nextBooking = BookingMapper.maptoBookingDtoForOwner(findNextBooking(itemId));
        }

        List<CommentDto> comments = findComments(itemId);
        log.info("Предмет {} был успешно найден с помощью id.", itemId);
        return ItemMapper.mapToItemDto(item, lastBooking, nextBooking, comments);
    }

    @Override
    @Transactional
    public List<ItemDto> getAll(long userId) {
        userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователя с id " + userId + " не существует!"));
        return itemRepository.findAllByOwnerId(userId).stream()
                .map(item -> ItemMapper.mapToItemDto(item,
                        BookingMapper.maptoBookingDtoForOwner(findLastBooking(item.getId())),
                        BookingMapper.maptoBookingDtoForOwner(findNextBooking(item.getId())),
                        findComments(item.getId())))
                .sorted(Comparator.comparingLong(ItemDto::getId))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public ItemDto createItem(long userId, ItemDto itemDto) {
        User owner = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь с id " + userId + " не найден"));
        Item item = ItemMapper.mapToItem(itemDto, owner);
        return ItemMapper.mapToItemDto(itemRepository.save(item), null, null, null);
    }

    @Override
    @Transactional
    public ItemDto updateItem(long userId, ItemDto itemDto, long itemId) {
        Item updatedItem = itemRepository.findById(itemId)
                .orElseThrow(() -> new NotFoundException("Вещь с id " + itemId + " не найдена"));

        if (updatedItem.getOwner().getId() != userId) {
            throw new NotFoundException("Нет прав для редактирования вещи");
        }

        if (itemDto.getName() != null) {
            updatedItem.setName(itemDto.getName());
        }

        if (itemDto.getDescription() != null) {
            updatedItem.setDescription(itemDto.getDescription());
        }

        if (itemDto.getAvailable() != null) {
            updatedItem.setAvailable(itemDto.getAvailable());
        }

        return ItemMapper.mapToItemDto(itemRepository.save(updatedItem),
                BookingMapper.maptoBookingDtoForOwner(findLastBooking(itemId)),
                BookingMapper.maptoBookingDtoForOwner(findNextBooking(itemId)),
                findComments(itemId));
    }

    @Override
    @Transactional
    public List<ItemDto> findItemsByRequest(String text) {
        if (text.isBlank()) {
            return new ArrayList<>();
        }
        return itemRepository.search(text).stream()
                .filter(Item::getAvailable)
                .map(item -> ItemMapper.mapToItemDto(item, null, null,
                        findComments(item.getId())))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public CommentDto createComment(long authorId, CommentDto commentDto, long itemId) {
        User author = userRepository.findById(authorId)
                .orElseThrow(() -> new NotFoundException("Пользователя с id " + authorId + " не существует!"));

        List<Booking> bookings = bookingRepository.findAllByItem_Id(itemId)
                .stream()
                .filter(booking -> booking.getBooker().getId() == authorId)
                .filter(booking -> booking.getStatus() == Status.APPROVED)
                .collect(Collectors.toList());

        if (bookings.isEmpty()) {
            throw new BadRequestException("Пользователь с id " + authorId + " не бронировал вещь с id " + itemId + ".");
        }

        List<Booking> pastOrPresentBookings = bookings.stream()
                .filter(booking -> booking.getStart().isBefore(LocalDateTime.now()))
                .collect(Collectors.toList());

        if (pastOrPresentBookings.isEmpty()) {
            throw new BadRequestException("Отзыв нельзя оставить без бронирования.");
        }

        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new NotFoundException("Вещи с id " + itemId + " не существует!"));
        commentDto.setCreated(LocalDateTime.now());
        Comment comment = CommentMapper.mapToComment(commentDto, item, author);

        return CommentMapper.mapToCommentDto(commentRepository.save(comment));
    }

    private List<CommentDto> findComments(long itemId) {
        return commentRepository.findCommentsByItemId(itemId)
                .stream()
                .map(CommentMapper::mapToCommentDto)
                .collect(Collectors.toList());
    }

    private Booking findLastBooking(long itemId) {
        List<Booking> itemBookings = bookingRepository.findAllByItem_Id(itemId);
        return itemBookings.stream()
                .filter(booking -> booking.getStart().isBefore(LocalDateTime.now()))
                .filter(booking -> booking.getStatus() == Status.APPROVED)
                .max(Comparator.comparing(Booking::getEnd))
                .orElse(null);
    }

    private Booking findNextBooking(long itemId) {
        List<Booking> itemBookings = bookingRepository.findAllByItem_Id(itemId);
        return itemBookings.stream()
                .filter(booking -> booking.getStart().isAfter(LocalDateTime.now()))
                .filter(booking -> booking.getStatus() == Status.APPROVED)
                .min(Comparator.comparing(Booking::getStart))
                .orElse(null);
    }
}