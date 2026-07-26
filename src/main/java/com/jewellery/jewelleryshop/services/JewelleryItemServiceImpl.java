package com.jewellery.jewelleryshop.services;


import com.jewellery.jewelleryshop.dto.JewelleryItemDto;
import com.jewellery.jewelleryshop.entity.JewelleryItem;
import com.jewellery.jewelleryshop.repository.JewelleryItemRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class JewelleryItemServiceImpl implements JewelleryItemService{


    @Autowired
    private JewelleryItemRepository jewelleryItemRepository;

    @Override
    public JewelleryItemDto saveItem(JewelleryItemDto dto){

        JewelleryItem item=JewelleryItem.builder()
                .itemCode(dto.getItemCode())
                .itemName(dto.getItemName())
                .category(dto.getCategory())
                .purity(dto.getPurity())
                .grossWeight(dto.getGrossWeight())
                .netWeight(dto.getNetWeight())
                .makingCharge(dto.getMakingCharge())
                .gst(dto.getGst())
                .price(dto.getPrice())
                .stockQuantity(dto.getStockQuantity()).build();

        JewelleryItem savedItem=jewelleryItemRepository.save(item);

        return JewelleryItemDto.builder()
                .itemCode(savedItem.getItemCode())
                .itemName(savedItem.getItemName())
                .category(savedItem.getCategory())
                .purity(savedItem.getPurity())
                .grossWeight(savedItem.getGrossWeight())
                .netWeight(savedItem.getNetWeight())
                .makingCharge(savedItem.getMakingCharge())
                .gst(savedItem.getGst())
                .price(savedItem.getPrice())
                .stockQuantity(savedItem.getStockQuantity()).build();
    }

    @Override
    public JewelleryItemDto getItemByCode(String itemCode){

        JewelleryItem item=jewelleryItemRepository.findByItemCode(itemCode)
                .orElseThrow(()->new RuntimeException("item Not Found"));

        return JewelleryItemDto.builder()
                .itemCode(item.getItemCode())
                .itemName(item.getItemName())
                .category(item.getCategory())
                .purity(item.getPurity())
                .grossWeight(item.getGrossWeight())
                .netWeight(item.getNetWeight())
                .makingCharge(item.getMakingCharge())
                .gst(item.getGst())
                .price(item.getPrice())
                .stockQuantity(item.getStockQuantity()).build();

    }

    @Override
    public List<JewelleryItemDto> getAllItems(){
        return jewelleryItemRepository.findAll().stream().map(item->
                JewelleryItemDto.builder()
                        .itemCode(item.getItemCode())
                        .itemName(item.getItemName())
                        .category(item.getCategory())
                        .purity(item.getPurity())
                        .grossWeight(item.getGrossWeight())
                        .netWeight(item.getNetWeight())
                        .makingCharge(item.getMakingCharge())
                        .gst(item.getGst())
                        .price(item.getPrice())
                        .stockQuantity(item.getStockQuantity()).build()).toList();
    }


    @Override
    public JewelleryItemDto updateItem(String itemCode,JewelleryItemDto dto){

        JewelleryItem item=jewelleryItemRepository.findByItemCode(itemCode)
                .orElseThrow(()->new RuntimeException("Item Not Found"));

        item.setItemName(dto.getItemName());
        item.setCategory(dto.getCategory());
        item.setPurity(dto.getPurity());
        item.setGrossWeight(dto.getGrossWeight());
        item.setNetWeight(dto.getNetWeight());
        item.setMakingCharge(dto.getMakingCharge());
        item.setGst(dto.getGst());
        item.setPrice(dto.getPrice());
        item.setStockQuantity(dto.getStockQuantity());


        JewelleryItem updateItem=jewelleryItemRepository.save(item);


        return JewelleryItemDto.builder()
                .itemCode(updateItem.getItemCode())
                .itemName(updateItem.getItemName())
                .category(updateItem.getCategory())
                .purity(updateItem.getPurity())
                .grossWeight(updateItem.getGrossWeight())
                .netWeight(updateItem.getNetWeight())
                .makingCharge(updateItem.getMakingCharge())
                .gst(updateItem.getGst())
                .price(updateItem.getPrice())
                .stockQuantity(updateItem.getStockQuantity()).build();
    }

    @Override
    public void deleteItem(String itemCode)
    {
        JewelleryItem item=jewelleryItemRepository.findByItemCode(itemCode)
                .orElseThrow(()->new RuntimeException("item not Found"));

        jewelleryItemRepository.delete(item);
    }



}
