package com.jewellery.jewelleryshop.services;

import com.jewellery.jewelleryshop.dto.BillDto;

import java.math.BigDecimal;
import java.util.List;

public interface BillService {

    BillDto createBill(BillDto billDto);

    BillDto getBillByBillNumber(String billNumber);

    List<BillDto> getAllBills();

    BillDto payDueAmount(
            String billNumber,
            BigDecimal amount
    );

    void deleteBill(String billNumber);

//customer ka sara bill
    List<BillDto> getBillsByCustomerMobile(String mobileNumber);
}