package ru.practicum.shareit.item.service;

import ru.practicum.shareit.comment.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;

import java.util.List;

public interface ItemService {

    ItemDto getItemById(long userId, long itemId);

    List<ItemDto> getAll(long userId);

    ItemDto create(long userId, ItemDto itemDto);

    ItemDto update(long userId, ItemDto itemDto, long itemId);

    List<ItemDto> getByRequest(String text);

    CommentDto comment(long authorId, CommentDto commentDto, long itemId);
}