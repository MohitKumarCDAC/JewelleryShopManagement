package com.jewellery.jewelleryshop.controller;

import com.jewellery.jewelleryshop.dto.JewelleryItemDto;
import com.jewellery.jewelleryshop.services.JewelleryItemService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/items")
public class JewelleryItemController {

    @Autowired
    private JewelleryItemService jewelleryItemService;

    @PostMapping
    public JewelleryItemDto saveItem(
            @RequestBody JewelleryItemDto dto
    ) {
        return jewelleryItemService.saveItem(dto);
    }

    @GetMapping("/{itemCode}")
    public JewelleryItemDto getItemByCode(
            @PathVariable String itemCode
    ) {
        return jewelleryItemService.getItemByCode(itemCode);
    }

    @GetMapping
    public List<JewelleryItemDto> getAllItems() {
        return jewelleryItemService.getAllItems();
    }

    @PutMapping("/{itemCode}")
    public JewelleryItemDto updateItem(
            @PathVariable String itemCode,
            @RequestBody JewelleryItemDto dto
    ) {
        return jewelleryItemService.updateItem(itemCode, dto);
    }

    @DeleteMapping("/{itemCode}")
    public String deleteItem(
            @PathVariable String itemCode
    ) {
        jewelleryItemService.deleteItem(itemCode);

        return "Item Deleted Successfully";
    }
}