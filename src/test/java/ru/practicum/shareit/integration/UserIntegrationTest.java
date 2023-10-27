package ru.practicum.shareit.integration;


import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.service.UserServiceImpl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
public class UserIntegrationTest {
    @Autowired
    private UserServiceImpl userService;

    private UserDto userDto;
    private UserDto allUpdatedUserDto;
    private UserDto emailUpdatedUserDto;
    private UserDto nameUpdatedUserDto;

    @BeforeEach
    void setUp() {
        userDto = new UserDto(1L, "User", "user@mail.ru");
    }

    @Test
    void shouldCreateUser() {
        UserDto savedUser = userService.create(userDto);

        assertEquals(userDto.getId(), savedUser.getId());
        assertEquals(userDto.getName(), savedUser.getName());
        assertEquals(userDto.getEmail(), savedUser.getEmail());
    }

    @Test
    void shouldNotGetUser_notFound() {
        UserDto savedUser = userService.create(userDto);

        UserDto returnedUser = userService.getById(savedUser.getId());

        assertEquals(userDto.getId(), returnedUser.getId());
        assertEquals(userDto.getName(), returnedUser.getName());
        assertEquals(userDto.getEmail(), returnedUser.getEmail());
    }

    @Test
    void shouldNotGetUser_userNotFound() {
        Throwable thrown = assertThrows(NotFoundException.class, () -> userService.getById(9999));
        assertEquals("Пользователя с id " + 9999 + " не существует!", thrown.getMessage());
    }

    @Test
    void shouldUpdateUser() {
        allUpdatedUserDto = new UserDto(1, "User Updated", "updated@mail.ru");
        UserDto savedUser = userService.create(userDto);

        UserDto updatedUser = userService.update(allUpdatedUserDto, savedUser.getId());

        assertEquals(allUpdatedUserDto.getId(), updatedUser.getId());
        assertEquals(allUpdatedUserDto.getName(), updatedUser.getName());
        assertEquals(allUpdatedUserDto.getEmail(), updatedUser.getEmail());
    }

    @Test
    void shouldNotUpdateUser_userNotFound() {
        allUpdatedUserDto = new UserDto(1, "User Updated", "updated@mail.ru");

        Throwable thrown = assertThrows(NotFoundException.class, () -> userService.update(allUpdatedUserDto, 9999));
        assertEquals("Пользователь с id " + 9999 + " не найден", thrown.getMessage());
    }

    @Test
    void shouldUpdateUser_nameIsNull() {
        emailUpdatedUserDto = new UserDto(10, null, "test@test.test");

        emailUpdatedUserDto.setId(1);
        emailUpdatedUserDto.setEmail("updated@mail.ru");

        UserDto savedUser = userService.create(userDto);

        UserDto updatedUser = userService.update(emailUpdatedUserDto, savedUser.getId());

        assertEquals(emailUpdatedUserDto.getId(), updatedUser.getId());
        assertEquals(userDto.getName(), updatedUser.getName());
        assertEquals(emailUpdatedUserDto.getEmail(), updatedUser.getEmail());
    }

    @Test
    void shouldUpdateUser_emailIsNull() {
        nameUpdatedUserDto = new UserDto(11, "test", null);

        nameUpdatedUserDto.setId(1);
        nameUpdatedUserDto.setName("User Updated");

        UserDto savedUser = userService.create(userDto);

        UserDto updatedUser = userService.update(nameUpdatedUserDto, savedUser.getId());

        assertEquals(nameUpdatedUserDto.getId(), updatedUser.getId());
        assertEquals(nameUpdatedUserDto.getName(), updatedUser.getName());
        assertEquals(userDto.getEmail(), updatedUser.getEmail());
    }

    @Test
    void shouldNotDeleteUser_userNotFound() {
        Throwable thrown = assertThrows(NotFoundException.class, () -> userService.delete(9999));
        assertEquals("Пользователь с id " + 9999 + " не найден", thrown.getMessage());
    }
}
