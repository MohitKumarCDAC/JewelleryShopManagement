package com.jewellery.jewelleryshop.services;


import com.jewellery.jewelleryshop.dto.DashboardDto;
import com.jewellery.jewelleryshop.entity.Bill;
import com.jewellery.jewelleryshop.repository.BillRepository;
import com.jewellery.jewelleryshop.repository.CustomerRepositry;
import com.jewellery.jewelleryshop.repository.JewelleryItemRepository;
import com.jewellery.jewelleryshop.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DashboardServiceImpl implements  DashboardService{


    private final CustomerRepositry customerRepositry;

    private final JewelleryItemRepository jewelleryItemRepository;

    private final BillRepository billRepository;

    private final PaymentRepository paymentRepository;

    @Override
    public DashboardDto getDashboardData()
    {
        Long totalCustomers=customerRepositry.count();
        Long totalJewelleryItems=jewelleryItemRepository.count();
        Long totalBills=billRepository.count();
        Long totalPayments=paymentRepository.count();

        List<Bill>bills=billRepository.findAll();


        BigDecimal totalsales=BigDecimal.ZERO;
        BigDecimal totalDueAmount=BigDecimal.ZERO;

        for(Bill bill:bills){
            if(bill.getGrandTotal()!=null)
            {
                totalsales=totalsales.add(bill.getGrandTotal());
            }
            if(bill.getDueAmount()!=null)
            {
                totalDueAmount=totalDueAmount=totalDueAmount.add(bill.getDueAmount());
            }
        }
        return DashboardDto.builder()
                .totalCustomers(totalCustomers)
                .totalJewelleryItems(totalJewelleryItems)
                .totalSales(totalsales)
                .totalDueAmount(totalDueAmount).build();

    }


}
