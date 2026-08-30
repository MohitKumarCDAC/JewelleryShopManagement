package com.jewellery.jewelleryshop.controller;

import com.jewellery.jewelleryshop.dto.BillDto;
import com.jewellery.jewelleryshop.dto.OutstandingBillDto;
import com.jewellery.jewelleryshop.services.BillService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/bills")
public class BillController {

    @Autowired
    private BillService billService;


    @PostMapping
    public BillDto createBill(
            @RequestBody BillDto billDto
    ) {
        return billService.createBill(billDto);
    }


    @GetMapping("/{billNumber}")
    public BillDto getBillByBillNumber(
            @PathVariable String billNumber
    ) {
        return billService.getBillByBillNumber(
                billNumber
        );
    }


    @GetMapping
    public List<BillDto> getAllBills() {

        return billService.getAllBills();
    }


    @PutMapping("/pay/{billNumber}")
    public BillDto payDueAmount(
            @PathVariable String billNumber,
            @RequestParam BigDecimal amount
    ) {

        return billService.payDueAmount(
                billNumber,
                amount
        );
    }


    @DeleteMapping("/{billNumber}")
    public String deleteBill(
            @PathVariable String billNumber
    ) {

        billService.deleteBill(billNumber);

        return "Bill Deleted Successfully";
    }


    @GetMapping("/customer/{mobileNumber}")
    public List<BillDto> getBillsByCustomerMobile(
            @PathVariable String mobileNumber
    ) {
        return billService.getBillsByCustomerMobile(mobileNumber);
    }



// ==============================
// GET OUTSTANDING BILLS
// ==============================

    @GetMapping("/outstanding")
    public List<OutstandingBillDto> getOutstandingBills() {

        return billService.getOutstandingBills();
    }


}