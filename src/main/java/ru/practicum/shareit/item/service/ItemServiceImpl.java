package ru.practicum.shareit.item.service;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.item.exception.ItemNotFoundException;
import ru.practicum.shareit.item.exception.ItemValidationException;
import ru.practicum.shareit.item.exception.OwnerNotFoundException;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.storage.dao.ItemDao;
import ru.practicum.shareit.user.exception.UserNotFoundException;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.storage.dao.UserDao;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
@Slf4j
public class ItemServiceImpl implements ItemService {

    private final ItemDao itemDao;
    private final UserDao userDao;

    @Override
    public Optional<Item> findItemById(Long itemId) {
        if (isExist(itemId)) {
            log.info("Предмет {} был успешно найден с помощью id.", itemId);
            return itemDao.getById(itemId);
        } else {
            throw new ItemNotFoundException("Предмета с id " + itemId + " не существует!");
        }
    }

    @Override
    public List<Item> findAllItems(Long userId) {
        if (isExistUser(userId)) {
            log.info("Пользователь {} был успешно найден.", userId);
            if (itemDao.getAll(userId).isEmpty()) {
                log.info("Список предметов пользователя " + userId + " пуст.");
                return itemDao.getAll(userId);
            } else {
                log.info("Количество предметов пользователя " + userId + ": "
                        + itemDao.getAll(userId).size());
                return itemDao.getAll(userId);
            }
        } else {
            throw new UserNotFoundException("Пользователя с id " + userId + " не существует.");
        }
    }

    @Override
    public Optional<Item> createItem(Item item) {
        if (isItemValidationAccess(item)) {
            boolean ownerExists = isOwnerExists(item.getOwner());
            if (!ownerExists) {
                throw new OwnerNotFoundException("Владельца вещи не существует.");
            }
            itemDao.add(item);
            log.info("Предмет {} был успешно добавлен!", item);
            return Optional.of(item);
        } else {
            throw new ItemValidationException("Атрибуты предмета не прошли валидацию.");
        }
    }

    @Override
    public Optional<Item> updateItem(Item item) {
        itemDao.update(item);
        log.info("Предмет {} был успешно изменён.", item);
        return itemDao.getById(item.getId());
    }

    @Override
    public List<Item> findItemsByRequest(String text) {
        if (text == null || text.isBlank() || text.length() <= 3) {
            return new ArrayList<>();
        }
        return itemDao.getByRequest(text);
    }

    private boolean isExist(long itemId) {
        for (Item item : itemDao.getAll(itemId)) {
            if (itemId == item.getId()) {
                return true;
            }
        }
        return false;
    }

    private boolean isOwnerExists(long ownerId) {
        List<User> users = userDao.getAll();
        List<User> result = users
                .stream()
                .filter(user -> user.getId() == ownerId)
                .collect(Collectors.toList());
        return result.size() > 0;
    }

    private boolean isExistUser(long userId) {
        for (User user : userDao.getAll()) {
            if (userId == user.getId()) {
                return true;
            }
        }
        return false;
    }

    private boolean isItemValidationAccess(Item item) {
        if (item.getName() == null || item.getName().isBlank()) {
            return false;
        }
        if (item.getAvailable() == null) {
            return false;
        }
        if (item.getDescription() == null) {
            return false;
        }
        return true;
    }
}
