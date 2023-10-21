package ru.practicum.shareit.item.service;

import ru.practicum.shareit.comment.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;

import java.util.List;

public interface ItemService {

    ItemDto getItemById(long userId, long itemId);

    List<ItemDto> getAll(long userId);

    ItemDto createItem(long userId, ItemDto itemDto);

    ItemDto updateItem(long userId, ItemDto itemDto, long itemId);

    List<ItemDto> findItemsByRequest(String text);

    CommentDto createComment(long authorId, CommentDto commentDto, long itemId);
}