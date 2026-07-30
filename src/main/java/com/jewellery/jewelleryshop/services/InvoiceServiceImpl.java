package com.jewellery.jewelleryshop.services;



import com.itextpdf.text.Document;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.PdfWriter;
import com.jewellery.jewelleryshop.entity.Bill;
import com.jewellery.jewelleryshop.repository.BillRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;


import java.io.ByteArrayOutputStream;

@Service
@RequiredArgsConstructor
public class InvoiceServiceImpl implements InvoiceService {

    private final BillRepository billRepository;

    @Override
    public byte[] generateInvoice(String billNumber)
    {
        Bill bill=billRepository.findByBillNumber(billNumber)
                .orElseThrow(()->new RuntimeException("Bill Not Found"));

        try
        {
            ByteArrayOutputStream out=new ByteArrayOutputStream();

            Document document=new Document();
            PdfWriter.getInstance(document,out);

            document.open();
            document.add(new Paragraph("Mohit Jewellers"));
            document.add(new Paragraph("----------------------------------"));

            document.add(new Paragraph("Bill Number: "+bill.getBillNumber()));
            document.add(new Paragraph("Bill Date: "+bill.getBillDate()));
            document.add(new Paragraph("Customer Name: "+bill.getCustomer().getCustomerName()));
            document.add(new Paragraph("Mobile: "+bill.getCustomer().getMobileNumber()));
            document.add(new Paragraph("----------------------------------"));

            document.add(new Paragraph("Grand Total: "+bill.getGrandTotal()));
            document.add(new Paragraph("Paid Amount: "+bill.getDueAmount()));
            document.add(new Paragraph("Payment Mode: "+bill.getPaymentMode()));
            document.add(new Paragraph("------------------------------------"));

            document.add(new Paragraph("Thank You ! Visit Again :-"));

            document.close();

            return out.toByteArray();






        } catch (Exception e) {
            throw new RuntimeException("Error generating invoice",e);
        }

    }
}
