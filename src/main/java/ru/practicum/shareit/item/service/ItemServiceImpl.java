//package ru.practicum.shareit.item.service;
//
//import lombok.AllArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.stereotype.Service;
//import org.springframework.transaction.annotation.Transactional;
//import ru.practicum.shareit.exception.BadRequestException;
//import ru.practicum.shareit.item.model.Item;
//import ru.practicum.shareit.exception.NotFoundException;
//import ru.practicum.shareit.item.repository.ItemRepository;
//import ru.practicum.shareit.user.model.User;
//import ru.practicum.shareit.user.repository.UserRepository;
//
//import java.util.ArrayList;
//import java.util.List;
//import java.util.Optional;
//import java.util.stream.Collectors;
//
//@Service
//@AllArgsConstructor
//@Slf4j
//@Transactional(readOnly = true)
//public class ItemServiceImpl implements ItemService {
//
//    private final ItemRepository itemRepository;
//    private final UserRepository userRepository;
//
//    @Override
//    @Transactional
//    public Item getItemById(Long itemId) {
//        if (isExist(itemId)) {
//            log.info("Предмет {} был успешно найден с помощью id.", itemId);
//            return itemRepository.findById(itemId).get();
//        } else {
//            throw new NotFoundException("Предмета с id " + itemId + " не существует!");
//        }
//    }
//
//    @Override
//    @Transactional
//    public List<Item> getAll(Long userId) {
//        Optional<User> owner = userRepository.findById(userId);
//        List<Item> itemList = itemRepository.findAllByOwnerId(userId);
//    }
//
//    @Override
//    public Item createItem(Item item) {
//        if (isItemValidationAccess(item)) {
//            boolean ownerExists = isOwnerExists(item.getOwner());
//            if (!ownerExists) {
//                throw new NotFoundException("Владельца вещи не существует.");
//            }
//            itemDao.add(item);
//            log.info("Предмет {} был успешно добавлен!", item);
//            return item;
//        } else {
//            throw new BadRequestException("Атрибуты предмета не прошли валидацию.");
//        }
//    }
//
//    @Override
//    public Item updateItem(Item item) {
//        itemDao.update(item);
//        log.info("Предмет {} был успешно изменён.", item);
//        return itemDao.findById(item.getId()).get();
//    }
//
//    @Override
//    public List<Item> getItemsByRequest(String text) {
//        if (text == null || text.isBlank() || text.length() <= 3) {
//            return new ArrayList<>();
//        }
//        return itemDao.findByRequest(text);
//    }
//
//    private boolean isExist(long itemId) {
//        for (Item item : itemDao.findAll(itemId)) {
//            if (itemId == item.getId()) {
//                return true;
//            }
//        }
//        return false;
//    }
//
//    private boolean isOwnerExists(long ownerId) {
//        List<User> users = userRepository.findAll();
//        List<User> result = users
//                .stream()
//                .filter(user -> user.getId() == ownerId)
//                .collect(Collectors.toList());
//        return result.size() > 0;
//    }
//
//    private boolean isExistUser(long userId) {
//        for (User user : userRepository.findAll()) {
//            if (userId == user.getId()) {
//                return true;
//            }
//        }
//        return false;
//    }
//
//    private boolean isItemValidationAccess(Item item) {
//        if (item.getName() == null || item.getName().isBlank()) {
//            return false;
//        }
//        if (item.getAvailable() == null) {
//            return false;
//        }
//        if (item.getDescription() == null) {
//            return false;
//        }
//        return true;
//    }
//}
