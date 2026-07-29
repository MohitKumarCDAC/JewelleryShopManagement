package com.jewellery.jewelleryshop.services;

import com.jewellery.jewelleryshop.dto.BillDto;

import java.beans.JavaBean;
import java.util.List;

public interface BillService {

    BillDto createBill(BillDto billDto);
    BillDto getBillByBillNumber(String billNumber);
    List<BillDto>getAllBills();
    BillDto payDueAmount(String billNumber, java.math.BigDecimal amount);
    void deleteBill(String billNumber);
}
