package ru.practicum.shareit.unit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.mapper.UserMapper;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;
import ru.practicum.shareit.user.service.UserServiceImpl;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@ExtendWith(MockitoExtension.class)
public class UserMockTest {

    private UserDto userDto;
    private UserDto allUpdatedUserDto;
    private User user;

    @Mock
    private UserRepository userRepository;
    @InjectMocks
    private UserServiceImpl userService;

    @BeforeEach
    void setUp() {
        userDto = new UserDto(1L, "Юзер", "test@test.test");
        user = UserMapper.mapToUser(userDto);
    }

    @Test
    void shouldCreateUser() {
        Mockito.when(userRepository.save(Mockito.any()))
                .thenReturn(user);

        UserDto createdUser = userService.create(userDto);
        Mockito.verify(userRepository).save(Mockito.any());

        assertEquals(userDto.getId(), createdUser.getId());
        assertEquals(userDto.getName(), createdUser.getName());
        assertEquals(userDto.getEmail(), createdUser.getEmail());
    }

    @Test
    void shouldGetUserById() {
        Mockito.when(userRepository.findById(1L))
                .thenReturn(Optional.of(user));

        UserDto returnedUser = userService.getById(1);
        Mockito.verify(userRepository).findById(1L);

        assertEquals(userDto.getId(), returnedUser.getId());
        assertEquals(userDto.getName(), returnedUser.getName());
        assertEquals(userDto.getEmail(), returnedUser.getEmail());
    }

    @Test
    void shouldNotGetUserById_notFoundException() {
        Mockito.when(userRepository.findById(999L))
                .thenReturn(Optional.empty());
        NotFoundException exception = assertThrows(NotFoundException.class,
                () -> userService.getById(999));

        assertEquals("Пользователя с id " + 999 + " не существует!", exception.getMessage());
    }

    @Test
    void shouldUpdateUserById() {
        allUpdatedUserDto = new UserDto(1, "Обновленный юзер", "updatetest@updatetest.updatetest");

        Mockito.when(userRepository.findById(1L))
                .thenReturn(Optional.of(user));

        Mockito.when(userRepository.save(Mockito.any()))
                .thenReturn(UserMapper.mapToUser(allUpdatedUserDto));

        UserDto updatedUser = userService.update(allUpdatedUserDto, 1);
        Mockito.verify(userRepository).save(Mockito.any());

        assertEquals(allUpdatedUserDto.getName(), updatedUser.getName());
        assertEquals(allUpdatedUserDto.getEmail(), updatedUser.getEmail());
    }

    @Test
    void shouldNotUpdateUserById() {
        Mockito.when(userRepository.findById(999L))
                .thenReturn(Optional.empty());
        NotFoundException exception = assertThrows(NotFoundException.class,
                () -> userService.update(allUpdatedUserDto, 999));

        assertEquals("Пользователь с id " + 999 + " не найден", exception.getMessage());
    }

    @Test
    void shouldNotDeleteUserById_notFoundException() {
        Mockito.when(userRepository.findById(999L)).thenReturn(Optional.empty());

        NotFoundException notFoundException = assertThrows(NotFoundException.class,
                () -> userService.delete(999));

        assertEquals("Пользователь с id " + 999 + " не найден", notFoundException.getMessage());
    }

    @Test
    void shouldUpdateUserById_nameIsNull() {
        UserDto newEmailUserDto = new UserDto(3L, "Юзер", "test@test.test");
        newEmailUserDto.setEmail("updatetest@updatetest.updatetest");
        UserDto emailUpdatedUserDto = new UserDto(1, "Юзер", newEmailUserDto.getEmail());

        Mockito.when(userRepository.findById(1L))
                .thenReturn(Optional.of(user));

        Mockito.when(userRepository.save(Mockito.any()))
                .thenReturn(UserMapper.mapToUser(emailUpdatedUserDto));

        UserDto updatedUser = userService.update(newEmailUserDto, 1);
        Mockito.verify(userRepository).save(Mockito.any());

        assertEquals(userDto.getName(), updatedUser.getName());
        assertEquals(newEmailUserDto.getEmail(), updatedUser.getEmail());
    }

    @Test
    void shouldUpdateUserById_emailIsNull() {
        UserDto newNameUserDto = new UserDto(3L, "Юзер", "test@test.test");

        newNameUserDto.setName("test");

        UserDto nameUpdatedUserDto = new UserDto(1, "test", "test@test.test");

        Mockito.when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        Mockito.when(userRepository.save(Mockito.any())).thenReturn(UserMapper.mapToUser(nameUpdatedUserDto));

        UserDto updatedUser = userService.update(newNameUserDto, 1);

        Mockito.verify(userRepository).save(Mockito.any());

        assertEquals(newNameUserDto.getName(), updatedUser.getName());
        assertEquals(userDto.getEmail(), updatedUser.getEmail());
    }
}
