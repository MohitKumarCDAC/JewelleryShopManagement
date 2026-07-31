package com.jewellery.jewelleryshop.controller;


import com.jewellery.jewelleryshop.dto.CustomerDto;
import com.jewellery.jewelleryshop.dto.CustomerPurchaseHistoryDto;
import com.jewellery.jewelleryshop.services.CustomerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/customers")
public class CustomerController {

    @Autowired
    private CustomerService customerService;

    @PostMapping
    public CustomerDto saveCustomer(@RequestBody CustomerDto customerDto){
        return
                customerService.saveCustomer(customerDto);
    }

    @GetMapping("/{mobileNumber}")
    public CustomerDto getCustomerByMobile(@PathVariable String mobileNumber){
        return
                customerService.getCustomerByMobile(mobileNumber);
    }

    @PutMapping("/{mobileNumber}")
    public CustomerDto updateCustomer(@PathVariable String mobileNumber, @RequestBody CustomerDto customerDto){
        return
                customerService.updateCustomer(mobileNumber,customerDto);
    }

    @DeleteMapping("/{mobileNumber}")
    public String deleteCustomer(@PathVariable String mobileNumber)
    {
        customerService.deleteCustomer(mobileNumber);
        return "Customer Deleted Successfully";
    }

    @GetMapping
    public List<CustomerDto> getAllCustomers()
    {
        return customerService.getAllCustomers();
    }

    @GetMapping("/history/{mobileNumber}")
    public CustomerPurchaseHistoryDto getCustomerPurchaseHistory(@PathVariable String mobileNumber)
    {
        return customerService.getCustomerPurchaseHistory(mobileNumber);
    }

}
