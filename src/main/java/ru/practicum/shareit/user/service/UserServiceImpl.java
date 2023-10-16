package ru.practicum.shareit.user.service;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.exception.BadRequestException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.util.List;
import java.util.Optional;

@Service
@AllArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class UserServiceImpl implements UserService {

    private UserRepository userRepository;

    @Override
    @Transactional
    public List<User> getAll() {
        if (userRepository.findAll().isEmpty()) {
            log.info("Список пользователей пуст. ");
        } else {
            log.info("Количество записей в users: " + userRepository.findAll().size());
        }
        return userRepository.findAll();
    }

    @Override
    @Transactional
    public User getById(long userId) {
        if (isExist(userId)) {
            log.info("Пользователь {} был успешно найден с помощью id.", userId);
            return userRepository.findById(userId).get();
        } else {
            throw new NotFoundException("Пользователя с id " + userId + " не существует!");
        }
    }

    @Override
    @Transactional
    public User createUser(User user) {
        if (user.getEmail() == null) {
            throw new BadRequestException("Почта не может быть пустой.");
        }
        userRepository.save(user);
        log.info("Пользователь {} был успешно добавлен!", user);
        return Optional.ofNullable(user).get();
    }

    @Override
    @Transactional
    public User updateUser(long userId, User user) {
        User userOld = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователя с " + userId + " не существует")
                );
        String name = user.getName();
        if (name != null && !name.isBlank()) {
            userOld.setName(name);
        }
        String email = user.getEmail();
        if (email != null && !email.isBlank()) {
            userOld.setEmail(email);
        }
        log.info("Пользователь {} был успешно изменён.", userOld);
        return userRepository.findById(userId).get();
    }

    @Override
    @Transactional
    public void deleteUserById(long userId) {
        if (isExist(userId)) {
            userRepository.deleteById(userId);
            log.info("Пользователь с id {} был успешно удалён!", userId);
        } else {
            throw new NotFoundException("Пользователя с id " + userId + " не существует!");
        }
    }

    private boolean isExist(long userId) {
        for (User user : userRepository.findAll()) {
            if (userId == user.getId()) {
                return true;
            }
        }
        return false;
    }
}
