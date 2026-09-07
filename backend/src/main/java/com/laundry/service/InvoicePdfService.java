package com.laundry.service;

import com.laundry.model.Invoice;
import com.laundry.model.InvoiceItem;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.format.DateTimeFormatter;

@Service
public class InvoicePdfService {

    private static final float MARGIN = 45f;
    private static final float ROW_HEIGHT = 22f;

    public byte[] generateInvoicePdf(Invoice invoice) {
        try (PDDocument document = new PDDocument();
             ByteArrayOutputStream output = new ByteArrayOutputStream()) {

            PDPage page = new PDPage(PDRectangle.A4);
            document.addPage(page);

            float y = page.getMediaBox().getHeight() - MARGIN;

            try (PDPageContentStream content = new PDPageContentStream(document, page)) {
                content.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD), 20);
                write(content, MARGIN, y, "Sparkle Laundry");
                y -= 30;

                content.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD), 13);
                write(content, MARGIN, y, "Invoice #" + invoice.getId());
                y -= 22;

                content.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA), 10);
                write(content, MARGIN, y, "Date: " + invoice.getCreatedDate());
                y -= 16;
                write(content, MARGIN, y, "Customer: " + safe(invoice.getCustomerName()));
                y -= 16;
                write(content, MARGIN, y, "Phone: " + safe(invoice.getCustomerPhone()));
                y -= 28;

                // Table header
                drawLine(content, MARGIN, y, 550f, y);
                y -= 16;
                content.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD), 9);
                write(content, MARGIN, y, "Cloth");
                write(content, 180, y, "Service");
                write(content, 335, y, "Qty");
                write(content, 385, y, "Price");
                write(content, 465, y, "Amount");
                y -= 8;
                drawLine(content, MARGIN, y, 550f, y);
                y -= 18;

                content.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA), 9);
                for (InvoiceItem item : invoice.getItems()) {
                    write(content, MARGIN, y, safe(item.getClothType()));
                    write(content, 180, y, safe(item.getServiceType().name().replace("_", " ")));
                    write(content, 335, y, String.valueOf(item.getQuantity()));
                    write(content, 385, y, String.format("Rs. %.2f", item.getPricePerUnit()));
                    write(content, 465, y, String.format("Rs. %.2f", item.getQuantity() * item.getPricePerUnit()));
                    y -= ROW_HEIGHT;
                }

                y -= 8;
                drawLine(content, MARGIN, y, 550f, y);
                y -= 24;
                content.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD), 12);
                write(content, 385, y, String.format("Total: Rs. %.2f", invoice.getTotalAmount()));
                y -= 25;
                content.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA), 9);
                write(content, MARGIN, y, "Thank you for choosing Sparkle Laundry.");
            }

            document.save(output);
            return output.toByteArray();
        } catch (IOException ex) {
            throw new IllegalStateException("Could not generate invoice PDF.", ex);
        }
    }

    private void write(PDPageContentStream content, float x, float y, String text) throws IOException {
        content.beginText();
        content.newLineAtOffset(x, y);
        content.showText(sanitize(text));
        content.endText();
    }

    private void drawLine(PDPageContentStream content, float x1, float y, float x2, float y2) throws IOException {
        content.moveTo(x1, y);
        content.lineTo(x2, y2);
        content.stroke();
    }

    private String safe(String value) {
        return value == null || value.isBlank() ? "-" : value;
    }

    private String sanitize(String value) {
        // Helvetica does not support all Unicode characters; keep the generated
        // invoice portable and avoid PDF encoding errors.
        return value == null ? "" : value.replace("₹", "Rs.").replaceAll("[^\\x20-\\x7E]", "?");
    }
}
