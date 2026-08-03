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
import java.math.RoundingMode;
import java.util.List;

@Service
public class BillServiceImpl implements BillService {

    @Autowired
    private BillRepository billRepository;

    @Autowired
    private BillItemRepository billItemRepository;

    @Autowired
    private CustomerRepositry customerRepositry;

    @Autowired
    private JewelleryItemRepository jewelleryItemRepository;


    // ==========================================
    // AUTO GENERATE BILL NUMBER
    // ==========================================

    private String generateBillNumber() {

        return billRepository.findTopByOrderByIdDesc()
                .map(lastBill -> {

                    String lastBillNumber = lastBill.getBillNumber();

                    String numberPart =
                            lastBillNumber.replace("BILL-", "");

                    int nextNumber =
                            Integer.parseInt(numberPart) + 1;

                    return String.format(
                            "BILL-%04d",
                            nextNumber
                    );
                })
                .orElse("BILL-0001");
    }


    // ==========================================
    // CREATE BILL
    // ==========================================

    @Override
    public BillDto createBill(BillDto billDto) {

        // ==========================================
        // CUSTOMER FETCH
        // ==========================================

        Customer customer =
                customerRepositry
                        .findByMobileNumber(
                                billDto.getCustomerMobile()
                        )
                        .orElseThrow(
                                () -> new RuntimeException(
                                        "Customer Not Found"
                                )
                        );


        // ==========================================
        // BASIC VALUES
        // ==========================================

        BigDecimal discount =
                billDto.getDiscount() != null
                        ? billDto.getDiscount()
                        : BigDecimal.ZERO;

        BigDecimal paidAmount =
                billDto.getPaidAmount() != null
                        ? billDto.getPaidAmount()
                        : BigDecimal.ZERO;


        // ==========================================
        // CREATE BILL
        // ==========================================

        Bill bill = Bill.builder()
                .billNumber(generateBillNumber())
                .customer(customer)
                .discount(discount)
                .paidAmount(paidAmount)
                .paymentMode(billDto.getPaymentMode())
                .status(billDto.getStatus())
                .build();


        // ==========================================
        // TOTAL AMOUNT
        // ==========================================

        BigDecimal totalAmount = BigDecimal.ZERO;


        // ==========================================
        // SAVE BILL FIRST
        // ==========================================

        Bill savedBill = billRepository.save(bill);


        // ==========================================
        // LOOP THROUGH BILL ITEMS
        // ==========================================

        for (BillItemDto itemDto : billDto.getItems()) {

            JewelleryItem jewelleryItem =
                    jewelleryItemRepository
                            .findByItemCode(
                                    itemDto.getItemCode()
                            )
                            .orElseThrow(
                                    () -> new RuntimeException(
                                            "Item not found: "
                                                    + itemDto.getItemCode()
                                    )
                            );


            // ==========================================
            // STOCK CHECK
            // ==========================================

            if (jewelleryItem.getStockQuantity()
                    < itemDto.getQuantity()) {

                throw new RuntimeException(
                        "Insufficient Stock for item: "
                                + itemDto.getItemCode()
                );
            }


            // ==========================================
            // ITEM PRICE
            // ==========================================

            BigDecimal price =
                    jewelleryItem.getPrice();


            BigDecimal quantity =
                    BigDecimal.valueOf(
                            itemDto.getQuantity()
                    );


            // Price × Quantity

            BigDecimal itemPrice =
                    price.multiply(quantity);


            // ==========================================
            // MAKING CHARGE %
            // ==========================================

            BigDecimal makingChargePercent =
                    jewelleryItem.getMakingCharge() != null
                            ? jewelleryItem.getMakingCharge()
                            : BigDecimal.ZERO;


            BigDecimal makingChargeAmount =
                    itemPrice
                            .multiply(makingChargePercent)
                            .divide(
                                    BigDecimal.valueOf(100),
                                    2,
                                    RoundingMode.HALF_UP
                            );


            // ==========================================
            // GST %
            // ==========================================

            Double gstValue =
                    jewelleryItem.getGst() != null
                            ? jewelleryItem.getGst()
                            : 0.0;


            BigDecimal gstPercent =
                    BigDecimal.valueOf(gstValue);


            // GST taxable amount
            // = Item Price + Making Charge

            BigDecimal taxableAmount =
                    itemPrice.add(
                            makingChargeAmount
                    );


            BigDecimal gstAmount =
                    taxableAmount
                            .multiply(gstPercent)
                            .divide(
                                    BigDecimal.valueOf(100),
                                    2,
                                    RoundingMode.HALF_UP
                            );


            // ==========================================
            // ITEM TOTAL
            // ==========================================

            BigDecimal itemTotal =
                    taxableAmount.add(gstAmount);


            // Add to bill total

            totalAmount =
                    totalAmount.add(itemTotal);


            // ==========================================
            // SAVE BILL ITEM
            // ==========================================

            BillItem billItem =
                    BillItem.builder()
                            .bill(savedBill)
                            .jewelleryItem(jewelleryItem)
                            .quantity(
                                    itemDto.getQuantity()
                            )
                            .price(price)
                            .makingCharge(
                                    makingChargePercent
                            )
                            .gst(gstValue)
                            .total(itemTotal)
                            .build();


            billItemRepository.save(billItem);


            // ==========================================
            // REDUCE STOCK
            // ==========================================

            jewelleryItem.setStockQuantity(
                    jewelleryItem.getStockQuantity()
                            - itemDto.getQuantity()
            );

            jewelleryItemRepository.save(
                    jewelleryItem
            );
        }


        // ==========================================
        // SET TOTAL AMOUNT
        // ==========================================

        savedBill.setTotalAmount(totalAmount);


        // ==========================================
        // GRAND TOTAL
        // ==========================================

        BigDecimal grandTotal =
                totalAmount.subtract(discount);


        // Negative Grand Total avoid

        if (grandTotal.compareTo(BigDecimal.ZERO) < 0) {
            grandTotal = BigDecimal.ZERO;
        }


        savedBill.setGrandTotal(grandTotal);


        // ==========================================
        // DUE AMOUNT
        // ==========================================

        BigDecimal dueAmount =
                grandTotal.subtract(paidAmount);


        // Negative Due avoid

        if (dueAmount.compareTo(BigDecimal.ZERO) < 0) {
            dueAmount = BigDecimal.ZERO;
        }


        savedBill.setDueAmount(dueAmount);


        // ==========================================
        // SET BILL STATUS
        // ==========================================

        if (dueAmount.compareTo(BigDecimal.ZERO) == 0) {

            savedBill.setStatus(
                    BillStatus.PAID
            );

        } else if (
                paidAmount.compareTo(BigDecimal.ZERO) > 0
        ) {

            savedBill.setStatus(
                    BillStatus.PARTIAL
            );

        } else {

            savedBill.setStatus(
                    BillStatus.PARTIAL
            );
        }


        // ==========================================
        // SAVE FINAL BILL
        // ==========================================

        billRepository.save(savedBill);


        // ==========================================
        // RETURN RESPONSE
        // ==========================================

        return BillDto.builder()
                .billNumber(
                        savedBill.getBillNumber()
                )
                .customerMobile(
                        customer.getMobileNumber()
                )
                .items(
                        billDto.getItems()
                )
                .discount(
                        savedBill.getDiscount()
                )
                .paidAmount(
                        savedBill.getPaidAmount()
                )
                .paymentMode(
                        savedBill.getPaymentMode()
                )
                .status(
                        savedBill.getStatus()
                )
                .build();
    }


