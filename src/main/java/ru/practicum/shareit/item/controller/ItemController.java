package ru.practicum.shareit.item.controller;

import lombok.AllArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.service.ItemMapper;
import ru.practicum.shareit.item.service.ItemService;

import javax.validation.constraints.NotNull;
import java.util.List;

@RestController
@RequestMapping("/items")
@AllArgsConstructor
public class ItemController {

    public static final String USER_ID_HEADER = "X-Sharer-User-Id";

    private final ItemService itemService;
    private final ItemMapper mapper;

    @PostMapping
    public ItemDto createItem(@Validated @RequestBody ItemDto itemDto,
                              @NotNull
                              @RequestHeader(USER_ID_HEADER) Long userId) {
        Item item = mapper.toModel(itemDto, userId);
        return mapper.toDto(itemService.createItem(item).get());
    }

    @PatchMapping("/{itemId}")
    public ItemDto updateItem(@Validated @RequestBody ItemDto itemDto,
                              @PathVariable Long itemId,
                              @NotNull
                              @RequestHeader(USER_ID_HEADER) Long userId) {
        Item item = mapper.toModel(itemDto, userId);
        item.setId(itemId);
        return mapper.toDto(itemService.updateItem(item).get());
    }

    @GetMapping("/{itemId}")
    public ItemDto findItemById(@NotNull @PathVariable Long itemId) {
        return mapper.toDto(itemService.findItemById(itemId).get());
    }

    @GetMapping
    public List<ItemDto> findAllItems(@NotNull @RequestHeader(USER_ID_HEADER) Long userId) {
        List<Item> userItems = itemService.findAllItems(userId);
        return mapper.mapItemListToItemDtoList(userItems);
    }

    @GetMapping("/search")
    public List<ItemDto> findItemsByRequest(@RequestParam String text) {
        List<Item> foundItems = itemService.findItemsByRequest(text);
        return mapper.mapItemListToItemDtoList(foundItems);
    }
}
