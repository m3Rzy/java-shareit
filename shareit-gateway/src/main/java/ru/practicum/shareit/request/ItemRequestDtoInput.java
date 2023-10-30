package ru.practicum.shareit.request;

import lombok.AllArgsConstructor;
import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
@AllArgsConstructor
public class ItemRequestDtoInput {
    private long id;

    @NotBlank
    private String description;
}
