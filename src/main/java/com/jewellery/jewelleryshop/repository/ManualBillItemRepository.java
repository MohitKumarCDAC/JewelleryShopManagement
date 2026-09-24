package com.jewellery.jewelleryshop.repository;

import com.jewellery.jewelleryshop.entity.ManualBillItem;
import com.jewellery.jewelleryshop.entity.MetalType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ManualBillItemRepository extends JpaRepository<ManualBillItem, Long> {

    // Active Gold/Silver items
    List<ManualBillItem> findByMetalTypeAndActiveTrueOrderByItemNameAsc(MetalType metalType);

    // All items
    List<ManualBillItem> findAllByOrderByMetalTypeAscItemNameAsc();

    // Check duplicate item name within same metal
    boolean existsByItemNameIgnoreCaseAndMetalType(String itemName, MetalType metalType);

    // Find particular item
    Optional<ManualBillItem> findByIdAndActiveTrue(Long id);
}