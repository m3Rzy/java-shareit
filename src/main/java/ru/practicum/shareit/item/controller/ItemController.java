package ru.practicum.shareit.item.controller;

import lombok.AllArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.comment.dto.CommentDto;
import ru.practicum.shareit.item.dto.*;
import ru.practicum.shareit.item.service.ItemService;

import javax.validation.Valid;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import java.util.List;

@RestController
@AllArgsConstructor
@RequestMapping("/items")
public class ItemController {

    public static final String header = "X-Sharer-User-Id";
    private final ItemService itemService;

    @PostMapping
    public ItemDtoRequest createItem(@RequestHeader("X-Sharer-User-Id") long userId,
                                     @Valid @RequestBody ItemDtoInput itemDtoInput) {
        return itemService.create(userId, itemDtoInput);
    }

    @PatchMapping("/{id}")
    public ItemDtoRequest updateItem(@RequestHeader("X-Sharer-User-Id") long userId,
                                     @RequestBody ItemDtoInput itemDtoInput, @PathVariable long id) {
        return itemService.update(userId, itemDtoInput, id);
    }

    @GetMapping("/{id}")
    public ItemDtoComment findItemById(@RequestHeader("X-Sharer-User-Id") long userId,
                                       @PathVariable long id) {
        return itemService.getById(userId, id);
    }

    @GetMapping
    public List<ItemDtoComment> findAll(@RequestHeader("X-Sharer-User-Id") long userId,
                                        @RequestParam(defaultValue = "0") @Min(0) Integer from,
                                        @RequestParam(defaultValue = "10") @Min(1) @Max(200) Integer size) {
        Pageable pageable = PageRequest.of(from / size, size);
        return itemService.getAll(userId, pageable);
    }

    @GetMapping("/search")
    public List<ItemDtoOutput> search(@RequestParam String text,
                                      @RequestParam(value = "from", defaultValue = "0") @Min(0) Integer from,
                                      @RequestParam(value = "size", defaultValue = "10")
                                      @Min(1) @Max(200) Integer size) {
        Pageable pageable = PageRequest.of(from / size, size);
        return itemService.search(text, pageable);
    }

    @PostMapping("/{itemId}/comment")
    public CommentDto createComment(@RequestHeader("X-Sharer-User-Id") long authorId,
                                    @Valid @RequestBody CommentDto commentDto, @PathVariable long itemId) {
        return itemService.comment(authorId, commentDto, itemId);
    }
}