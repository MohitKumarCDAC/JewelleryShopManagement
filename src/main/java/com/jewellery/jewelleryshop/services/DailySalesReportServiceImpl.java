package com.jewellery.jewelleryshop.services;


import com.jewellery.jewelleryshop.dto.DailySalesReportDto;
import com.jewellery.jewelleryshop.entity.Bill;
import com.jewellery.jewelleryshop.repository.BillRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class DailySalesReportServiceImpl implements DailySalesReportService {

    private final BillRepository billRepository;

    public DailySalesReportServiceImpl(BillRepository billRepository)
    {
        this.billRepository=billRepository;
    }
    @Override
    public DailySalesReportDto getTodaySalesReport()
    {
        LocalDateTime today=LocalDateTime.now();

        LocalDateTime startOfDay=today.toLocalDate().atStartOfDay();
        LocalDateTime endOfDay=today.toLocalDate().plusDays(1).atStartOfDay();

        List<Bill> bills=billRepository.findByBillDateBetween(startOfDay,endOfDay);


        long totalBills=bills.size();


        BigDecimal totalSales=bills.stream()
                .map(Bill::getGrandTotal)
                .filter(amount->amount!=null)
                .reduce(BigDecimal.ZERO,BigDecimal::add);


        BigDecimal totalPaidAMount=bills.stream()
                .map(Bill::getPaidAmount)
                .filter(amount->amount!=null)
                .reduce(BigDecimal.ZERO,BigDecimal::add);


        BigDecimal totalDueAmount=bills.stream()
                .map(Bill::getDueAmount)
                .filter(amount->amount!=null)
                .reduce(BigDecimal.ZERO,BigDecimal::add);

        long totalCustomers=bills.stream()
                .map(Bill::getCustomer)
                .distinct().count();

        return DailySalesReportDto.builder()
                .totalBills(totalBills)
                .totalSales(totalSales)
                .totalPaidAmount(totalPaidAMount)
                .totalDueAmount(totalDueAmount)
                .totalCustomers(totalCustomers)
                .build();
    }

}
