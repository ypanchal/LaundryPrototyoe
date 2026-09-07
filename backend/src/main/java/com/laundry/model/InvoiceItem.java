package com.laundry.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;

@Entity
public class InvoiceItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // e.g. "Shirt", "Trouser", "Bedsheet"
    private String clothType;

    @Enumerated(EnumType.STRING)
    private ServiceType serviceType;

    private int quantity;

    private double pricePerUnit;

    @ManyToOne
    @JoinColumn(name = "invoice_id")
    @JsonIgnore // prevent infinite recursion when serializing Invoice -> items -> invoice
    private Invoice invoice;

    public InvoiceItem() {}

    public double lineTotal() {
        return quantity * pricePerUnit;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getClothType() { return clothType; }
    public void setClothType(String clothType) { this.clothType = clothType; }
    public ServiceType getServiceType() { return serviceType; }
    public void setServiceType(ServiceType serviceType) { this.serviceType = serviceType; }
    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }
    public double getPricePerUnit() { return pricePerUnit; }
    public void setPricePerUnit(double pricePerUnit) { this.pricePerUnit = pricePerUnit; }
    public Invoice getInvoice() { return invoice; }
    public void setInvoice(Invoice invoice) { this.invoice = invoice; }
}
