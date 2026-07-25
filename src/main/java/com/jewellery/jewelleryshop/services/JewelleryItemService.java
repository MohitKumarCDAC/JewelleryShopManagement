package com.jewellery.jewelleryshop.services;

import com.jewellery.jewelleryshop.dto.JewelleryItemDto;

import java.util.List;

public interface JewelleryItemService {

    JewelleryItemDto saveItem(JewelleryItemDto dto);

    JewelleryItemDto getItemByCode(String itemCode);

    List<JewelleryItemDto> getAllItems();

    JewelleryItemDto updateItem(String itemCode, JewelleryItemDto dto);


    void deleteItem(String itemCode);
}
