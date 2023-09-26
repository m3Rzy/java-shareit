package ru.practicum.shareit.user.service;

import ru.practicum.shareit.user.model.User;

import java.util.List;
import java.util.Optional;

public interface UserService {

    List<User> findAllUsers();

    Optional<User> findUserById(long userId);

    Optional<User> createUser(User user);

    Optional<User> updateUser(long userId, User user);

    void deleteUserById(long userId);
}
