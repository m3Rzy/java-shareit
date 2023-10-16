package ru.practicum.shareit.user.storage.dao;

import ru.practicum.shareit.user.model.User;

import java.util.List;
import java.util.Optional;

public interface UserDao {
    User add(User user);

    User update(long userId, User user);

    Optional<User> findById(long userId);

    void delete(long userId);

    List<User> findAll();
}
