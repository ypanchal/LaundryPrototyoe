package com.laundry.service;

import com.laundry.model.Invoice;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;

import java.util.Map;

@Service
public class WhatsAppService {

    private final RestClient restClient;
    private final InvoicePdfService invoicePdfService;

    @Value("${whatsapp.enabled:false}")
    private boolean enabled;

    @Value("${whatsapp.access-token:}")
    private String accessToken;

    @Value("${whatsapp.phone-number-id:}")
    private String phoneNumberId;

    @Value("${whatsapp.graph-api-version:v23.0}")
    private String graphApiVersion;

    public WhatsAppService(InvoicePdfService invoicePdfService) {
        this.invoicePdfService = invoicePdfService;
        this.restClient = RestClient.builder().build();
    }

    public SendResult sendInvoice(Invoice invoice) {
        if (!enabled) {
            return new SendResult(false, "WhatsApp sending is disabled. Set WHATSAPP_ENABLED=true.");
        }

        if (isBlank(accessToken) || isBlank(phoneNumberId)) {
            return new SendResult(false, "WhatsApp is enabled but access token/phone number ID is not configured.");
        }

        try {
            byte[] pdf = invoicePdfService.generateInvoicePdf(invoice);
            String mediaId = uploadPdf(pdf, "invoice-" + invoice.getId() + ".pdf");
            sendDocument(invoice.getCustomerPhone(), mediaId, invoice.getId());
            return new SendResult(true, "Invoice PDF sent on WhatsApp.");
        } catch (Exception ex) {
            // Invoice creation must not be rolled back because WhatsApp is temporarily
            // unavailable. The API returns the failure message so the admin can retry.
            String detail = ex.getMessage() == null ? ex.getClass().getSimpleName() : ex.getMessage();
            return new SendResult(false, "Invoice was saved, but WhatsApp sending failed: " + detail);
        }
    }

    private String uploadPdf(byte[] pdf, String filename) {
        String url = graphUrl("/media");

        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("messaging_product", "whatsapp");
        body.add("type", "application/pdf");
        body.add("file", new ByteArrayResource(pdf) {
            @Override
            public String getFilename() {
                return filename;
            }
        });

        Map<?, ?> response = restClient.post()
                .uri(url)
                .header("Authorization", "Bearer " + accessToken)
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .body(body)
                .retrieve()
                .body(Map.class);

        if (response == null || response.get("id") == null) {
            throw new IllegalStateException("WhatsApp media upload returned no media ID.");
        }
        return response.get("id").toString();
    }

    private void sendDocument(String customerPhone, String mediaId, Long invoiceId) {
        String to = normalizePhone(customerPhone);

        Map<String, Object> document = Map.of(
                "id", mediaId,
                "filename", "invoice-" + invoiceId + ".pdf",
                "caption", "Sparkle Laundry invoice #" + invoiceId
        );

        Map<String, Object> payload = Map.of(
                "messaging_product", "whatsapp",
                "to", to,
                "type", "document",
                "document", document
        );

        restClient.post()
                .uri(graphUrl("/messages"))
                .header("Authorization", "Bearer " + accessToken)
                .contentType(MediaType.APPLICATION_JSON)
                .body(payload)
                .retrieve()
                .toBodilessEntity();
    }

    private String graphUrl(String path) {
        return "https://graph.facebook.com/" + graphApiVersion
                + "/" + phoneNumberId + path;
    }

    private String normalizePhone(String phone) {
        String digits = phone == null ? "" : phone.replaceAll("\\D", "");
        if (digits.length() == 10) {
            return "91" + digits;
        }
        if (digits.startsWith("91") && digits.length() == 12) {
            return digits;
        }
        if (digits.startsWith("+")) {
            return digits.substring(1);
        }
        throw new IllegalArgumentException("Customer phone must contain a valid WhatsApp number.");
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    public record SendResult(boolean sent, String message) {}
}
