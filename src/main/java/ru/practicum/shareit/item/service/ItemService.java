package ru.practicum.shareit.item.service;

import ru.practicum.shareit.item.model.Item;

import java.util.List;
import java.util.Optional;

public interface ItemService {
    Optional<Item> findItemById(Long itemId);

    List<Item> findAllItems(Long userId);

    Optional<Item> createItem(Item item);

    Optional<Item> updateItem(Item item);

    List<Item> findItemsByRequest(String text);
}
