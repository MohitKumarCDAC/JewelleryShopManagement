package com.jewellery.jewelleryshop.services;


import com.jewellery.jewelleryshop.dto.JewelleryItemDto;
import com.jewellery.jewelleryshop.entity.JewelleryItem;
import com.jewellery.jewelleryshop.repository.JewelleryItemRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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
    }
}
