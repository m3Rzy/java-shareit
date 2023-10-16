package ru.practicum.shareit.user.service;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.exception.BadRequestException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.storage.dao.UserDao;

import java.util.List;
import java.util.Optional;

@Service
@AllArgsConstructor
@Slf4j
public class UserServiceImpl implements UserService {

    private UserDao userDao;

    @Override
    public List<User> getAll() {
        if (userDao.findAll().isEmpty()) {
            log.info("Список пользователей пуст. ");
        } else {
            log.info("Количество записей в users: " + userDao.findAll().size());
        }
        return userDao.findAll();
    }

    @Override
    public User getById(long userId) {
        if (isExist(userId)) {
            log.info("Пользователь {} был успешно найден с помощью id.", userId);
            return userDao.findById(userId).get();
        } else {
            throw new NotFoundException("Пользователя с id " + userId + " не существует!");
        }
    }

    @Override
    public User createUser(User user) {
        if (user.getEmail() == null) {
            throw new BadRequestException("Почта не может быть пустой.");
        }
        userDao.add(user);
        log.info("Пользователь {} был успешно добавлен!", user);
        return Optional.ofNullable(user).get();
    }

    @Override
    public User updateUser(long userId, User user) {
        if (isExist(userId)) {
            userDao.update(userId, user);
            log.info("Пользователь {} был успешно изменён.", user);
            return userDao.findById(userId).get();
        } else {
            throw new NotFoundException("Пользователя с id " + userId + " не существует!");
        }
    }

    @Override
    public void deleteUserById(long userId) {
        if (isExist(userId)) {
            userDao.delete(userId);
            log.info("Пользователь с id {} был успешно удалён!", userId);
        } else {
            throw new NotFoundException("Пользователя с id " + userId + " не существует!");
        }
    }

    private boolean isExist(long userId) {
        for (User user : userDao.findAll()) {
            if (userId == user.getId()) {
                return true;
            }
        }
        return false;
    }
}
