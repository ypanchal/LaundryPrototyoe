package com.laundry.config;

import com.laundry.model.*;
import com.laundry.repository.InvoiceRepository;
import com.laundry.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

@Component
public class DataSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final InvoiceRepository invoiceRepository;

    public DataSeeder(UserRepository userRepository, InvoiceRepository invoiceRepository) {
        this.userRepository = userRepository;
        this.invoiceRepository = invoiceRepository;
    }

    @Override
    public void run(String... args) {
        // Shop owner / admin login
        userRepository.save(new User("admin", "admin123", "Shop Owner", Role.ADMIN));

        // Demo customer login
        User customer = userRepository.save(new User("9999999999", "customer123", "Ramesh Kumar", Role.CUSTOMER));

        // One sample past invoice so the customer dashboard isn't empty on first run
        Invoice invoice = new Invoice();
        String invoiceId = ZonedDateTime.now(ZoneId.of("Asia/Kolkata"))
                .format(DateTimeFormatter.ofPattern("ddMMyyHHmmss"));
        invoice.setId(Long.parseLong(invoiceId));
        invoice.setCustomerPhone(customer.getPhone());
        invoice.setCustomerName(customer.getName());
        invoice.setStatus(InvoiceStatus.DELIVERED);

        InvoiceItem item1 = new InvoiceItem();
        item1.setClothType("Shirt");
        item1.setServiceType(ServiceType.WASH_AND_IRON);
        item1.setQuantity(5);
        item1.setPricePerUnit(20);
        invoice.addItem(item1);

        InvoiceItem item2 = new InvoiceItem();
        item2.setClothType("Trouser");
        item2.setServiceType(ServiceType.IRON);
        item2.setQuantity(3);
        item2.setPricePerUnit(15);
        invoice.addItem(item2);

        invoice.recalculateTotal();
        invoiceRepository.save(invoice);

        System.out.println("=========================================");
        System.out.println(" Demo logins:");
        System.out.println("   Admin    -> phone: admin        password: admin123");
        System.out.println("   Customer -> phone: 9999999999   password: customer123");
        System.out.println("=========================================");
    }
}
