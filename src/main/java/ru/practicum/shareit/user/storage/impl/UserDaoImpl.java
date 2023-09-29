package ru.practicum.shareit.user.storage.impl;

import org.springframework.stereotype.Repository;
import ru.practicum.shareit.exception.EmailConflictException;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.storage.dao.UserDao;

import java.util.*;

@Repository
public class UserDaoImpl implements UserDao {

    private final HashMap<Long, User> users = new HashMap<>();
    private long id = 1;

    @Override
    public List<User> findAll() {
        return new ArrayList<>(users.values());
    }

    @Override
    public Optional<User> findById(long userId) {
        return Optional.ofNullable(users.get(userId));
    }

    @Override
    public User add(User user) {
        checkEmailIsAlreadyExist(user.getEmail());
        user.setId(generateId());
        users.put(user.getId(), user);
        return user;
    }

    @Override
    public User update(long userId, User user) {
        User newUser = users.get(userId);

        if (user.getName() != null) {
            newUser.setName(user.getName());
        }

        if (user.getEmail() != null) {
            for (User userCheckEmail : findAll()) {
                if (userCheckEmail.getEmail().equals(user.getEmail()) && userCheckEmail.getId() != userId) {
                    throw new EmailConflictException("Пользователь с такой почтой уже зарегистрирован.  " + user.getEmail());
                }
            }
            newUser.setEmail(user.getEmail());
        }
        users.put(userId, newUser);
        return users.get(user.getId());
    }

    @Override
    public void delete(long userId) {
        users.remove(userId);
    }

    private long generateId() {
        return id++;
    }

    private void checkEmailIsAlreadyExist(String email) {
        for (User user : users.values()) {
            if (email.equals(user.getEmail())) {
                throw new EmailConflictException("Такая почта уже используется другим пользовалетем.");
            }
        }
    }
}
