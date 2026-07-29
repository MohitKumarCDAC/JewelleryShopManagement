package com.jewellery.jewelleryshop.services;


import com.jewellery.jewelleryshop.dto.BillDto;
import com.jewellery.jewelleryshop.dto.BillItemDto;
import com.jewellery.jewelleryshop.entity.*;
import com.jewellery.jewelleryshop.repository.BillItemRepository;
import com.jewellery.jewelleryshop.repository.BillRepository;
import com.jewellery.jewelleryshop.repository.CustomerRepositry;
import com.jewellery.jewelleryshop.repository.JewelleryItemRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
public class BillServiceImpl  implements BillService{

    @Autowired
    private BillRepository billRepository;

    @Autowired
    private BillItemRepository billItemRepository;

    @Autowired
    private CustomerRepositry customerRepositry;

    @Autowired
    private JewelleryItemRepository jewelleryItemRepository;

    @Override
    public BillDto createBill(BillDto billDto){

        //customer Fetch
        Customer customer=customerRepositry.findByMobileNumber(billDto.getCustomerMobile())
                .orElseThrow(()->new RuntimeException("Customer Not Found"));

        //Bill create
        Bill bill=Bill.builder()
                .billNumber(billDto.getBillNumber())
                .customer(customer)
                .discount(billDto.getDiscount())
                .paidAmount(billDto.getPaidAmount())
                .paymentMode(billDto.getPaymentMode())
                .status(billDto.getStatus()).build();

        BigDecimal totalAmount=BigDecimal.ZERO;

        //save bill first
        Bill savedBill=billRepository.save(bill);

        //loop through items

        for(BillItemDto itemDto:billDto.getItems())
        {
            JewelleryItem jewelleryItem=jewelleryItemRepository.findByItemCode(itemDto.getItemCode())
                    .orElseThrow(()->new RuntimeException("item not found"));

            //stock check
            if(jewelleryItem.getStockQuantity()< itemDto.getQuantity())
            {
                throw  new RuntimeException("insufficient Stock");
            }

            BigDecimal itemTotal=jewelleryItem.getPrice().multiply(BigDecimal.valueOf(itemDto.getQuantity()));

            totalAmount=totalAmount.add(itemTotal);


            BillItem billItem=BillItem.builder()
                    .bill(savedBill)
                    .jewelleryItem(jewelleryItem)
                    .quantity(itemDto.getQuantity())
                    .price(jewelleryItem.getPrice())
                    .makingCharge(jewelleryItem.getMakingCharge())
                    .gst(jewelleryItem.getGst())
                    .total(itemTotal)
                    .build();


            billItemRepository.save(billItem);


            //Reduce Stock
            jewelleryItem.setStockQuantity(jewelleryItem.getStockQuantity()-itemDto.getQuantity());

            //save ReStock
            jewelleryItemRepository.save(jewelleryItem);

        }

        //total
        savedBill.setTotalAmount(totalAmount);

        //find grandTotal
        BigDecimal grandTotal=totalAmount.subtract(savedBill.getDiscount());
        //save Grand Total
        savedBill.setGrandTotal(grandTotal);

        //find due Amount
        BigDecimal due=grandTotal.subtract(savedBill.getPaidAmount());
        //dueAmount Set
        savedBill.setDueAmount(due);
        //save Bill
        billRepository.save(savedBill);
        return billDto;
    }

    @Override
    public BillDto getBillByBillNumber(String billNumber)
    {
        Bill bill=billRepository.findByBillNumber(billNumber)
                .orElseThrow(()-> new RuntimeException("Bill not found"));

        return BillDto.builder()
                .billNumber(bill.getBillNumber())
                .customerMobile(bill.getCustomer().getMobileNumber())
                .discount(bill.getDiscount())
                .paidAmount(bill.getPaidAmount())
                .paymentMode(bill.getPaymentMode())
                .status(bill.getStatus())
                .build();

    }

    @Override
    public List<BillDto>getAllBills(){
        return billRepository.findAll().stream()
                .map(bill -> BillDto.builder()
                        .billNumber(bill.getBillNumber())
                        .customerMobile(bill.getCustomer().getMobileNumber())
                        .discount(bill.getDiscount())
                        .paidAmount(bill.getPaidAmount())
                        .paymentMode(bill.getPaymentMode())
                        .status(bill.getStatus()).build()).toList();

    }

    @Override
    public void deleteBill(String billNumber)
    {
        Bill bill=billRepository.findByBillNumber(billNumber)
                .orElseThrow(()-> new RuntimeException("Bill Not Fund"));

        billRepository.delete(bill);
    }


    @Override
    public BillDto payDueAmount(String billNumber,BigDecimal amount)
    {
        Bill bill=billRepository.findByBillNumber(billNumber)
                .orElseThrow(()-> new RuntimeException("Bill Not Found"));
// customer jb aaye bkaya bill paid krne tb
        BigDecimal newPaid=bill.getPaidAmount().add(amount);
    //paisa lekr set kr diye
        bill.setPaidAmount(newPaid);
        //then phir se new due amount set kre
        BigDecimal due=bill.getGrandTotal().subtract(newPaid);

        bill.setDueAmount(due);
//agr paisa dene baad pura jama ho ajye to paid likh dega nhi to partial likh dega mtlb ki or due hai
        if(due.compareTo(BigDecimal.ZERO)<= 0)
        {
            bill.setStatus(BillStatus.PAID);
        }else{
            bill.setStatus(BillStatus.PARTIAL);
        }

        billRepository.save(bill);

        return BillDto.builder()
                .billNumber(bill.getBillNumber())
                .customerMobile(bill.getCustomer().getMobileNumber())
                .discount(bill.getDiscount())
                .paidAmount(bill.getPaidAmount())
                .paymentMode(bill.getPaymentMode())
                .status(bill.getStatus())
                .build();
    }
}
