package ru.practicum.shareit.user.service;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.user.exception.EmailBadRequestException;
import ru.practicum.shareit.user.exception.UserNotFoundException;
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
    public List<User> findAllUsers() {
        if (userDao.getAll().isEmpty()) {
            log.info("Список пользователей пуст. ");
        } else {
            log.info("Количество записей в users: " + userDao.getAll().size());
        }
        return userDao.getAll();
    }

    @Override
    public Optional<User> findUserById(long userId) {
        if (isExist(userId)) {
            log.info("Пользователь {} был успешно найден с помощью id.", userId);
            return userDao.getById(userId);
        } else {
            throw new UserNotFoundException("Пользователя с id " + userId + " не существует!");
        }
    }

    @Override
    public Optional<User> createUser(User user) {
        if (user.getEmail() == null) {
            throw new EmailBadRequestException("Почта не может быть пустой.");
        }
        userDao.add(user);
        log.info("Пользователь {} был успешно добавлен!", user);
        return Optional.ofNullable(user);
    }

    @Override
    public Optional<User> updateUser(long userId, User user) {
        if (isExist(userId)) {
            userDao.update(userId, user);
            log.info("Пользователь {} был успешно изменён.", user);
            return userDao.getById(userId);
        } else {
            throw new UserNotFoundException("Пользователя с id " + userId + " не существует!");
        }
    }

    @Override
    public void deleteUserById(long userId) {
        if (isExist(userId)) {
            userDao.delete(userId);
            log.info("Пользователь с id {} был успешно удалён!", userId);
        } else {
            throw new UserNotFoundException("Пользователя с id " + userId + " не существует!");
        }
    }

    private boolean isExist(long userId) {
        for (User user : userDao.getAll()) {
            if (userId == user.getId()) {
                return true;
            }
        }
        return false;
    }
}
