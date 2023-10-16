package ru.practicum.shareit.item.storage.impl;

import org.springframework.stereotype.Repository;
import ru.practicum.shareit.exception.DeniedAccessException;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.storage.dao.ItemDao;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

@Repository
public class ItemDaoImpl implements ItemDao {

    private final HashMap<Long, Item> items = new HashMap<>();
    private long id = 1;

    @Override
    public Optional<Item> findById(Long itemId) {
        return Optional.ofNullable(items.get(itemId));
    }

    @Override
    public List<Item> findAll(Long userId) {
        List<Item> result = new ArrayList<>();
        for (Item item : items.values()) {
            if (item.getOwner().equals(userId)) result.add(item);
        }
        return result;
    }

    @Override
    public Item add(Item item) {
        item.setId(generateId());
        items.put(item.getId(), item);
        return item;
    }

    @Override
    public Item update(Item item) {
        Item updatedItem = items.get(item.getId());
        if (!updatedItem.getOwner().equals(item.getOwner())) {
            throw new DeniedAccessException("Пользователь не владеет этой вещью. " +
                    "userId: " + item.getOwner() + ", itemId: " + item.getId());
        }
        checkItem(updatedItem, item);
        return updatedItem;
    }

    @Override
    public List<Item> findByRequest(String text) {
        List<Item> result = new ArrayList<>();
        String wantedItem = text.toLowerCase();

        for (Item item : items.values()) {
            String itemName = item.getName().toLowerCase();
            String itemDescription = item.getDescription().toLowerCase();

            if ((itemName.contains(wantedItem) || itemDescription.contains(wantedItem))
                    && item.getAvailable().equals(true)) {
                result.add(item);
            }
        }
        return result;
    }

    private long generateId() {
        return id++;
    }

    private void checkItem(Item oldItem, Item newItem) {
        String name = newItem.getName();
        if (name != null) {
            oldItem.setName(name);
        }

        String description = newItem.getDescription();
        if (description != null) {
            oldItem.setDescription(description);
        }

        Boolean available = newItem.getAvailable();
        if (available != null) {
            oldItem.setAvailable(available);
        }
    }
}
