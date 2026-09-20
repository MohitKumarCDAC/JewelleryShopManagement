package com.jewellery.jewelleryshop.services;

import com.jewellery.jewelleryshop.dto.BillDto;
import com.jewellery.jewelleryshop.dto.OutstandingBillDto;

import java.math.BigDecimal;
import java.util.List;

public interface BillService {

    BillDto createBill(BillDto billDto);

    BillDto updateBill(String billNumber, BillDto billDto);

    BillDto getBillByBillNumber(String billNumber);

    List<BillDto> getAllBills();

    BillDto payDueAmount(String billNumber, BigDecimal amount);

    void deleteBill(String billNumber);

    List<BillDto> getBillsByCustomerMobile(String mobileNumber);

    List<OutstandingBillDto> getOutstandingBills();
}
