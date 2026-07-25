package com.jewellery.jewelleryshop.services;


import com.jewellery.jewelleryshop.dto.CustomerDto;
import com.jewellery.jewelleryshop.entity.Customer;
import com.jewellery.jewelleryshop.repository.CustomerRepositry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CustomerServiceImpl implements CustomerService{

    @Autowired
    private CustomerRepositry customerRepositry;

    @Override
    public CustomerDto saveCustomer(CustomerDto customerDto){


        Customer customer=Customer.builder()
                .customerName(customerDto.getCustomerName())
                .mobileNumber(customerDto.getMobileNumber())
                .place(customerDto.getPlace()).build();

        Customer saveCustomer =customerRepositry.save(customer);

        return CustomerDto.builder()
                .customerName(saveCustomer.getCustomerName())
                .mobileNumber(saveCustomer.getMobileNumber())
                .place(saveCustomer.getPlace()).build();
    }

    @Override
    public CustomerDto getCustomerByMobile(String mobileNumber){


        Customer customer=customerRepositry.findByMobileNumber(mobileNumber)
                .orElseThrow(()->new RuntimeException("Customer Not Found"));

        return CustomerDto.builder()
                .customerName(customer.getCustomerName())
                .mobileNumber(customer.getMobileNumber())
                .place(customer.getPlace()).build();
    }

    @Override
    public CustomerDto updateCustomer(String mobileNumber,CustomerDto customerDto){
        Customer customer=customerRepositry.findByMobileNumber(mobileNumber)
                .orElseThrow(()->new RuntimeException("Customer Not Found"));

        customer.setCustomerName(customerDto.getCustomerName());
        customer.setMobileNumber(customerDto.getMobileNumber());
        customer.setPlace(customerDto.getPlace());

        Customer updateCustomer=customerRepositry.save(customer);

        return  CustomerDto.builder()
                .customerName(updateCustomer.getCustomerName())
                .mobileNumber(updateCustomer.getMobileNumber())
                .place(updateCustomer.getPlace()).build();
    }

    @Override
    public void deleteCustomer(String mobileNumber){
        Customer customer=customerRepositry.findByMobileNumber(mobileNumber)
                .orElseThrow(()->new RuntimeException("Customer Not Found"));

        customerRepositry.delete(customer);
    }


    @Override
    public List<CustomerDto> getAllCustomers()
    {
        return customerRepositry.findAll()
                .stream().map(customer -> CustomerDto.builder()
                        .customerName(customer.getCustomerName())
                        .mobileNumber(customer.getMobileNumber())
                        .place(customer.getPlace()).build()).toList();


    }


}
