package com.jewellery.jewelleryshop.controller;


import com.jewellery.jewelleryshop.dto.CustomerDto;
import com.jewellery.jewelleryshop.services.CustomerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

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

}
