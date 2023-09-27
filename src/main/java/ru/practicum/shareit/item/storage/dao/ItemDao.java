package ru.practicum.shareit.item.storage.dao;

import ru.practicum.shareit.item.model.Item;

import java.util.List;
import java.util.Optional;

public interface ItemDao {

    Optional<Item> add(Item item);

    Optional<Item> update(Item item);

    Optional<Item> getById(Long itemId);

    List<Item> getAll(Long userId);

    List<Item> getByRequest(String text);
}