    // ==========================================
    // GET BILL BY BILL NUMBER
    // ==========================================

    @Override
    public BillDto getBillByBillNumber(
            String billNumber
    ) {

        Bill bill =
                billRepository
                        .findByBillNumber(billNumber)
                        .orElseThrow(
                                () -> new RuntimeException(
                                        "Bill not found"
                                )
                        );

        return BillDto.builder()
                .billNumber(
                        bill.getBillNumber()
                )
                .customerMobile(
                        bill.getCustomer()
                                .getMobileNumber()
                )
                .discount(
                        bill.getDiscount()
                )
                .paidAmount(
                        bill.getPaidAmount()
                )
                .paymentMode(
                        bill.getPaymentMode()
                )
                .status(
                        bill.getStatus()
                )
                .build();
    }


    // ==========================================
    // GET ALL BILLS
    // ==========================================

    @Override
    public List<BillDto> getAllBills() {

        return billRepository.findAll()
                .stream()
                .map(bill ->
                        BillDto.builder()
                                .billNumber(
                                        bill.getBillNumber()
                                )
                                .customerMobile(
                                        bill.getCustomer()
                                                .getMobileNumber()
                                )
                                .discount(
                                        bill.getDiscount()
                                )
                                .paidAmount(
                                        bill.getPaidAmount()
                                )
                                .paymentMode(
                                        bill.getPaymentMode()
                                )
                                .status(
                                        bill.getStatus()
                                )
                                .build()
                )
                .toList();
    }


    // ==========================================
    // DELETE BILL
    // ==========================================

    @Override
    public void deleteBill(
            String billNumber
    ) {

        Bill bill =
                billRepository
                        .findByBillNumber(billNumber)
                        .orElseThrow(
                                () -> new RuntimeException(
                                        "Bill Not Found"
                                )
                        );

        billRepository.delete(bill);
    }


    // ==========================================
    // PAY DUE AMOUNT
    // ==========================================

    @Override
    public BillDto payDueAmount(
            String billNumber,
            BigDecimal amount
    ) {

        Bill bill =
                billRepository
                        .findByBillNumber(billNumber)
                        .orElseThrow(
                                () -> new RuntimeException(
                                        "Bill Not Found"
                                )
                        );


        // New Paid Amount

        BigDecimal newPaid =
                bill.getPaidAmount()
                        .add(amount);


        bill.setPaidAmount(newPaid);


        // New Due

        BigDecimal due =
                bill.getGrandTotal()
                        .subtract(newPaid);


        if (due.compareTo(BigDecimal.ZERO) < 0) {
            due = BigDecimal.ZERO;
        }


        bill.setDueAmount(due);


        // Status

        if (due.compareTo(BigDecimal.ZERO) == 0) {

            bill.setStatus(
                    BillStatus.PAID
            );

        } else {

            bill.setStatus(
                    BillStatus.PARTIAL
            );
        }


        billRepository.save(bill);


        return BillDto.builder()
                .billNumber(
                        bill.getBillNumber()
                )
                .customerMobile(
                        bill.getCustomer()
                                .getMobileNumber()
                )
                .discount(
                        bill.getDiscount()
                )
                .paidAmount(
                        bill.getPaidAmount()
                )
                .paymentMode(
                        bill.getPaymentMode()
                )
                .status(
                        bill.getStatus()
                )
                .build();
    }
}