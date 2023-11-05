package ru.practicum.shareit.user.service;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.mapper.UserMapper;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.util.List;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
@Slf4j
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    @Override
    @Transactional
    public List<UserDto> getAll() {
        if (userRepository.findAll().isEmpty()) {
            log.info("Список пользователей пуст.");
        } else {
            log.info("Количество записей в users: " + userRepository.findAll().size());
        }
        return userRepository.findAll()
                .stream()
                .map(UserMapper::mapToUserDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public UserDto getById(long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователя с id " + userId + " не существует!"));
        log.info("Пользователь {} был успешно найден с помощью id.", userId);
        return UserMapper.mapToUserDto(user);
    }

    @Override
    @Transactional
    public UserDto create(UserDto userDto) {
        User user = UserMapper.mapToUser(userDto);
        log.info("Пользователь {} успешно добавлен.", user);
        return UserMapper.mapToUserDto(userRepository.save(user));
    }

    @Override
    @Transactional
    public UserDto update(UserDto userDto, long userId) {
        User updatedUser = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь с id " + userId + " не найден"));
        if (userDto.getName() != null) {
            updatedUser.setName(userDto.getName());
        }
        if (userDto.getEmail() != null) {
            updatedUser.setEmail(userDto.getEmail());
        }
        log.info("Пользователь {} был успешно изменён.", updatedUser);
        return UserMapper.mapToUserDto(userRepository.save(updatedUser));
    }

    @Override
    @Transactional
    public void delete(long userId) {
        userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь с id " + userId + " не найден"));
        log.info("Пользователь с id {} был успешно удалён!", userId);
        userRepository.deleteById(userId);
    }
}