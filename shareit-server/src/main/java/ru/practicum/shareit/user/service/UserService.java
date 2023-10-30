package ru.practicum.shareit.user.service;

import ru.practicum.shareit.user.dto.UserDto;

import java.util.List;

public interface UserService {

    List<UserDto> getAll();

    UserDto getById(long userId);

    UserDto create(UserDto userDto);

    UserDto update(UserDto userDto, long userId);

    void delete(long userId);
}