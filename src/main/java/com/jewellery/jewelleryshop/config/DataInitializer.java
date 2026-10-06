package com.jewellery.jewelleryshop.config;

import com.jewellery.jewelleryshop.entity.Admin;
import com.jewellery.jewelleryshop.repository.AdminRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final AdminRepository adminRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.initial-admin.username:admin}")
    private String initialAdminUsername;

    @Value("${app.initial-admin.email:admin@jewellers.com}")
    private String initialAdminEmail;

    @Value("${app.initial-admin.password:}")
    private String initialAdminPassword;

    @Override
    public void run(String... args) {

        /*
         * IMPORTANT:
         *
         * If an admin already exists, DO NOTHING.
         *
         * This prevents the admin password from being
         * reset every time the application starts.
         */
        if (adminRepository.count() > 0) {

            System.out.println(
                    "Existing admin account detected. " +
                            "Admin password was NOT changed."
            );

            return;
        }

        /*
         * New installation:
         *
         * Admin will only be created when
         * INITIAL_ADMIN_PASSWORD is explicitly configured.
         */
        if (initialAdminPassword == null
                || initialAdminPassword.isBlank()) {

            System.out.println(
                    "No admin account exists. " +
                            "Initial admin was NOT created because " +
                            "INITIAL_ADMIN_PASSWORD is not configured."
            );

            return;
        }

        Admin admin = Admin.builder()
                .userName(initialAdminUsername)
                .email(initialAdminEmail)
                .password(
                        passwordEncoder.encode(initialAdminPassword)
                )
                .build();

        adminRepository.save(admin);

        System.out.println(
                "Initial admin account created successfully."
        );
    }
}