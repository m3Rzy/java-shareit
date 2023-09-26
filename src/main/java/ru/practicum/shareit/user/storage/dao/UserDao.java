package ru.practicum.shareit.user.storage.dao;

import ru.practicum.shareit.user.model.User;

import java.util.List;
import java.util.Optional;

public interface UserDao {
    Optional<User> add(User user);

    Optional<User>  update(long userId, User user);

    Optional<User>  getById(long userId);

    void delete(long userId);

    List<User> getAll();
}
