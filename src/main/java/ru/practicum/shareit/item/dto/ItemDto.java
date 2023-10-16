package ru.practicum.shareit.item.dto;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@ToString
public class ItemDto {
    private Long id;
    private String name;
    private String description;
    private Boolean available;
}
