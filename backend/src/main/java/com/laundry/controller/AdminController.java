package com.laundry.controller;

import org.springframework.transaction.annotation.Transactional;
import com.laundry.dto.InvoiceCreatedResponse;
import com.laundry.dto.InvoiceRequest;
import com.laundry.model.*;
import com.laundry.repository.InvoiceRepository;
import com.laundry.repository.UserRepository;
import com.laundry.service.WhatsAppService;
import com.laundry.service.InvoicePdfService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.time.ZonedDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final InvoiceRepository invoiceRepository;
    private final UserRepository userRepository;
    private final WhatsAppService whatsAppService;
    private final InvoicePdfService invoicePdfService;

    public AdminController(InvoiceRepository invoiceRepository,
                           UserRepository userRepository,
                           WhatsAppService whatsAppService,
                           InvoicePdfService invoicePdfService) {
        this.invoiceRepository = invoiceRepository;
        this.userRepository = userRepository;
        this.whatsAppService = whatsAppService;
        this.invoicePdfService = invoicePdfService;
    }

    @PostMapping("/invoices")
    @Transactional
    public ResponseEntity<?> createInvoice(@Valid @RequestBody InvoiceRequest request) {
        String phone = request.getCustomerPhone().trim();
        String tempPassword = null;

        if (!userRepository.existsByPhone(phone)) {
            String suffix = phone.length() >= 4 ? phone.substring(phone.length() - 4) : phone;
            tempPassword = suffix + "123";
            String name = request.getCustomerName() != null && !request.getCustomerName().isBlank()
                    ? request.getCustomerName() : "Customer";
            userRepository.save(new User(phone, tempPassword, name, Role.CUSTOMER));
        }

        Invoice invoice = new Invoice();
        String invoiceId = ZonedDateTime.now(ZoneId.of("Asia/Kolkata"))
                .format(DateTimeFormatter.ofPattern("ddMMyyHHmmss"));
        invoice.setId(Long.parseLong(invoiceId));
        invoice.setCustomerPhone(phone);
        invoice.setCustomerName(request.getCustomerName());

        for (InvoiceRequest.ItemRequest itemReq : request.getItems()) {
            InvoiceItem item = new InvoiceItem();
            item.setClothType(itemReq.getClothType());
            item.setServiceType(itemReq.getServiceType());
            item.setQuantity(itemReq.getQuantity());
            item.setPricePerUnit(itemReq.getPricePerUnit());
            invoice.addItem(item);
        }
        invoice.recalculateTotal();

        Invoice saved = invoiceRepository.save(invoice);

        // Generate and send the invoice PDF only after the invoice has an ID.
        WhatsAppService.SendResult whatsappResult = whatsAppService.sendInvoice(saved);

        return ResponseEntity.ok(new InvoiceCreatedResponse(
                saved,
                tempPassword,
                whatsappResult.sent(),
                whatsappResult.message()
        ));
    }

    @GetMapping("/invoices")
    public List<Invoice> getAllInvoices(@RequestParam(required = false) String phone) {
        if (phone != null && !phone.isBlank()) {
            return invoiceRepository.findByCustomerPhoneContainingOrderByCreatedDateDesc(phone.trim());
        }
        return invoiceRepository.findAllByOrderByCreatedDateDesc();
    }

    @PostMapping("/invoices/{id}/whatsapp")
    public ResponseEntity<?> sendInvoiceOnWhatsApp(@PathVariable String id) {
        return invoiceRepository.findById(Long.parseLong(id))
                .map(invoice -> {
                    WhatsAppService.SendResult result = whatsAppService.sendInvoice(invoice);
                    return ResponseEntity.ok(new WhatsAppSendResponse(
                            result.sent(),
                            result.message()
                    ));
                })
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/invoices/{id}/pdf")
    public ResponseEntity<byte[]> exportInvoicePdf(@PathVariable String id) {
        return invoiceRepository.findById(Long.parseLong(id))
                .map(invoice -> {
                    byte[] pdf = invoicePdfService.generateInvoicePdf(invoice);
                    return ResponseEntity.ok()
                            .contentType(MediaType.APPLICATION_PDF)
                            .header(HttpHeaders.CONTENT_DISPOSITION,
                                    "attachment; filename=invoice-" + invoice.getId() + ".pdf")
                            .body(pdf);
                })
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PutMapping("/invoices/{id}/status")
    public ResponseEntity<?> updateStatus(@PathVariable String id, @RequestBody StatusUpdate update) {
        return invoiceRepository.findById(Long.parseLong(id))
                .map(invoice -> {
                    invoice.setStatus(update.status());
                    invoiceRepository.save(invoice);
                    return ResponseEntity.ok(invoice);
                })
                .orElse(ResponseEntity.notFound().build());
    }

    public record StatusUpdate(InvoiceStatus status) {}

    public record WhatsAppSendResponse(boolean sent, String message) {}

    @GetMapping("/customers")
    public List<User> getCustomers() {
        return userRepository.findByRole(Role.CUSTOMER);
    }
}
