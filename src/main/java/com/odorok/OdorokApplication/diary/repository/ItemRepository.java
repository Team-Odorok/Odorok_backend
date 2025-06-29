package com.odorok.OdorokApplication.diary.repository;

import com.odorok.OdorokApplication.draftDomain.Item;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;


public interface ItemRepository extends JpaRepository<Item, Long> {
    Optional<Item> findByTitle(String title);
}
