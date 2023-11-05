package ru.practicum.shareit.request.service;

import lombok.AllArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.dto.ItemDtoRequest;
import ru.practicum.shareit.item.mapper.ItemMapper;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.dto.ItemRequestDtoOutput;
import ru.practicum.shareit.request.mapper.ItemRequestMapper;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.request.repository.ItemRequestRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;
import ru.practicum.shareit.user.service.UserService;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toList;

@Service
@AllArgsConstructor
public class ItemRequestServiceImpl implements ItemRequestService {

    private final ItemRequestRepository itemRequestRepository;
    private final ItemRepository itemRepository;
    private final UserRepository userRepository;

    private final UserService userService;

    @Override
    @Transactional
    public ItemRequestDtoOutput create(long userId, ItemRequestDto itemRequestDtoInput) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователя с id " + userId + " не существует!"));

        ItemRequest itemRequest = ItemRequestMapper
                .mapToItemRequest(itemRequestDtoInput, user, LocalDateTime.now());

        List<ItemDtoRequest> items = itemRepository.findAllByItemRequestId(itemRequest.getId())
                .stream()
                .map(item -> ItemMapper.mapToItemDtoWithRequestId(item, itemRequest.getId()))
                .collect(toList());

        return ItemRequestMapper.mapToItemRequestDtoOutput(itemRequestRepository.save(itemRequest), items);
    }

    @Override
    @Transactional
    public List<ItemRequestDtoOutput> getAllByRequestor(long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователя с id " + userId + " не существует!"));

        List<ItemRequest> requests = itemRequestRepository.findAllByRequestorIdOrderByCreatedDesc(userId);

        Map<Long, List<ItemDtoRequest>> items = findItemsByRequest(requests);

        return requests
                .stream()
                .map(itemRequest -> ItemRequestMapper.mapToItemRequestDtoOutput(itemRequest,
                        items.get(itemRequest.getId())))
                .collect(toList());
    }

    @Override
    @Transactional
    public List<ItemRequestDtoOutput> getAllUsersRequests(long userId, Pageable pageable) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователя с id " + userId + " не существует!"));

        List<ItemRequest> requests = itemRequestRepository
                .findAllByRequestorIdNotOrderByCreatedDesc(userId, pageable)
                .getContent();

        Map<Long, List<ItemDtoRequest>> items = findItemsByRequest(requests);

        return requests
                .stream()
                .map(itemRequest -> ItemRequestMapper.mapToItemRequestDtoOutput(itemRequest,
                        items.get(itemRequest.getId())))
                .collect(toList());
    }

    @Override
    @Transactional
    public ItemRequestDtoOutput getById(long userId, long id) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователя с id " + userId + " не существует!"));

        ItemRequest itemRequest = itemRequestRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Запроса с id " + id + " не существует!"));

        List<ItemDtoRequest> items = itemRepository.findAllByItemRequestId(itemRequest.getId())
                .stream()
                .map(item -> ItemMapper.mapToItemDtoWithRequestId(item, itemRequest.getId()))
                .collect(toList());

        return ItemRequestMapper
                .mapToItemRequestDtoOutput(itemRequestRepository.findByIdOrderByCreatedDesc(id), items);
    }

    private Map<Long, List<ItemDtoRequest>> findItemsByRequest(List<ItemRequest> requests) {
        return itemRepository.findAllByItemRequestIdIn(requests
                        .stream()
                        .map(ItemRequest::getId)
                        .collect(toList()))
                .stream()
                .map(item -> ItemMapper.mapToItemDtoWithRequestId(item,
                        item.getItemRequest().getId()))
                .collect(groupingBy(ItemDtoRequest::getRequestId, toList()));
    }
}
