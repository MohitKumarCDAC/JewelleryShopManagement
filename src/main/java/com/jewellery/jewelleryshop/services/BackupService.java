package com.jewellery.jewelleryshop.services;

import com.jewellery.jewelleryshop.entity.Bill;
import com.jewellery.jewelleryshop.entity.BillItem;
import com.jewellery.jewelleryshop.entity.Customer;
import com.jewellery.jewelleryshop.entity.JewelleryItem;
import com.jewellery.jewelleryshop.entity.PaymentHistory;
import com.jewellery.jewelleryshop.entity.GstInvoice;
import com.jewellery.jewelleryshop.entity.GstInvoiceItem;
import com.jewellery.jewelleryshop.entity.GstExchangeItem;
import com.jewellery.jewelleryshop.entity.GstPaymentHistory;
import com.jewellery.jewelleryshop.repository.GstInvoiceRepository;
import com.jewellery.jewelleryshop.repository.GstInvoiceItemRepository;
import com.jewellery.jewelleryshop.repository.GstExchangeItemRepository;
import com.jewellery.jewelleryshop.repository.GstPaymentHistoryRepository;

import com.jewellery.jewelleryshop.repository.BillItemRepository;
import com.jewellery.jewelleryshop.repository.BillRepository;
import com.jewellery.jewelleryshop.repository.CustomerRepositry;
import com.jewellery.jewelleryshop.repository.JewelleryItemRepository;
import com.jewellery.jewelleryshop.repository.PaymentHistoryRepository;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class BackupService {

    private final CustomerRepositry customerRepository;
    private final BillRepository billRepository;
    private final BillItemRepository billItemRepository;
    private final JewelleryItemRepository jewelleryItemRepository;
    private final PaymentHistoryRepository paymentHistoryRepository;
    private final GstInvoiceRepository gstInvoiceRepository;
    private final GstInvoiceItemRepository gstInvoiceItemRepository;
    private final GstExchangeItemRepository gstExchangeItemRepository;
    private final GstPaymentHistoryRepository gstPaymentHistoryRepository;

    private static final DateTimeFormatter DATE_TIME_FORMATTER =
            DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss");

    public BackupService(
            CustomerRepositry customerRepository,
            BillRepository billRepository,
            BillItemRepository billItemRepository,
            JewelleryItemRepository jewelleryItemRepository,
            PaymentHistoryRepository paymentHistoryRepository,
            GstInvoiceRepository gstInvoiceRepository,
            GstInvoiceItemRepository gstInvoiceItemRepository,
            GstExchangeItemRepository gstExchangeItemRepository,
            GstPaymentHistoryRepository gstPaymentHistoryRepository
    ) {
        this.customerRepository = customerRepository;
        this.billRepository = billRepository;
        this.billItemRepository = billItemRepository;
        this.jewelleryItemRepository = jewelleryItemRepository;
        this.paymentHistoryRepository = paymentHistoryRepository;
        this.gstInvoiceRepository = gstInvoiceRepository;
        this.gstInvoiceItemRepository = gstInvoiceItemRepository;
        this.gstExchangeItemRepository = gstExchangeItemRepository;
        this.gstPaymentHistoryRepository = gstPaymentHistoryRepository;
    }

    public byte[] generateBackupExcel() {

        try (Workbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {

            // =========================================================
            // STYLES
            // =========================================================

            CellStyle headerStyle = createHeaderStyle(workbook);

            CellStyle titleStyle = createTitleStyle(workbook);

            CellStyle dateStyle = createDateStyle(workbook);

            // =========================================================
            // 1. CUSTOMERS
            // =========================================================

            createCustomersSheet(
                    workbook,
                    headerStyle,
                    titleStyle,
                    dateStyle
            );

            // =========================================================
            // 2. BILLS
            // =========================================================

            createBillsSheet(
                    workbook,
                    headerStyle,
                    titleStyle,
                    dateStyle
            );

            // =========================================================
            // 3. BILL ITEMS
            // =========================================================

            createBillItemsSheet(
                    workbook,
                    headerStyle,
                    titleStyle
            );

            // =========================================================
            // 4. STOCK
            // =========================================================

            createStockSheet(
                    workbook,
                    headerStyle,
                    titleStyle,
                    dateStyle
            );

            // =========================================================
            // 5. PAYMENTS
            // =========================================================

            createPaymentsSheet(
                    workbook,
                    headerStyle,
                    titleStyle,
                    dateStyle
            );

            // =========================================================
            // 6. OUTSTANDING
            // =========================================================

            createOutstandingSheet(
                    workbook,
                    headerStyle,
                    titleStyle,
                    dateStyle
            );

            // =========================================================
            // 7. GST INVOICES
            // =========================================================

            createGstInvoicesSheet(
                    workbook,
                    headerStyle,
                    titleStyle,
                    dateStyle
            );

            // =========================================================
            // 8. GST INVOICE ITEMS
            // =========================================================

            createGstInvoiceItemsSheet(
                    workbook,
                    headerStyle,
                    titleStyle
            );

            // =========================================================
            // 9. GST EXCHANGE ITEMS
            // =========================================================

            createGstExchangeItemsSheet(
                    workbook,
                    headerStyle,
                    titleStyle
            );

            // =========================================================
            // 10. GST PAYMENTS
            // =========================================================

            createGstPaymentsSheet(
                    workbook,
                    headerStyle,
                    titleStyle,
                    dateStyle
            );

            workbook.write(outputStream);

            return outputStream.toByteArray();

        } catch (Exception e) {

            throw new RuntimeException(
                    "Failed to generate backup Excel file",
                    e
            );
        }
    }

    // =============================================================
    // CUSTOMERS SHEET
    // =============================================================

    private void createCustomersSheet(
            Workbook workbook,
            CellStyle headerStyle,
            CellStyle titleStyle,
            CellStyle dateStyle
    ) {

        Sheet sheet = workbook.createSheet("Customers");

        Row titleRow = sheet.createRow(0);
        Cell titleCell = titleRow.createCell(0);
        titleCell.setCellValue("Mohit Jewellers - Customers Backup");
        titleCell.setCellStyle(titleStyle);

        Row headerRow = sheet.createRow(2);

        String[] headers = {
                "Customer ID",
                "Customer Name",
                "Mobile Number",
                "Place",
                "Created Date"
        };

        createHeaderRow(headerRow, headers, headerStyle);

        List<Customer> customers = customerRepository.findAll();

        int rowIndex = 3;

        for (Customer customer : customers) {

            Row row = sheet.createRow(rowIndex++);

            row.createCell(0).setCellValue(customer.getId());
            row.createCell(1).setCellValue(safe(customer.getCustomerName()));
            row.createCell(2).setCellValue(safe(customer.getMobileNumber()));
            row.createCell(3).setCellValue(safe(customer.getPlace()));

            Cell dateCell = row.createCell(4);

            if (customer.getCreatedDate() != null) {
                dateCell.setCellValue(
                        formatDate(customer.getCreatedDate())
                );
            } else {
                dateCell.setCellValue("");
            }

            dateCell.setCellStyle(dateStyle);
        }

        autoSizeColumns(sheet, headers.length);
    }

    // =============================================================
    // BILLS SHEET
    // =============================================================

    private void createBillsSheet(
            Workbook workbook,
            CellStyle headerStyle,
            CellStyle titleStyle,
            CellStyle dateStyle
    ) {

        Sheet sheet = workbook.createSheet("Bills");

        Row titleRow = sheet.createRow(0);
        Cell titleCell = titleRow.createCell(0);

        titleCell.setCellValue("Mohit Jewellers - Bills Backup");
        titleCell.setCellStyle(titleStyle);

        Row headerRow = sheet.createRow(2);

        String[] headers = {

                "Bill ID",
                "Bill Number",
                "Customer Name",
                "Customer Mobile",

                "Bill Date",

                "Total Amount",
                "Discount",
                "GST Amount",
                "Grand Total",

                "Paid Amount",
                "Due Amount",

                "Gold Exchange Weight",
                "Gold Exchange Rate",
                "Gold Exchange Amount",

                "Silver Exchange Weight",
                "Silver Exchange Rate",
                "Silver Exchange Amount",

                "Total Exchange Amount",

                "Status",
                "Payment Mode"
        };

        createHeaderRow(headerRow, headers, headerStyle);

        List<Bill> bills = billRepository.findAll();

        int rowIndex = 3;

        for (Bill bill : bills) {

            Row row = sheet.createRow(rowIndex++);

            row.createCell(0).setCellValue(
                    bill.getId() != null ? bill.getId() : 0
            );

            row.createCell(1).setCellValue(
                    safe(bill.getBillNumber())
            );

            if (bill.getCustomer() != null) {

                row.createCell(2).setCellValue(
                        safe(bill.getCustomer().getCustomerName())
                );

                row.createCell(3).setCellValue(
                        safe(bill.getCustomer().getMobileNumber())
                );

            } else {

                row.createCell(2).setCellValue("");
                row.createCell(3).setCellValue("");
            }

            Cell billDateCell = row.createCell(4);

            if (bill.getBillDate() != null) {

                billDateCell.setCellValue(
                        formatDate(bill.getBillDate())
                );

            } else {

                billDateCell.setCellValue("");
            }

            billDateCell.setCellStyle(dateStyle);

            row.createCell(5).setCellValue(
                    decimal(bill.getTotalAmount())
            );

            row.createCell(6).setCellValue(
                    decimal(bill.getDiscount())
            );

            row.createCell(7).setCellValue(
                    decimal(bill.getGstAmount())
            );

            row.createCell(8).setCellValue(
                    decimal(bill.getGrandTotal())
            );

            row.createCell(9).setCellValue(
                    decimal(bill.getPaidAmount())
            );

            row.createCell(10).setCellValue(
                    decimal(bill.getDueAmount())
            );

            row.createCell(11).setCellValue(
                    decimal(bill.getGoldExchangeWeight())
            );

            row.createCell(12).setCellValue(
                    decimal(bill.getGoldExchangeRate())
            );

            row.createCell(13).setCellValue(
                    decimal(bill.getGoldExchangeAmount())
            );

            row.createCell(14).setCellValue(
                    decimal(bill.getSilverExchangeWeight())
            );

            row.createCell(15).setCellValue(
                    decimal(bill.getSilverExchangeRate())
            );

            row.createCell(16).setCellValue(
                    decimal(bill.getSilverExchangeAmount())
            );

            row.createCell(17).setCellValue(
                    decimal(bill.getTotalExchangeAmount())
            );

            row.createCell(18).setCellValue(
                    bill.getStatus() != null
                            ? bill.getStatus().toString()
                            : ""
            );

            row.createCell(19).setCellValue(
                    bill.getPaymentMode() != null
                            ? bill.getPaymentMode().toString()
                            : ""
            );
        }

        autoSizeColumns(sheet, headers.length);
    }

    // =============================================================
    // BILL ITEMS SHEET
    // =============================================================

    private void createBillItemsSheet(
            Workbook workbook,
            CellStyle headerStyle,
            CellStyle titleStyle
    ) {

        Sheet sheet = workbook.createSheet("Bill Items");

        Row titleRow = sheet.createRow(0);

        Cell titleCell = titleRow.createCell(0);

        titleCell.setCellValue(
                "Mohit Jewellers - Bill Items Backup"
        );

        titleCell.setCellStyle(titleStyle);

        Row headerRow = sheet.createRow(2);

        String[] headers = {

                "Bill Number",
                "Item ID",
                "Stock Item Code",

                "Item Name",

                "Weight (gm)",
                "Quantity",

                "Metal Rate",
                "Metal Amount",

                "Making Charge %",
                "Making Charge Type",
                "Making Charge Value",
                "Making Charge Amount",

                "GST %",
                "GST Amount",

                "Item Total"
        };

        createHeaderRow(headerRow, headers, headerStyle);

        List<BillItem> billItems =
                billItemRepository.findAll();

        int rowIndex = 3;

        for (BillItem item : billItems) {

            Row row = sheet.createRow(rowIndex++);

            // Bill number
            if (item.getBill() != null) {

                row.createCell(0).setCellValue(
                        safe(item.getBill().getBillNumber())
                );

            } else {

                row.createCell(0).setCellValue("");
            }

            // Bill Item ID
            row.createCell(1).setCellValue(
                    item.getId() != null ? item.getId() : 0
            );

            // Stock item code
            if (item.getJewelleryItem() != null) {

                row.createCell(2).setCellValue(
                        safe(item.getJewelleryItem().getItemCode())
                );

            } else {

                row.createCell(2).setCellValue("");
            }

            // Item name
            String itemName = item.getItemName();

            if ((itemName == null || itemName.isBlank())
                    && item.getJewelleryItem() != null) {

                itemName =
                        item.getJewelleryItem().getItemName();
            }

            row.createCell(3).setCellValue(
                    safe(itemName)
            );

            row.createCell(4).setCellValue(
                    decimal(item.getWeight())
            );

            row.createCell(5).setCellValue(
                    item.getQuantity() != null
                            ? item.getQuantity()
                            : 0
            );

            row.createCell(6).setCellValue(
                    decimal(item.getMetalRate())
            );

            row.createCell(7).setCellValue(
                    decimal(item.getMetalAmount())
            );

            row.createCell(8).setCellValue(
                    decimal(item.getMakingChargePercent())
            );

            row.createCell(9).setCellValue(
                    safe(item.getMakingChargeType())
            );

            row.createCell(10).setCellValue(
                    decimal(item.getMakingChargeValue())
            );

            row.createCell(11).setCellValue(
                    decimal(item.getMakingChargeAmount())
            );

            row.createCell(12).setCellValue(
                    decimal(item.getGstPercent())
            );

            row.createCell(13).setCellValue(
                    decimal(item.getGstAmount())
            );

            row.createCell(14).setCellValue(
                    decimal(item.getTotal())
            );
        }

        autoSizeColumns(sheet, headers.length);
    }

    // =============================================================
    // STOCK SHEET
    // =============================================================

    private void createStockSheet(
            Workbook workbook,
            CellStyle headerStyle,
            CellStyle titleStyle,
            CellStyle dateStyle
    ) {

        Sheet sheet = workbook.createSheet("Stock");

        Row titleRow = sheet.createRow(0);

        Cell titleCell = titleRow.createCell(0);

        titleCell.setCellValue(
                "Mohit Jewellers - Stock Backup"
        );

        titleCell.setCellStyle(titleStyle);

        Row headerRow = sheet.createRow(2);

        String[] headers = {

                "Stock ID",
                "Item Code",
                "Item Name",
                "Category",
                "Purity",
                "Weight (gm)",
                "Stock Quantity",
                "Created Date"
        };

        createHeaderRow(headerRow, headers, headerStyle);

        List<JewelleryItem> items =
                jewelleryItemRepository.findAll();

        int rowIndex = 3;

        for (JewelleryItem item : items) {

            Row row = sheet.createRow(rowIndex++);

            row.createCell(0).setCellValue(
                    item.getId() != null ? item.getId() : 0
            );

            row.createCell(1).setCellValue(
                    safe(item.getItemCode())
            );

            row.createCell(2).setCellValue(
                    safe(item.getItemName())
            );

            row.createCell(3).setCellValue(
                    item.getCategory() != null
                            ? item.getCategory().toString()
                            : ""
            );

            row.createCell(4).setCellValue(
                    item.getPurity() != null
                            ? item.getPurity().toString()
                            : ""
            );

            row.createCell(5).setCellValue(
                    decimal(item.getWeight())
            );

            row.createCell(6).setCellValue(
                    item.getStockQuantity() != null
                            ? item.getStockQuantity()
                            : 0
            );

            Cell dateCell = row.createCell(7);

            if (item.getCreatedDate() != null) {

                dateCell.setCellValue(
                        formatDate(item.getCreatedDate())
                );

            } else {

                dateCell.setCellValue("");
            }

            dateCell.setCellStyle(dateStyle);
        }

        autoSizeColumns(sheet, headers.length);
    }

    // =============================================================
    // PAYMENTS SHEET
    // =============================================================

    private void createPaymentsSheet(
            Workbook workbook,
            CellStyle headerStyle,
            CellStyle titleStyle,
            CellStyle dateStyle
    ) {

        Sheet sheet = workbook.createSheet("Payments");

        Row titleRow = sheet.createRow(0);

        Cell titleCell = titleRow.createCell(0);

        titleCell.setCellValue(
                "Mohit Jewellers - Payment History Backup"
        );

        titleCell.setCellStyle(titleStyle);

        Row headerRow = sheet.createRow(2);

        String[] headers = {

                "Payment ID",
                "Bill Number",

                "Customer Name",
                "Customer Mobile",

                "Amount",
                "Payment Date",
                "Payment Mode"
        };

        createHeaderRow(headerRow, headers, headerStyle);

        List<PaymentHistory> payments =
                paymentHistoryRepository.findAll();

        int rowIndex = 3;

        for (PaymentHistory payment : payments) {

            Row row = sheet.createRow(rowIndex++);

            row.createCell(0).setCellValue(
                    payment.getId() != null
                            ? payment.getId()
                            : 0
            );

            if (payment.getBill() != null) {

                row.createCell(1).setCellValue(
                        safe(payment.getBill().getBillNumber())
                );

            } else {

                row.createCell(1).setCellValue("");
            }

            if (payment.getCustomer() != null) {

                row.createCell(2).setCellValue(
                        safe(payment.getCustomer().getCustomerName())
                );

                row.createCell(3).setCellValue(
                        safe(payment.getCustomer().getMobileNumber())
                );

            } else {

                row.createCell(2).setCellValue("");
                row.createCell(3).setCellValue("");
            }

            row.createCell(4).setCellValue(
                    decimal(payment.getAmount())
            );

            Cell dateCell = row.createCell(5);

            if (payment.getPaymentDate() != null) {

                dateCell.setCellValue(
                        formatDate(payment.getPaymentDate())
                );

            } else {

                dateCell.setCellValue("");
            }

            dateCell.setCellStyle(dateStyle);

            row.createCell(6).setCellValue(
                    payment.getPaymentMode() != null
                            ? payment.getPaymentMode().toString()
                            : ""
            );
        }

        autoSizeColumns(sheet, headers.length);
    }

    // =============================================================
    // OUTSTANDING SHEET
    // =============================================================

    private void createOutstandingSheet(
            Workbook workbook,
            CellStyle headerStyle,
            CellStyle titleStyle,
            CellStyle dateStyle
    ) {

        Sheet sheet = workbook.createSheet("Outstanding");

        Row titleRow = sheet.createRow(0);

        Cell titleCell = titleRow.createCell(0);

        titleCell.setCellValue(
                "Mohit Jewellers - Outstanding Backup"
        );

        titleCell.setCellStyle(titleStyle);

        Row headerRow = sheet.createRow(2);

        String[] headers = {

                "Bill Number",
                "Bill Date",

                "Customer Name",
                "Customer Mobile",
                "Customer Place",

                "Grand Total",
                "Paid Amount",
                "Due Amount",

                "Payment Mode",
                "Status"
        };

        createHeaderRow(headerRow, headers, headerStyle);

        List<Bill> outstandingBills =
                billRepository.findByDueAmountGreaterThan(
                        BigDecimal.ZERO
                );

        int rowIndex = 3;

        for (Bill bill : outstandingBills) {

            Row row = sheet.createRow(rowIndex++);

            row.createCell(0).setCellValue(
                    safe(bill.getBillNumber())
            );

            Cell billDateCell = row.createCell(1);

            if (bill.getBillDate() != null) {

                billDateCell.setCellValue(
                        formatDate(bill.getBillDate())
                );

            } else {

                billDateCell.setCellValue("");
            }

            billDateCell.setCellStyle(dateStyle);

            if (bill.getCustomer() != null) {

                row.createCell(2).setCellValue(
                        safe(
                                bill.getCustomer()
                                        .getCustomerName()
                        )
                );

                row.createCell(3).setCellValue(
                        safe(
                                bill.getCustomer()
                                        .getMobileNumber()
                        )
                );

                row.createCell(4).setCellValue(
                        safe(
                                bill.getCustomer()
                                        .getPlace()
                        )
                );

            } else {

                row.createCell(2).setCellValue("");
                row.createCell(3).setCellValue("");
                row.createCell(4).setCellValue("");
            }

            row.createCell(5).setCellValue(
                    decimal(bill.getGrandTotal())
            );

            row.createCell(6).setCellValue(
                    decimal(bill.getPaidAmount())
            );

            row.createCell(7).setCellValue(
                    decimal(bill.getDueAmount())
            );

            row.createCell(8).setCellValue(
                    bill.getPaymentMode() != null
                            ? bill.getPaymentMode().toString()
                            : ""
            );

            row.createCell(9).setCellValue(
                    bill.getStatus() != null
                            ? bill.getStatus().toString()
                            : ""
            );
        }

        autoSizeColumns(sheet, headers.length);
    }



    // =============================================================
    // GST INVOICES SHEET
    // =============================================================

    private void createGstInvoicesSheet(
            Workbook workbook,
            CellStyle headerStyle,
            CellStyle titleStyle,
            CellStyle dateStyle
    ) {

        Sheet sheet = workbook.createSheet("GST Invoices");

        Row titleRow = sheet.createRow(0);
        Cell titleCell = titleRow.createCell(0);
        titleCell.setCellValue(
                "Mohit Jewellers - GST Invoices Backup"
        );
        titleCell.setCellStyle(titleStyle);

        Row headerRow = sheet.createRow(2);

        String[] headers = {
                "Invoice ID",
                "Invoice Number",
                "Financial Year",
                "Invoice Date Time",
                "Place Of Supply",
                "Place Of Supply State Code",
                "Seller GSTIN",
                "Customer Name",
                "Customer Mobile",
                "Customer Address",
                "Customer GSTIN",
                "Taxable Amount",
                "CGST Amount",
                "SGST Amount",
                "IGST Amount",
                "Total GST Amount",
                "Discount Amount",
                "Round Off",
                "Grand Total",
                "Total Exchange Amount",
                "Cash Amount",
                "UPI Amount",
                "Card Amount",
                "Other Amount",
                "Paid Amount",
                "Due Amount",
                "Payment Status",
                "Reverse Charge"
        };

        createHeaderRow(headerRow, headers, headerStyle);

        List<GstInvoice> invoices =
                gstInvoiceRepository.findAll();

        int rowIndex = 3;

        for (GstInvoice invoice : invoices) {

            Row row = sheet.createRow(rowIndex++);

            row.createCell(0).setCellValue(
                    invoice.getId() != null ? invoice.getId() : 0
            );
            row.createCell(1).setCellValue(
                    safe(invoice.getInvoiceNumber())
            );
            row.createCell(2).setCellValue(
                    safe(invoice.getFinancialYear())
            );

            Cell dateCell = row.createCell(3);
            dateCell.setCellValue(
                    formatDate(invoice.getInvoiceDateTime())
            );
            dateCell.setCellStyle(dateStyle);

            row.createCell(4).setCellValue(
                    safe(invoice.getPlaceOfSupply())
            );
            row.createCell(5).setCellValue(
                    safe(invoice.getPlaceOfSupplyStateCode())
            );
            row.createCell(6).setCellValue(
                    safe(invoice.getSellerGstin())
            );
            row.createCell(7).setCellValue(
                    safe(invoice.getCustomerName())
            );
            row.createCell(8).setCellValue(
                    safe(invoice.getCustomerMobile())
            );
            row.createCell(9).setCellValue(
                    safe(invoice.getCustomerAddress())
            );
            row.createCell(10).setCellValue(
                    safe(invoice.getCustomerGstin())
            );
            row.createCell(11).setCellValue(decimal(invoice.getTaxableAmount()));
            row.createCell(12).setCellValue(decimal(invoice.getCgstAmount()));
            row.createCell(13).setCellValue(decimal(invoice.getSgstAmount()));
            row.createCell(14).setCellValue(decimal(invoice.getIgstAmount()));
            row.createCell(15).setCellValue(decimal(invoice.getTotalGstAmount()));
            row.createCell(16).setCellValue(decimal(invoice.getDiscountAmount()));
            row.createCell(17).setCellValue(decimal(invoice.getRoundOff()));
            row.createCell(18).setCellValue(decimal(invoice.getGrandTotal()));
            row.createCell(19).setCellValue(decimal(invoice.getTotalExchangeAmount()));
            row.createCell(20).setCellValue(decimal(invoice.getCashAmount()));
            row.createCell(21).setCellValue(decimal(invoice.getUpiAmount()));
            row.createCell(22).setCellValue(decimal(invoice.getCardAmount()));
            row.createCell(23).setCellValue(decimal(invoice.getOtherAmount()));
            row.createCell(24).setCellValue(decimal(invoice.getPaidAmount()));
            row.createCell(25).setCellValue(decimal(invoice.getDueAmount()));
            row.createCell(26).setCellValue(safe(invoice.getPaymentStatus()));
            row.createCell(27).setCellValue(
                    Boolean.TRUE.equals(invoice.getReverseCharge()) ? "TRUE" : "FALSE"
            );
        }

        autoSizeColumns(sheet, headers.length);
    }


    // =============================================================
    // GST INVOICE ITEMS SHEET
    // =============================================================

    private void createGstInvoiceItemsSheet(
            Workbook workbook,
            CellStyle headerStyle,
            CellStyle titleStyle
    ) {

        Sheet sheet = workbook.createSheet("GST Invoice Items");

        Row titleRow = sheet.createRow(0);
        Cell titleCell = titleRow.createCell(0);
        titleCell.setCellValue(
                "Mohit Jewellers - GST Invoice Items Backup"
        );
        titleCell.setCellStyle(titleStyle);

        Row headerRow = sheet.createRow(2);

        String[] headers = {
                "Invoice Number",
                "Item ID",
                "Item Code",
                "Item Name",
                "HSN Code",
                "Metal Type",
                "Purity",
                "Quantity",
                "Weight",
                "Metal Rate",
                "Metal Amount",
                "Making Charge Type",
                "Making Charge Value",
                "Making Charge Amount",
                "Discount Amount",
                "Taxable Amount",
                "GST %",
                "CGST Amount",
                "SGST Amount",
                "IGST Amount",
                "Total Amount"
        };

        createHeaderRow(headerRow, headers, headerStyle);

        List<GstInvoiceItem> items =
                gstInvoiceItemRepository.findAll();

        int rowIndex = 3;

        for (GstInvoiceItem item : items) {

            Row row = sheet.createRow(rowIndex++);

            row.createCell(0).setCellValue(
                    item.getGstInvoice() != null
                            ? safe(item.getGstInvoice().getInvoiceNumber())
                            : ""
            );
            row.createCell(1).setCellValue(
                    item.getId() != null ? item.getId() : 0
            );
            row.createCell(2).setCellValue(safe(item.getItemCode()));
            row.createCell(3).setCellValue(safe(item.getItemName()));
            row.createCell(4).setCellValue(safe(item.getHsnCode()));
            row.createCell(5).setCellValue(safe(item.getMetalType()));
            row.createCell(6).setCellValue(safe(item.getPurity()));
            row.createCell(7).setCellValue(decimal(item.getQuantity()));
            row.createCell(8).setCellValue(decimal(item.getWeight()));
            row.createCell(9).setCellValue(decimal(item.getMetalRate()));
            row.createCell(10).setCellValue(decimal(item.getMetalAmount()));
            row.createCell(11).setCellValue(safe(item.getMakingChargeType()));
            row.createCell(12).setCellValue(decimal(item.getMakingChargeValue()));
            row.createCell(13).setCellValue(decimal(item.getMakingChargeAmount()));
            row.createCell(14).setCellValue(decimal(item.getDiscountAmount()));
            row.createCell(15).setCellValue(decimal(item.getTaxableAmount()));
            row.createCell(16).setCellValue(decimal(item.getGstPercent()));
            row.createCell(17).setCellValue(decimal(item.getCgstAmount()));
            row.createCell(18).setCellValue(decimal(item.getSgstAmount()));
            row.createCell(19).setCellValue(decimal(item.getIgstAmount()));
            row.createCell(20).setCellValue(decimal(item.getTotalAmount()));
        }

        autoSizeColumns(sheet, headers.length);
    }


    // =============================================================
    // GST EXCHANGE ITEMS SHEET
    // =============================================================

    private void createGstExchangeItemsSheet(
            Workbook workbook,
            CellStyle headerStyle,
            CellStyle titleStyle
    ) {

        Sheet sheet = workbook.createSheet("GST Exchange Items");

        Row titleRow = sheet.createRow(0);
        Cell titleCell = titleRow.createCell(0);
        titleCell.setCellValue(
                "Mohit Jewellers - GST Exchange Items Backup"
        );
        titleCell.setCellStyle(titleStyle);

        Row headerRow = sheet.createRow(2);

        String[] headers = {
                "Invoice Number",
                "Exchange Item ID",
                "Metal Type",
                "Weight",
                "Rate",
                "Amount"
        };

        createHeaderRow(headerRow, headers, headerStyle);

        List<GstExchangeItem> items =
                gstExchangeItemRepository.findAll();

        int rowIndex = 3;

        for (GstExchangeItem item : items) {

            Row row = sheet.createRow(rowIndex++);

            row.createCell(0).setCellValue(
                    item.getGstInvoice() != null
                            ? safe(item.getGstInvoice().getInvoiceNumber())
                            : ""
            );
            row.createCell(1).setCellValue(
                    item.getId() != null ? item.getId() : 0
            );
            row.createCell(2).setCellValue(safe(item.getMetalType()));
            row.createCell(3).setCellValue(decimal(item.getWeight()));
            row.createCell(4).setCellValue(decimal(item.getRate()));
            row.createCell(5).setCellValue(decimal(item.getAmount()));
        }

        autoSizeColumns(sheet, headers.length);
    }


    // =============================================================
    // GST PAYMENTS SHEET
    // =============================================================

    private void createGstPaymentsSheet(
            Workbook workbook,
            CellStyle headerStyle,
            CellStyle titleStyle,
            CellStyle dateStyle
    ) {

        Sheet sheet = workbook.createSheet("GST Payments");

        Row titleRow = sheet.createRow(0);
        Cell titleCell = titleRow.createCell(0);
        titleCell.setCellValue(
                "Mohit Jewellers - GST Payment History Backup"
        );
        titleCell.setCellStyle(titleStyle);

        Row headerRow = sheet.createRow(2);

        String[] headers = {
                "Payment ID",
                "Invoice Number",
                "Customer Name",
                "Customer Mobile",
                "Amount",
                "Payment Date",
                "Payment Mode"
        };

        createHeaderRow(headerRow, headers, headerStyle);

        List<GstPaymentHistory> payments =
                gstPaymentHistoryRepository.findAll();

        int rowIndex = 3;

        for (GstPaymentHistory payment : payments) {

            Row row = sheet.createRow(rowIndex++);

            row.createCell(0).setCellValue(
                    payment.getId() != null ? payment.getId() : 0
            );
            row.createCell(1).setCellValue(
                    safe(payment.getInvoiceNumber())
            );
            row.createCell(2).setCellValue(
                    safe(payment.getCustomerName())
            );
            row.createCell(3).setCellValue(
                    safe(payment.getCustomerMobile())
            );
            row.createCell(4).setCellValue(
                    decimal(payment.getAmount())
            );

            Cell dateCell = row.createCell(5);
            dateCell.setCellValue(
                    formatDate(payment.getPaymentDate())
            );
            dateCell.setCellStyle(dateStyle);

            row.createCell(6).setCellValue(
                    safe(payment.getPaymentMode())
            );
        }

        autoSizeColumns(sheet, headers.length);
    }

    // =============================================================
    // COMMON METHODS
    // =============================================================

    private void createHeaderRow(
            Row row,
            String[] headers,
            CellStyle headerStyle
    ) {

        for (int i = 0; i < headers.length; i++) {

            Cell cell = row.createCell(i);

            cell.setCellValue(headers[i]);

            cell.setCellStyle(headerStyle);
        }
    }

    private CellStyle createHeaderStyle(
            Workbook workbook
    ) {

        CellStyle style = workbook.createCellStyle();

        Font font = workbook.createFont();

        font.setBold(true);
        font.setColor(IndexedColors.WHITE.getIndex());

        style.setFont(font);

        style.setFillForegroundColor(
                IndexedColors.DARK_BLUE.getIndex()
        );

        style.setFillPattern(
                FillPatternType.SOLID_FOREGROUND
        );

        style.setAlignment(
                HorizontalAlignment.CENTER
        );

        style.setVerticalAlignment(
                VerticalAlignment.CENTER
        );

        style.setBorderBottom(
                BorderStyle.THIN
        );

        style.setBorderTop(
                BorderStyle.THIN
        );

        style.setBorderLeft(
                BorderStyle.THIN
        );

        style.setBorderRight(
                BorderStyle.THIN
        );

        return style;
    }

    private CellStyle createTitleStyle(
            Workbook workbook
    ) {

        CellStyle style = workbook.createCellStyle();

        Font font = workbook.createFont();

        font.setBold(true);
        font.setFontHeightInPoints((short) 14);

        style.setFont(font);

        return style;
    }

    private CellStyle createDateStyle(
            Workbook workbook
    ) {

        CellStyle style = workbook.createCellStyle();

        style.setAlignment(
                HorizontalAlignment.LEFT
        );

        return style;
    }

    private void autoSizeColumns(
            Sheet sheet,
            int columnCount
    ) {

        for (int i = 0; i < columnCount; i++) {

            sheet.autoSizeColumn(i);

            // Prevent extremely wide columns
            int currentWidth = sheet.getColumnWidth(i);

            int maxWidth = 256 * 35;

            if (currentWidth > maxWidth) {
                sheet.setColumnWidth(i, maxWidth);
            }
        }

        sheet.createFreezePane(0, 3);
    }

    private String safe(String value) {

        if (value == null) {
            return "";
        }

        return value;
    }

    private double decimal(BigDecimal value) {

        if (value == null) {
            return 0.0;
        }

        return value.doubleValue();
    }

    private String formatDate(LocalDateTime dateTime) {

        if (dateTime == null) {
            return "";
        }

        return dateTime.format(DATE_TIME_FORMATTER);
    }
}