package com.jewellery.jewelleryshop.services;

import com.jewellery.jewelleryshop.dto.SalesReportDto;
import com.jewellery.jewelleryshop.entity.Bill;
import com.jewellery.jewelleryshop.repository.BillRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SalesReportServiceImpl
        implements SalesReportService {

    private final BillRepository billRepository;


    @Override
    public SalesReportDto getSalesReport(
            LocalDate startDate,
            LocalDate endDate
    ) {

        // =========================
        // SELECTED DATE RANGE
        // =========================

        LocalDateTime startDateTime =
                startDate.atStartOfDay();

        LocalDateTime endDateTime =
                endDate.plusDays(1).atStartOfDay();


        // Selected date range ke bills
        List<Bill> bills =
                billRepository.findByBillDateBetween(
                        startDateTime,
                        endDateTime
                );


        // =========================
        // VARIABLES
        // =========================

        BigDecimal totalSales =
                BigDecimal.ZERO;

        BigDecimal totalPaid =
                BigDecimal.ZERO;

        BigDecimal totalDue =
                BigDecimal.ZERO;

        BigDecimal cashSales =
                BigDecimal.ZERO;

        BigDecimal upiSales =
                BigDecimal.ZERO;

        BigDecimal cardSales =
                BigDecimal.ZERO;

        BigDecimal bankTransferSales =
                BigDecimal.ZERO;


        // =========================
        // PROCESS SELECTED DATE RANGE
        // =========================

        for (Bill bill : bills) {

            // -------------------------
            // Total Sales
            // -------------------------

            if (bill.getGrandTotal() != null) {

                totalSales =
                        totalSales.add(
                                bill.getGrandTotal()
                        );
            }


            // -------------------------
            // Paid Amount
            // -------------------------

            if (bill.getPaidAmount() != null) {

                totalPaid =
                        totalPaid.add(
                                bill.getPaidAmount()
                        );
            }


            // -------------------------
            // Due Amount
            // -------------------------

            if (bill.getDueAmount() != null) {

                totalDue =
                        totalDue.add(
                                bill.getDueAmount()
                        );
            }


            // -------------------------
            // Payment Mode
            // -------------------------

            if (bill.getPaymentMode() != null
                    && bill.getGrandTotal() != null) {

                switch (bill.getPaymentMode()) {

                    case CASH:

                        cashSales =
                                cashSales.add(
                                        bill.getGrandTotal()
                                );

                        break;


                    case UPI:

                        upiSales =
                                upiSales.add(
                                        bill.getGrandTotal()
                                );

                        break;


                    case CARD:

                        cardSales =
                                cardSales.add(
                                        bill.getGrandTotal()
                                );

                        break;


                    case BANK_TRANSFER:

                        bankTransferSales =
                                bankTransferSales.add(
                                        bill.getGrandTotal()
                                );

                        break;


                    default:
                        break;
                }
            }
        }


        // =========================
        // TODAY / YESTERDAY SALES
        // INDEPENDENT OF DATE FILTER
        // =========================

        LocalDate today =
                LocalDate.now();

        LocalDate yesterday =
                today.minusDays(1);


        // -------------------------
        // TODAY
        // -------------------------

        LocalDateTime todayStart =
                today.atStartOfDay();

        LocalDateTime todayEnd =
                today.plusDays(1).atStartOfDay();


        List<Bill> todayBills =
                billRepository.findByBillDateBetween(
                        todayStart,
                        todayEnd
                );


        BigDecimal todaySales =
                BigDecimal.ZERO;


        for (Bill bill : todayBills) {

            if (bill.getGrandTotal() != null) {

                todaySales =
                        todaySales.add(
                                bill.getGrandTotal()
                        );
            }
        }


        // -------------------------
        // YESTERDAY
        // -------------------------

        LocalDateTime yesterdayStart =
                yesterday.atStartOfDay();

        LocalDateTime yesterdayEnd =
                today.atStartOfDay();


        List<Bill> yesterdayBills =
                billRepository.findByBillDateBetween(
                        yesterdayStart,
                        yesterdayEnd
                );


        BigDecimal yesterdaySales =
                BigDecimal.ZERO;


        for (Bill bill : yesterdayBills) {

            if (bill.getGrandTotal() != null) {

                yesterdaySales =
                        yesterdaySales.add(
                                bill.getGrandTotal()
                        );
            }
        }


        // =========================
        // RESPONSE
        // =========================

        return SalesReportDto.builder()

                .totalBills(
                        bills.size()
                )

                .totalSales(
                        totalSales
                )

                .totalPaid(
                        totalPaid
                )

                .totalDue(
                        totalDue
                )

                .cashSales(
                        cashSales
                )

                .upiSales(
                        upiSales
                )

                .cardSales(
                        cardSales
                )

                .bankTransferSales(
                        bankTransferSales
                )

                .todaySales(
                        todaySales
                )

                .yesterdaySales(
                        yesterdaySales
                )

                .build();
    }
}