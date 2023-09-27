package ru.practicum.shareit.item.model;

import lombok.*;

@Getter
@Setter
@Builder
@AllArgsConstructor
@ToString
public class Item {
    private Long id;
    private String name;
    private String description;
    private Boolean available;
    private Long owner;
}
