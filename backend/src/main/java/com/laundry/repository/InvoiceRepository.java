package com.laundry.repository;

import com.laundry.model.Invoice;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface InvoiceRepository extends JpaRepository<Invoice, Long> {
    List<Invoice> findByCustomerPhoneContainingOrderByCreatedDateDesc(String customerPhone);
    List<Invoice> findAllByOrderByCreatedDateDesc();
}
