package com.jewellery.jewelleryshop.services;

import com.jewellery.jewelleryshop.dto.CustomerDto;
import com.jewellery.jewelleryshop.dto.CustomerPurchaseHistoryDto;

import java.util.List;

public interface CustomerService {

    //save customer detail
    CustomerDto  saveCustomer(CustomerDto customerDto);

    //getcustomer by mobile number
    CustomerDto getCustomerByMobile(String mobileNumber);


    //update customer
    CustomerDto updateCustomer(String mobileNumber, CustomerDto customerDto);

    //delete customer
    void deleteCustomer(String mobileNumber);

    //getAll customer
    List<CustomerDto> getAllCustomers();

    CustomerPurchaseHistoryDto getCustomerPurchaseHistory(String mobileNumber);

}
