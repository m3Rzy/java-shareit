package ru.practicum.shareit.user.service;

import ru.practicum.shareit.user.model.User;

import java.util.List;

public interface UserService {

    List<User> getAll();

    User getById(long userId);

    User createUser(User user);

    User updateUser(long userId, User user);

    void deleteUserById(long userId);
}
