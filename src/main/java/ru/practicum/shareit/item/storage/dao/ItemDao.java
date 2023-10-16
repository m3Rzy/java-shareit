package ru.practicum.shareit.item.storage.dao;

import ru.practicum.shareit.item.model.Item;

import java.util.List;
import java.util.Optional;

public interface ItemDao {

    Item add(Item item);

    Item update(Item item);

    Optional<Item> findById(Long itemId);

    List<Item> findAll(Long userId);

    List<Item> findByRequest(String text);
}
