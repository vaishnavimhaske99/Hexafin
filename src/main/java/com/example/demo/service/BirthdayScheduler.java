package com.example.demo.service;

import java.time.LocalDate;
import java.util.List;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.example.demo.entity.Customer;
import com.example.demo.repository.CustomerRepository;

@Service
public class BirthdayScheduler {

    private final CustomerRepository customerRepository;

    public BirthdayScheduler(CustomerRepository customerRepository) {
        this.customerRepository = customerRepository;
    }

    @Scheduled(cron = "0 0 9 * * *")
    public void sendBirthdayWishes() {
        LocalDate today = LocalDate.now();
        int month = today.getMonthValue();
        int day = today.getDayOfMonth();

        List<Customer> birthdayCustomers =
                customerRepository.findByBirthday(month, day);

        if (birthdayCustomers.isEmpty()) {
            System.out.println("🎂 No birthdays today (" + today + ")");
            return;
        }

        System.out.println("🎂 " + birthdayCustomers.size() + " birthdays today!");

        for (Customer c : birthdayCustomers) {
            String name = c.getFirstName() != null ? c.getFirstName() : "Customer";
            String phone = c.getPhone();

            System.out.println("════════════════════════════════");
            System.out.println("🎂 BIRTHDAY: " + name);
            System.out.println("📞 Phone: " + phone);
            System.out.println("🎉 Message:");
            System.out.println("   Dear " + name + ",");
            System.out.println("   🎂 Happy Birthday! 🎉");
            System.out.println("   Wishing you a wonderful year...");
            System.out.println("════════════════════════════════");
        }
    }
}