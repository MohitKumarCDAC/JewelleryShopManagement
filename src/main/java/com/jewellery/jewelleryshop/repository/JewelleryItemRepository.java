package com.jewellery.jewelleryshop.repository;

import com.jewellery.jewelleryshop.entity.JewelleryItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface JewelleryItemRepository extends JpaRepository<JewelleryItem, Long> {

    Optional<JewelleryItem> findByItemCode(String itemCode);
}