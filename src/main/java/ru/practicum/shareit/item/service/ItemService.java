package ru.practicum.shareit.item.service;

import org.springframework.data.domain.Pageable;
import ru.practicum.shareit.comment.dto.CommentDto;
import ru.practicum.shareit.item.dto.*;

import java.util.List;

public interface ItemService {

    ItemDtoComment getById(long userId, long itemId);

    List<ItemDtoComment> getAll(long userId, Pageable pageable);

    ItemDtoRequest create(long userId, ItemDtoInput itemDtoInput);

    ItemDtoRequest update(long userId, ItemDtoInput itemDtoInput, long itemId);

    CommentDto comment(long authorId, CommentDto commentDto, long itemId);

    List<ItemDtoOutput> search(String text, Pageable pageable);

}