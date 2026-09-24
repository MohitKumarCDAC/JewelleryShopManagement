package com.jewellery.jewelleryshop.services;

import com.jewellery.jewelleryshop.dto.ManualBillItemDto;
import com.jewellery.jewelleryshop.entity.ManualBillItem;
import com.jewellery.jewelleryshop.entity.MetalType;
import com.jewellery.jewelleryshop.repository.ManualBillItemRepository;
import com.jewellery.jewelleryshop.services.ManualBillItemService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ManualBillItemServiceImpl implements ManualBillItemService {

    private final ManualBillItemRepository manualBillItemRepository;

    public ManualBillItemServiceImpl(
            ManualBillItemRepository manualBillItemRepository) {
        this.manualBillItemRepository = manualBillItemRepository;
    }

    // =========================================================
    // GET ALL ITEMS
    // =========================================================

    @Override
    public List<ManualBillItemDto> getAllItems() {

        return manualBillItemRepository
                .findAllByOrderByMetalTypeAscItemNameAsc()
                .stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    // =========================================================
    // GET ITEMS BY GOLD / SILVER
    // =========================================================

    @Override
    public List<ManualBillItemDto> getItemsByMetalType(MetalType metalType) {

        if (metalType == null) {
            throw new IllegalArgumentException("Metal type is required");
        }

        return manualBillItemRepository
                .findByMetalTypeAndActiveTrueOrderByItemNameAsc(metalType)
                .stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    // =========================================================
    // ADD ITEM
    // =========================================================

    @Override
    @Transactional
    public ManualBillItemDto addItem(ManualBillItemDto dto) {

        validateDto(dto);

        String itemName = dto.getItemName().trim();
        MetalType metalType = dto.getMetalType();

        // Duplicate check
        if (manualBillItemRepository
                .existsByItemNameIgnoreCaseAndMetalType(itemName, metalType)) {

            throw new IllegalArgumentException(
                    "Item already exists in " + metalType.name() + ": " + itemName
            );
        }

        ManualBillItem item = new ManualBillItem();

        item.setItemName(itemName);
        item.setMetalType(metalType);
        item.setActive(true);

        ManualBillItem savedItem =
                manualBillItemRepository.save(item);

        return convertToDto(savedItem);
    }

    // =========================================================
    // UPDATE ITEM
    // =========================================================

    @Override
    @Transactional
    public ManualBillItemDto updateItem(
            Long id,
            ManualBillItemDto dto) {

        if (id == null) {
            throw new IllegalArgumentException("Item ID is required");
        }

        validateDto(dto);

        ManualBillItem existingItem =
                manualBillItemRepository.findById(id)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Manual bill item not found with ID: " + id
                                )
                        );

        String itemName = dto.getItemName().trim();
        MetalType metalType = dto.getMetalType();

        // Check duplicate only if another item has same
        // name + metal type
        List<ManualBillItem> allItems =
                manualBillItemRepository.findAll();

        boolean duplicateExists = allItems.stream()
                .anyMatch(item ->
                        !item.getId().equals(id)
                                && item.getItemName() != null
                                && item.getItemName().equalsIgnoreCase(itemName)
                                && item.getMetalType() == metalType
                );

        if (duplicateExists) {
            throw new IllegalArgumentException(
                    "Another item already exists in "
                            + metalType.name()
                            + ": "
                            + itemName
            );
        }

        existingItem.setItemName(itemName);
        existingItem.setMetalType(metalType);

        // Active status only changes through deactivate operation
        if (existingItem.getActive() == null) {
            existingItem.setActive(true);
        }

        ManualBillItem updatedItem =
                manualBillItemRepository.save(existingItem);

        return convertToDto(updatedItem);
    }

    // =========================================================
    // DEACTIVATE ITEM
    // =========================================================

    @Override
    @Transactional
    public void deactivateItem(Long id) {

        if (id == null) {
            throw new IllegalArgumentException("Item ID is required");
        }

        ManualBillItem item =
                manualBillItemRepository.findById(id)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Manual bill item not found with ID: " + id
                                )
                        );

        item.setActive(false);

        manualBillItemRepository.save(item);
    }

    // =========================================================
    // VALIDATION
    // =========================================================

    private void validateDto(ManualBillItemDto dto) {

        if (dto == null) {
            throw new IllegalArgumentException(
                    "Item data is required"
            );
        }

        if (dto.getItemName() == null
                || dto.getItemName().trim().isEmpty()) {

            throw new IllegalArgumentException(
                    "Item name is required"
            );
        }

        if (dto.getItemName().trim().length() > 100) {

            throw new IllegalArgumentException(
                    "Item name cannot exceed 100 characters"
            );
        }

        if (dto.getMetalType() == null) {

            throw new IllegalArgumentException(
                    "Metal type is required"
            );
        }
    }

    // =========================================================
    // ENTITY → DTO
    // =========================================================

    private ManualBillItemDto convertToDto(
            ManualBillItem item) {

        return new ManualBillItemDto(
                item.getId(),
                item.getItemName(),
                item.getMetalType(),
                item.getActive()
        );
    }
}