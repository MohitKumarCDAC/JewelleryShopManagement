package com.jewellery.jewelleryshop.services;

import com.jewellery.jewelleryshop.security.JwtService;
import com.jewellery.jewelleryshop.entity.Admin;
import com.jewellery.jewelleryshop.repository.AdminRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AdminService {

    private final AdminRepository adminRepository;

    private final PasswordEncoder passwordEncoder;

    private final JwtService jwtService;


    // =========================
    // CREATE ADMIN
    // =========================

    public Admin createAdmin(
            String username,
            String email,
            String password
    ) {

        Admin admin = Admin.builder()
                .userName(username)
                .email(email)
                .password(
                        passwordEncoder.encode(password)
                )
                .build();

        return adminRepository.save(admin);
    }


    // =========================
    // ADMIN LOGIN
    // =========================

    public String login(
            String username,
            String password
    ) {

        Admin admin =
                adminRepository
                        .findByUserName(username)
                        .orElseThrow(
                                () -> new RuntimeException(
                                        "Invalid username or password"
                                )
                        );


        // Password verify

        if (!passwordEncoder.matches(
                password,
                admin.getPassword()
        )) {

            throw new RuntimeException(
                    "Invalid username or password"
            );
        }


        // JWT generate

        return jwtService.generateToken(
                admin.getUserName()
        );
    }
}