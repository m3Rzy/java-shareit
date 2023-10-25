package ru.practicum.shareit.request.service;

import org.springframework.data.domain.Pageable;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.dto.ItemRequestDtoOutput;

import java.util.List;

public interface ItemRequestService {

    ItemRequestDtoOutput getById(long userId, long id);

    ItemRequestDtoOutput create(long userId, ItemRequestDto itemRequestDtoInput);

    List<ItemRequestDtoOutput> getAllByRequestor(long userId);

    List<ItemRequestDtoOutput> getAllUsersRequests(long userId, Pageable pageable);
}
