package com.laundry.dto;

import com.laundry.model.ServiceType;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public class InvoiceRequest {

    @NotBlank
    private String customerPhone;

    private String customerName; // optional, used if we need to create the customer account

    @NotEmpty
    @Valid
    private List<ItemRequest> items;

    public static class ItemRequest {
        @NotBlank
        private String clothType;
        private ServiceType serviceType;
        private int quantity;
        private double pricePerUnit;

        public String getClothType() { return clothType; }
        public void setClothType(String clothType) { this.clothType = clothType; }
        public ServiceType getServiceType() { return serviceType; }
        public void setServiceType(ServiceType serviceType) { this.serviceType = serviceType; }
        public int getQuantity() { return quantity; }
        public void setQuantity(int quantity) { this.quantity = quantity; }
        public double getPricePerUnit() { return pricePerUnit; }
        public void setPricePerUnit(double pricePerUnit) { this.pricePerUnit = pricePerUnit; }
    }

    public String getCustomerPhone() { return customerPhone; }
    public void setCustomerPhone(String customerPhone) { this.customerPhone = customerPhone; }
    public String getCustomerName() { return customerName; }
    public void setCustomerName(String customerName) { this.customerName = customerName; }
    public List<ItemRequest> getItems() { return items; }
    public void setItems(List<ItemRequest> items) { this.items = items; }
}
