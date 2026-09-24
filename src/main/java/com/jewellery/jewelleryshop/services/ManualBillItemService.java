package com.jewellery.jewelleryshop.services;

import com.jewellery.jewelleryshop.dto.ManualBillItemDto;
import com.jewellery.jewelleryshop.entity.MetalType;

import java.util.List;

public interface ManualBillItemService {

    List<ManualBillItemDto> getAllItems();

    List<ManualBillItemDto> getItemsByMetalType(MetalType metalType);

    ManualBillItemDto addItem(ManualBillItemDto dto);

    ManualBillItemDto updateItem(Long id, ManualBillItemDto dto);

    void deactivateItem(Long id);
}