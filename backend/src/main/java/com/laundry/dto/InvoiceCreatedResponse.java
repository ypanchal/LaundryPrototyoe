package com.laundry.dto;

import com.laundry.model.Invoice;

public class InvoiceCreatedResponse {
    private Invoice invoice;
    private String newCustomerTempPassword;
    private boolean whatsappSent;
    private String whatsappMessage;

    public InvoiceCreatedResponse(Invoice invoice,
                                  String newCustomerTempPassword,
                                  boolean whatsappSent,
                                  String whatsappMessage) {
        this.invoice = invoice;
        this.newCustomerTempPassword = newCustomerTempPassword;
        this.whatsappSent = whatsappSent;
        this.whatsappMessage = whatsappMessage;
    }

    public Invoice getInvoice() { return invoice; }
    public String getNewCustomerTempPassword() { return newCustomerTempPassword; }
    public boolean isWhatsappSent() { return whatsappSent; }
    public String getWhatsappMessage() { return whatsappMessage; }
}
