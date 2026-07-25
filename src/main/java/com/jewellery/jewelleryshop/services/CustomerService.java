package com.jewellery.jewelleryshop.services;

import com.jewellery.jewelleryshop.dto.CustomerDto;

public interface CustomerService {

    CustomerDto  saveCustomer(CustomerDto customerDto);

    CustomerDto getCustomerByMobile(String mobileNumber);

    CustomerDto updateCustomer(String mobileNumber, CustomerDto customerDto);
}
