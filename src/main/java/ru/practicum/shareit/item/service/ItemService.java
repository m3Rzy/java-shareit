package ru.practicum.shareit.item.service;

import ru.practicum.shareit.item.model.Item;

import java.util.List;

public interface ItemService {
    Item getItemById(Long itemId);

    List<Item> getAll(Long userId);

    Item createItem(Item item);

    Item updateItem(Item item);

    List<Item> getItemsByRequest(String text);
}
