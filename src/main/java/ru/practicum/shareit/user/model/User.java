package ru.practicum.shareit.user.model;

import lombok.*;

import javax.validation.constraints.Email;

/**
 * TODO Sprint add-controllers.
 */

@ToString
@Getter
@Setter
@Builder
@AllArgsConstructor
public class User {
    private Long id;
    private String name;
    @Email
    private String email;
}
