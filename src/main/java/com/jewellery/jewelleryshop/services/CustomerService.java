package com.jewellery.jewelleryshop.services;

import com.jewellery.jewelleryshop.dto.CustomerBillingInfoDto;
import com.jewellery.jewelleryshop.dto.CustomerDto;
import com.jewellery.jewelleryshop.dto.CustomerPurchaseHistoryDto;

import java.util.List;

public interface CustomerService {

    // Save customer detail
    CustomerDto saveCustomer(CustomerDto customerDto);

    // Get customer by mobile number
    CustomerDto getCustomerByMobile(String mobileNumber);

    // Search customers by name
    List<CustomerDto> searchCustomersByName(String name);

    // Update customer
    CustomerDto updateCustomer(String mobileNumber, CustomerDto customerDto);

    // Delete customer
    void deleteCustomer(String mobileNumber);

    // Get all customers
    List<CustomerDto> getAllCustomers();

    // Get customer purchase history
    CustomerPurchaseHistoryDto getCustomerPurchaseHistory(String mobileNumber);

    // Get customer billing information
    CustomerBillingInfoDto getCustomerBillingInfo(String mobileNumber);
}