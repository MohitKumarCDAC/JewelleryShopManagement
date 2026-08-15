package com.jewellery.jewelleryshop.config;

import com.jewellery.jewelleryshop.entity.Admin;
import com.jewellery.jewelleryshop.repository.AdminRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final AdminRepository adminRepository;

    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {

        Admin admin = adminRepository
                .findByUserName("admin")
                .orElse(null);

        if (admin == null) {

            admin = Admin.builder()
                    .userName("admin")
                    .email("admin@jewellers.com")
                    .password(
                            passwordEncoder.encode("admin123")
                    )
                    .build();

        } else {

            admin.setPassword(
                    passwordEncoder.encode("admin123")
            );

        }

        adminRepository.save(admin);

        System.out.println(
                "Admin password initialized/reset successfully!"
        );
    }
}