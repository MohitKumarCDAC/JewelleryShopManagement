package com.jewellery.jewelleryshop.controller;

import com.jewellery.jewelleryshop.dto.ManualBillItemDto;
import com.jewellery.jewelleryshop.entity.MetalType;
import com.jewellery.jewelleryshop.services.ManualBillItemService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/manual-bill-items")
@CrossOrigin(origins = "http://localhost:5173")
public class ManualBillItemController {

    private final ManualBillItemService manualBillItemService;

    public ManualBillItemController(
            ManualBillItemService manualBillItemService) {
        this.manualBillItemService = manualBillItemService;
    }

    // =========================================================
    // GET ALL ITEMS
    // =========================================================

    @GetMapping
    public ResponseEntity<List<ManualBillItemDto>> getAllItems() {

        return ResponseEntity.ok(
                manualBillItemService.getAllItems()
        );
    }

    // =========================================================
    // GET GOLD / SILVER ACTIVE ITEMS
    // =========================================================

    @GetMapping("/{metalType}")
    public ResponseEntity<List<ManualBillItemDto>> getItemsByMetalType(
            @PathVariable MetalType metalType) {

        return ResponseEntity.ok(
                manualBillItemService.getItemsByMetalType(metalType)
        );
    }

    // =========================================================
    // ADD ITEM
    // =========================================================

    @PostMapping
    public ResponseEntity<ManualBillItemDto> addItem(
            @RequestBody ManualBillItemDto dto) {

        ManualBillItemDto savedItem =
                manualBillItemService.addItem(dto);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(savedItem);
    }

    // =========================================================
    // UPDATE ITEM
    // =========================================================

    @PutMapping("/{id}")
    public ResponseEntity<ManualBillItemDto> updateItem(
            @PathVariable Long id,
            @RequestBody ManualBillItemDto dto) {

        ManualBillItemDto updatedItem =
                manualBillItemService.updateItem(id, dto);

        return ResponseEntity.ok(updatedItem);
    }

    // =========================================================
    // DEACTIVATE ITEM
    // =========================================================

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deactivateItem(
            @PathVariable Long id) {

        manualBillItemService.deactivateItem(id);

        return ResponseEntity.ok(
                "Manual bill item deactivated successfully"
        );
    }
}