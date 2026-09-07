package com.laundry.controller;

import com.laundry.model.Invoice;
import com.laundry.repository.InvoiceRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/customer")
public class CustomerController {

    private final InvoiceRepository invoiceRepository;

    public CustomerController(InvoiceRepository invoiceRepository) {
        this.invoiceRepository = invoiceRepository;
    }

    // The AuthFilter already verified the token and attached the caller's
    // phone number to the request - we trust that, not anything the client sends,
    // so a customer can never fetch someone else's invoices.
    @GetMapping("/invoices")
    public List<Invoice> myInvoices(HttpServletRequest request) {
        String phone = (String) request.getAttribute("phone");
        return invoiceRepository.findByCustomerPhoneContainingOrderByCreatedDateDesc(phone);
    }

    @GetMapping("/me")
    public Map<String, Object> me(HttpServletRequest request) {
        return Map.of(
                "phone", request.getAttribute("phone"),
                "name", request.getAttribute("name")
        );
    }
}
