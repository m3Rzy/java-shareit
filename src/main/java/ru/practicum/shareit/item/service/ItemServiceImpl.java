package ru.practicum.shareit.item.service;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.dto.BookingDtoForOwner;
import ru.practicum.shareit.booking.mapper.BookingMapper;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.Status;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.comment.dto.CommentDto;
import ru.practicum.shareit.comment.mapper.CommentMapper;
import ru.practicum.shareit.comment.model.Comment;
import ru.practicum.shareit.comment.repository.CommentRepository;
import ru.practicum.shareit.exception.BadRequestException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.dto.*;
import ru.practicum.shareit.item.mapper.ItemMapper;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.request.repository.ItemRequestRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;
import ru.practicum.shareit.user.service.UserService;

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
    private final BookingRepository bookingRepository;
    private final CommentRepository commentRepository;
    private final ItemRequestRepository itemRequestRepository;
    private final UserRepository userRepository;

    private final UserService userService;

    @Override
    @Transactional
    public ItemDtoComment getById(long userId, long itemId) {
        userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Такого пользователя не существует!"));

        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new NotFoundException("Такого предмета не существует."));

        BookingDtoForOwner lastBooking = null;
        BookingDtoForOwner nextBooking = null;
        long ownerId = item.getOwner().getId();

        if (ownerId == userId) {
            lastBooking = BookingMapper.maptoBookingDtoForOwner(findLastBooking(itemId));
            nextBooking = BookingMapper.maptoBookingDtoForOwner(findNextBooking(itemId));
        }
        List<CommentDto> comments = findComments(itemId);
        return ItemMapper.mapToItemDtoWithComments(item, lastBooking, nextBooking, comments);
    }

    @Override
    @Transactional
    public List<ItemDtoComment> getAll(long userId, Pageable pageable) {
        userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Такого пользователя не существует!"));
        return itemRepository.findAllByOwnerId(userId, pageable)
                .getContent()
                .stream()
                .map(item -> ItemMapper.mapToItemDtoWithComments(item,
                        BookingMapper.maptoBookingDtoForOwner(findLastBooking(item.getId())),
                        BookingMapper.maptoBookingDtoForOwner(findNextBooking(item.getId())),
                        findComments(item.getId())))
                .sorted(Comparator.comparingLong(ItemDtoComment::getId))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public ItemDtoRequest create(long userId, ItemDtoInput itemDtoInput) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Такого пользователя не существует!"));

        ItemRequest itemRequest = itemRequestRepository.findById(itemDtoInput.getRequestId())
                .orElse(null);

        Item item = ItemMapper.mapToItem(itemDtoInput, user, itemRequest);

        return ItemMapper.mapToItemDtoWithRequestId(itemRepository.save(item), getRequestId(item));
    }

    @Override
    @Transactional
    public ItemDtoRequest update(long userId, ItemDtoInput itemDtoInput, long itemId) {
        Item updatedItem = itemRepository.findById(itemId)
                .orElseThrow(() -> new NotFoundException("Такого предмета не существует."));

        if (updatedItem.getOwner().getId() != userId) {
            throw new NotFoundException("Нет прав для редактирования вещи.");
        }

        if (itemDtoInput.getName() != null) {
            updatedItem.setName(itemDtoInput.getName());
        }

        if (itemDtoInput.getDescription() != null) {
            updatedItem.setDescription(itemDtoInput.getDescription());
        }

        if (itemDtoInput.getAvailable() != null) {
            updatedItem.setAvailable(itemDtoInput.getAvailable());
        }

        return ItemMapper.mapToItemDtoWithRequestId(itemRepository.save(updatedItem), getRequestId(updatedItem));
    }

    @Override
    @Transactional
    public CommentDto comment(long authorId, CommentDto commentDto, long itemId) {
        User user = userRepository.findById(authorId)
                .orElseThrow(() -> new NotFoundException("Такого пользователя не существует!"));

        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new NotFoundException("Такого предмета не существует."));

        List<Booking> itemBookings = bookingRepository.findAllByItem_Id(itemId)
                .stream()
                .filter(booking -> booking.getBooker().getId() == authorId)
                .filter(booking -> booking.getStatus() == Status.APPROVED)
                .collect(Collectors.toList());

        if (itemBookings.isEmpty()) {
            throw new BadRequestException("Пользователь с id " + authorId + " не бронировал вещь с id " + itemId + ".");
        }

        List<Booking> pastOrPresentBookings = itemBookings.stream()
                .filter(booking -> booking.getStart().isBefore(LocalDateTime.now()))
                .collect(Collectors.toList());

        if (pastOrPresentBookings.isEmpty()) {
            throw new BadRequestException("Ошибка бронирования!");
        }

        commentDto.setCreated(LocalDateTime.now());

        Comment comment = CommentMapper.mapToComment(commentDto, item, user);

        return CommentMapper.mapToCommentDto(commentRepository.save(comment));
    }

    @Override
    @Transactional
    public List<ItemDtoOutput> search(String text, Pageable pageable) {
        if (text.isBlank()) {
            return new ArrayList<>();
        }
        return itemRepository.search(text, pageable)
                .getContent()
                .stream()
                .filter(Item::getAvailable)
                .map(item -> ItemMapper.mapToItemDtoOutput(item, null, null))
                .collect(Collectors.toList());
    }

    private long getRequestId(Item item) {
        if (item.getItemRequest() == null) {
            return 0;
        } else {
            return item.getItemRequest().getId();
        }
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

    private List<CommentDto> findComments(long itemId) {
        return commentRepository.findCommentsByItemId(itemId)
                .stream()
                .map(CommentMapper::mapToCommentDto)
                .collect(Collectors.toList());
    }
}