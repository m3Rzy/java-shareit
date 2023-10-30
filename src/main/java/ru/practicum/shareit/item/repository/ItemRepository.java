package ru.practicum.shareit.item.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import ru.practicum.shareit.item.model.Item;

import java.util.List;

@Repository
public interface ItemRepository extends JpaRepository<Item, Long> {

    List<Item> findAllByOwnerId(long userId);

    Page<Item> findAllByOwnerId(long userId, Pageable pageable);

    List<Item> findAllByItemRequestId(long itemRequestId);

    List<Item> findAllByItemRequestIdIn(List<Long> requests);

    @Query("SELECT i FROM Item i " +
            "WHERE upper(i.name) LIKE upper(concat('%', ?1, '%')) " +
            "OR upper(i.description) LIKE upper(concat('%', ?1, '%'))")
    Page<Item> search(String text, Pageable pageable);
}