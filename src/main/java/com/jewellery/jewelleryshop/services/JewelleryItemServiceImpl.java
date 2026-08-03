package com.jewellery.jewelleryshop.services;

import com.jewellery.jewelleryshop.dto.JewelleryItemDto;
import com.jewellery.jewelleryshop.entity.JewelleryItem;
import com.jewellery.jewelleryshop.repository.JewelleryItemRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class JewelleryItemServiceImpl implements JewelleryItemService {

    @Autowired
    private JewelleryItemRepository jewelleryItemRepository;

    @Override
    public JewelleryItemDto saveItem(JewelleryItemDto dto) {

        JewelleryItem item = JewelleryItem.builder()
                .itemCode(dto.getItemCode())
                .itemName(dto.getItemName())
                .category(dto.getCategory())
                .purity(dto.getPurity())
                .weight(dto.getWeight())
                .stockQuantity(dto.getStockQuantity())
                .build();

        JewelleryItem savedItem = jewelleryItemRepository.save(item);

        return convertToDto(savedItem);
    }

    @Override
    public JewelleryItemDto getItemByCode(String itemCode) {

        JewelleryItem item = jewelleryItemRepository.findByItemCode(itemCode)
                .orElseThrow(() -> new RuntimeException("Item Not Found"));

        return convertToDto(item);
    }

    @Override
    public List<JewelleryItemDto> getAllItems() {

        return jewelleryItemRepository.findAll()
                .stream()
                .map(this::convertToDto)
                .toList();
    }

    @Override
    public JewelleryItemDto updateItem(
            String itemCode,
            JewelleryItemDto dto
    ) {

        JewelleryItem item = jewelleryItemRepository.findByItemCode(itemCode)
                .orElseThrow(() -> new RuntimeException("Item Not Found"));

        item.setItemName(dto.getItemName());
        item.setCategory(dto.getCategory());
        item.setPurity(dto.getPurity());
        item.setWeight(dto.getWeight());
        item.setStockQuantity(dto.getStockQuantity());

        JewelleryItem updatedItem =
                jewelleryItemRepository.save(item);

        return convertToDto(updatedItem);
    }

    @Override
    public void deleteItem(String itemCode) {

        JewelleryItem item = jewelleryItemRepository.findByItemCode(itemCode)
                .orElseThrow(() -> new RuntimeException("Item Not Found"));

        jewelleryItemRepository.delete(item);
    }

    private JewelleryItemDto convertToDto(JewelleryItem item) {

        return JewelleryItemDto.builder()
                .itemCode(item.getItemCode())
                .itemName(item.getItemName())
                .category(item.getCategory())
                .purity(item.getPurity())
                .weight(item.getWeight())
                .stockQuantity(item.getStockQuantity())
                .build();
    }
}