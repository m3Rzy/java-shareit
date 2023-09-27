package ru.practicum.shareit.user.controller;

import lombok.AllArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.service.UserMapper;
import ru.practicum.shareit.user.service.UserService;

import javax.validation.constraints.NotNull;
import java.util.List;

@AllArgsConstructor
@RestController
@RequestMapping(path = "/users")
public class UserController {

    private final UserMapper mapper;
    private final UserService userService;

    @GetMapping
    public List<UserDto> findAllUsers() {
        return mapper.mapUserListToUserDtoList(userService.findAllUsers());
    }

    @GetMapping("/{userId}")
    public UserDto findUserById(@NotNull @PathVariable Long userId) {
        return mapper.toDto(userService.findUserById(userId).get());
    }

    @PostMapping
    public UserDto createUser(@Validated @NotNull @RequestBody UserDto userDto) {
        User user = mapper.toModel(userDto, null);
        return mapper.toDto(userService.createUser(user).get());
    }

    @PatchMapping("/{userId}")
    public UserDto updateUser(@Validated @NotNull @PathVariable Long userId, @RequestBody UserDto userDto) {
        User user = mapper.toModel(userDto, userId);
        return mapper.toDto(userService.updateUser(userId, user).get());
    }

    @DeleteMapping("/{userId}")
    public void deleteUserById(@NotNull @PathVariable Long userId) {
        userService.deleteUserById(userId);
    }
}
