package com.jewellery.jewelleryshop.services;


import com.jewellery.jewelleryshop.dto.CustomerDto;
import com.jewellery.jewelleryshop.dto.CustomerPurchaseHistoryDto;
import com.jewellery.jewelleryshop.entity.Bill;
import com.jewellery.jewelleryshop.entity.Customer;
import com.jewellery.jewelleryshop.repository.BillRepository;
import com.jewellery.jewelleryshop.repository.CustomerRepositry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import com.jewellery.jewelleryshop.entity.Bill;

@Service
public class CustomerServiceImpl implements CustomerService{

    @Autowired
    private CustomerRepositry customerRepositry;

    @Autowired
    private BillRepository billRepository;



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

    @Override
    public CustomerPurchaseHistoryDto getCustomerPurchaseHistory(String mobileNumber)
    {
        List<Bill> bills=billRepository.findByCustomer_MobileNumber(mobileNumber);

        if(bills.isEmpty())
        {
            throw new RuntimeException("No Purchase History found for this mobile number");
        }

        Customer customer=bills.get(0).getCustomer();

        BigDecimal totalPurchaseAmount=BigDecimal.ZERO;
        BigDecimal totalPaidAmount=BigDecimal.ZERO;
        BigDecimal totalDueAmount=BigDecimal.ZERO;

        List<String> billsNumber=new ArrayList<>();

        for(Bill bill:bills)
        {
            if(bill.getGrandTotal()!=null)
            {
                totalPurchaseAmount=totalPurchaseAmount.add(bill.getGrandTotal());
            }
            if(bill.getPaidAmount()!=null)
            {
                totalPaidAmount=totalPaidAmount.add(bill.getPaidAmount());
            }
            if(bill.getDueAmount()!=null)
            {
                totalDueAmount=totalPaidAmount.add(bill.getDueAmount());
            }
            billsNumber.add(bill.getBillNumber());
        }
        return
                CustomerPurchaseHistoryDto.builder()
                        .customerName(customer.getCustomerName())
                        .mobileNumber(customer.getMobileNumber())
                        .totalBills((long)bills.size())
                        .totalPurchaseAmount(totalPurchaseAmount)
                        .totalPaidAmount(totalPaidAmount)
                        .totalDueAmount(totalDueAmount)
                        .billNumbers(billsNumber)
                        .build();
    }


}
