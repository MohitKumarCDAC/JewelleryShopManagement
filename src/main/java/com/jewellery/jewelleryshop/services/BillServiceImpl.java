
package com.jewellery.jewelleryshop.services;

import com.jewellery.jewelleryshop.dto.BillDto;
import com.jewellery.jewelleryshop.dto.BillItemDto;
import com.jewellery.jewelleryshop.entity.*;
import com.jewellery.jewelleryshop.repository.BillItemRepository;
import com.jewellery.jewelleryshop.repository.BillRepository;
import com.jewellery.jewelleryshop.repository.CustomerRepositry;
import com.jewellery.jewelleryshop.repository.JewelleryItemRepository;
import com.jewellery.jewelleryshop.repository.PaymentHistoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
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

    @Autowired
    private PaymentHistoryRepository paymentHistoryRepository;


    // ==============================
    // CREATE BILL
    // ==============================

    @Override
    @Transactional
    public BillDto createBill(BillDto billDto) {

        // Customer fetch
        Customer customer = customerRepositry
                .findByMobileNumber(billDto.getCustomerMobile())
                .orElseThrow(() ->
                        new RuntimeException("Customer Not Found"));


        // Generate Bill Number automatically
        String billNumber = generateBillNumber();


        // Create Bill
        Bill bill = Bill.builder()
                .billNumber(billNumber)
                .customer(customer)
                .discount(
                        billDto.getDiscount() == null
                                ? BigDecimal.ZERO
                                : billDto.getDiscount()
                )
                .paidAmount(
                        billDto.getPaidAmount() == null
                                ? BigDecimal.ZERO
                                : billDto.getPaidAmount()
                )
                .paymentMode(billDto.getPaymentMode())
                .status(BillStatus.PARTIAL)
                .build();


        // Save Bill first
        Bill savedBill = billRepository.save(bill);


        BigDecimal totalAmount = BigDecimal.ZERO;
        BigDecimal totalGst = BigDecimal.ZERO;


        // ==============================
        // ITEMS
        // ==============================

        for (BillItemDto itemDto : billDto.getItems()) {

            JewelleryItem jewelleryItem =
                    jewelleryItemRepository
                            .findByItemCode(itemDto.getItemCode())
                            .orElseThrow(() ->
                                    new RuntimeException(
                                            "Item Not Found: "
                                                    + itemDto.getItemCode()
                                    ));


            // Quantity validation
            if (itemDto.getQuantity() == null ||
                    itemDto.getQuantity() <= 0) {

                throw new RuntimeException(
                        "Quantity must be greater than zero"
                );
            }


            // Stock check
            if (jewelleryItem.getStockQuantity() == null ||
                    jewelleryItem.getStockQuantity()
                            < itemDto.getQuantity()) {

                throw new RuntimeException(
                        "Insufficient Stock for Item: "
                                + itemDto.getItemCode()
                );
            }


            // Required billing values
            if (itemDto.getMetalRate() == null ||
                    itemDto.getMetalRate()
                            .compareTo(BigDecimal.ZERO) <= 0) {

                throw new RuntimeException(
                        "Metal rate must be greater than zero"
                );
            }


            BigDecimal makingPercent =
                    itemDto.getMakingChargePercent() == null
                            ? BigDecimal.ZERO
                            : itemDto.getMakingChargePercent();


            BigDecimal gstPercent =
                    itemDto.getGstPercent() == null
                            ? BigDecimal.ZERO
                            : itemDto.getGstPercent();


            // ==============================
            // METAL VALUE
            // ==============================

            BigDecimal weight = jewelleryItem.getWeight();


            BigDecimal quantity =
                    BigDecimal.valueOf(
                            itemDto.getQuantity()
                    );


            BigDecimal metalAmount =
                    weight
                            .multiply(quantity)
                            .multiply(itemDto.getMetalRate());


            // ==============================
            // MAKING CHARGE
            // ==============================

            BigDecimal makingChargeAmount =
                    metalAmount
                            .multiply(makingPercent)
                            .divide(
                                    BigDecimal.valueOf(100)
                            );


            // ==============================
            // TAXABLE AMOUNT
            // ==============================

            BigDecimal taxableAmount =
                    metalAmount
                            .add(makingChargeAmount);


            // ==============================
            // GST
            // ==============================

            BigDecimal gstAmount =
                    taxableAmount
                            .multiply(gstPercent)
                            .divide(
                                    BigDecimal.valueOf(100)
                            );


            // ==============================
            // ITEM TOTAL
            // ==============================

            BigDecimal itemTotal =
                    taxableAmount.add(gstAmount);


            totalAmount =
                    totalAmount.add(
                            metalAmount
                                    .add(makingChargeAmount)
                    );


            totalGst =
                    totalGst.add(gstAmount);


            // ==============================
            // SAVE BILL ITEM
            // ==============================

            BillItem billItem =
                    BillItem.builder()
                            .bill(savedBill)
                            .jewelleryItem(jewelleryItem)
                            .quantity(itemDto.getQuantity())
                            .metalRate(itemDto.getMetalRate())
                            .metalAmount(metalAmount)
                            .makingChargePercent(makingPercent)
                            .makingChargeAmount(
                                    makingChargeAmount
                            )
                            .gstPercent(gstPercent)
                            .gstAmount(gstAmount)
                            .total(itemTotal)
                            .build();


            billItemRepository.save(billItem);


            // ==============================
            // REDUCE STOCK
            // ==============================

            jewelleryItem.setStockQuantity(
                    jewelleryItem.getStockQuantity()
                            - itemDto.getQuantity()
            );


            jewelleryItemRepository.save(jewelleryItem);
        }


        // ==============================
        // DISCOUNT
        // ==============================

        BigDecimal discount =
                savedBill.getDiscount() == null
                        ? BigDecimal.ZERO
                        : savedBill.getDiscount();


        // Total before GST
        savedBill.setTotalAmount(totalAmount);


        // GST
        savedBill.setGstAmount(totalGst);


        // Grand total
        BigDecimal grandTotal =
                totalAmount
                        .add(totalGst)
                        .subtract(discount);


        if (grandTotal.compareTo(BigDecimal.ZERO) < 0) {
            grandTotal = BigDecimal.ZERO;
        }


        savedBill.setGrandTotal(grandTotal);


        // ==============================
        // PAID AMOUNT
        // ==============================

        BigDecimal paidAmount =
                savedBill.getPaidAmount() == null
                        ? BigDecimal.ZERO
                        : savedBill.getPaidAmount();


        if (paidAmount.compareTo(grandTotal) > 0) {

            throw new RuntimeException(
                    "Paid amount cannot be greater than Grand Total"
            );
        }


        // ==============================
        // DUE
        // ==============================

        BigDecimal dueAmount =
                grandTotal.subtract(paidAmount);


        savedBill.setDueAmount(dueAmount);


        // ==============================
        // STATUS
        // ==============================

        if (dueAmount.compareTo(BigDecimal.ZERO) == 0) {

            savedBill.setStatus(BillStatus.PAID);

        } else if (paidAmount.compareTo(BigDecimal.ZERO) > 0) {

            savedBill.setStatus(BillStatus.PARTIAL);

        } else {

            savedBill.setStatus(BillStatus.PARTIAL);
        }


        // ==============================
        // SAVE FINAL BILL
        // ==============================

        billRepository.save(savedBill);


        // ==============================
        // INITIAL PAYMENT HISTORY
        // ==============================

        if (paidAmount.compareTo(BigDecimal.ZERO) > 0) {

            PaymentHistory paymentHistory =
                    PaymentHistory.builder()
                            .bill(savedBill)
                            .customer(customer)
                            .amount(paidAmount)
                            .paymentMode(
                                    savedBill.getPaymentMode()
                            )
                            .build();

            paymentHistoryRepository.save(
                    paymentHistory
            );
        }


        return convertToDto(savedBill);
    }


    // ==============================
    // GET BILL
    // ==============================

    @Override
    public BillDto getBillByBillNumber(
            String billNumber
    ) {

        Bill bill =
                billRepository
                        .findByBillNumber(billNumber)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Bill Not Found"
                                ));


        return convertToDto(bill);
    }


    // ==============================
    // GET ALL BILLS
    // ==============================

    @Override
    public List<BillDto> getAllBills() {

        return billRepository.findAll()
                .stream()
                .map(this::convertToDto)
                .toList();
    }


    // ==============================
    // GET CUSTOMER ALL BILLS
    // ==============================

    @Override
    public List<BillDto> getBillsByCustomerMobile(
            String mobileNumber
    ) {

        return billRepository
                .findByCustomer_MobileNumber(mobileNumber)
                .stream()
                .map(this::convertToDto)
                .toList();
    }


    // ==============================
    // PAY DUE
    // ==============================

    @Override
    @Transactional
    public BillDto payDueAmount(
            String billNumber,
            BigDecimal amount
    ) {

        Bill bill =
                billRepository
                        .findByBillNumber(billNumber)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Bill Not Found"
                                ));


        // ==============================
        // PAYMENT VALIDATION
        // ==============================

        if (amount == null ||
                amount.compareTo(BigDecimal.ZERO) <= 0) {

            throw new RuntimeException(
                    "Payment amount must be greater than zero"
            );
        }


        BigDecimal currentDue =
                bill.getDueAmount() == null
                        ? BigDecimal.ZERO
                        : bill.getDueAmount();


        if (amount.compareTo(currentDue) > 0) {

            throw new RuntimeException(
                    "Payment cannot be greater than due amount"
            );
        }


        // ==============================
        // UPDATE PAID AMOUNT
        // ==============================

        BigDecimal currentPaid =
                bill.getPaidAmount() == null
                        ? BigDecimal.ZERO
                        : bill.getPaidAmount();


        BigDecimal newPaid =
                currentPaid.add(amount);


        bill.setPaidAmount(newPaid);


        // ==============================
        // UPDATE DUE
        // ==============================

        BigDecimal due =
                bill.getGrandTotal()
                        .subtract(newPaid);


        bill.setDueAmount(due);


        // ==============================
        // UPDATE STATUS
        // ==============================

        if (due.compareTo(BigDecimal.ZERO) == 0) {

            bill.setStatus(
                    BillStatus.PAID
            );

        } else {

            bill.setStatus(
                    BillStatus.PARTIAL
            );
        }


        // ==============================
        // SAVE UPDATED BILL
        // ==============================

        Bill savedBill =
                billRepository.save(bill);


        // ==============================
        // SAVE PAYMENT HISTORY
        // ==============================

        PaymentHistory paymentHistory =
                PaymentHistory.builder()
                        .bill(savedBill)
                        .customer(
                                savedBill.getCustomer()
                        )
                        .amount(amount)
                        .paymentMode(
                                savedBill.getPaymentMode()
                        )
                        .build();


        paymentHistoryRepository.save(
                paymentHistory
        );


        return convertToDto(savedBill);
    }


    // ==============================
    // DELETE BILL
    // ==============================

    @Override
    @Transactional
    public void deleteBill(String billNumber) {

        Bill bill =
                billRepository
                        .findByBillNumber(billNumber)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Bill Not Found"
                                ));


        billRepository.delete(bill);
    }


    // ==============================
    // GENERATE BILL NUMBER
    // ==============================

    private String generateBillNumber() {

        long count =
                billRepository.count() + 1;

        return String.format(
                "BILL%05d",
                count
        );
    }


    // ==============================
    // DTO CONVERTER
    // ==============================

    private BillDto convertToDto(Bill bill) {

        // Bill ke saare items database se fetch karna
        List<BillItem> billItems =
                billItemRepository.findByBill(bill);

        List<BillItemDto> itemDtos =
                new ArrayList<>();


        for (BillItem billItem : billItems) {

            JewelleryItem item =
                    billItem.getJewelleryItem();


            BillItemDto itemDto =
                    BillItemDto.builder()

                            .itemCode(
                                    item.getItemCode()
                            )

                            .itemName(
                                    item.getItemName()
                            )

                            .quantity(
                                    billItem.getQuantity()
                            )

                            .weight(
                                    item.getWeight()
                            )

                            .metalRate(
                                    billItem.getMetalRate()
                            )

                            .makingChargePercent(
                                    billItem.getMakingChargePercent()
                            )

                            .gstPercent(
                                    billItem.getGstPercent()
                            )

                            .total(
                                    billItem.getTotal()
                            )

                            .build();


            itemDtos.add(itemDto);
        }


        // ==============================
        // BILL DTO
        // ==============================

        return BillDto.builder()

                .billNumber(
                        bill.getBillNumber()
                )

                .billDate(
                        bill.getBillDate()
                )

                .customerMobile(
                        bill.getCustomer()
                                .getMobileNumber()
                )

                .items(
                        itemDtos
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

                .totalAmount(
                        bill.getTotalAmount()
                )

                .gstAmount(
                        bill.getGstAmount()
                )

                .grandTotal(
                        bill.getGrandTotal()
                )

                .dueAmount(
                        bill.getDueAmount()
                )

                .build();
    }
}

