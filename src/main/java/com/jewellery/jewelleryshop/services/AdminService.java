
package com.jewellery.jewelleryshop.services;

import com.jewellery.jewelleryshop.security.JwtService;
import com.jewellery.jewelleryshop.entity.Admin;
import com.jewellery.jewelleryshop.repository.AdminRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
public class AdminService {

    private final AdminRepository adminRepository;

    private final PasswordEncoder passwordEncoder;

    private final JwtService jwtService;

    private final EmailService emailService;


    // =========================
    // OTP STORAGE
    // =========================

    private final Map<String, OtpData> otpStorage =
            new ConcurrentHashMap<>();


    // =========================
    // CREATE NEW USER
    // =========================

    public Admin createAdmin(
            String username,
            String email,
            String password
    ) {

        if (adminRepository
                .findByUserName(username)
                .isPresent()) {

            throw new RuntimeException(
                    "Username already exists"
            );
        }


        if (adminRepository
                .findByEmail(email)
                .isPresent()) {

            throw new RuntimeException(
                    "Email already exists"
            );
        }


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


        if (!passwordEncoder.matches(
                password,
                admin.getPassword()
        )) {

            throw new RuntimeException(
                    "Invalid username or password"
            );
        }


        return jwtService.generateToken(
                admin.getUserName()
        );
    }


    // =========================
    // FORGOT PASSWORD
    // SEND OTP
    // =========================

    public void sendForgotPasswordOtp(
            String email
    ) {

        Admin admin =
                adminRepository
                        .findByEmail(email)
                        .orElseThrow(
                                () -> new RuntimeException(
                                        "No account found with this email"
                                )
                        );


        // Generate 6 digit OTP

        SecureRandom random =
                new SecureRandom();

        String otp =
                String.format(
                        "%06d",
                        random.nextInt(1000000)
                );


        // OTP valid for 5 minutes

        LocalDateTime expiryTime =
                LocalDateTime.now()
                        .plusMinutes(5);


        otpStorage.put(
                email,
                new OtpData(
                        otp,
                        expiryTime
                )
        );


        // Send OTP

        emailService.sendOtpEmail(
                admin.getEmail(),
                otp
        );
    }


    // =========================
    // VERIFY OTP
    // =========================

    public boolean verifyOtp(
            String email,
            String otp
    ) {

        OtpData otpData =
                otpStorage.get(email);


        if (otpData == null) {

            throw new RuntimeException(
                    "OTP not found or expired"
            );
        }


        // Check expiry

        if (LocalDateTime.now()
                .isAfter(otpData.expiryTime())) {

            otpStorage.remove(email);

            throw new RuntimeException(
                    "OTP has expired"
            );
        }


        // Check OTP

        if (!otpData.otp()
                .equals(otp)) {

            throw new RuntimeException(
                    "Invalid OTP"
            );
        }


        return true;
    }


    // =========================
    // RESET PASSWORD
    // =========================

    public void resetPassword(
            String email,
            String otp,
            String newPassword
    ) {

        // Verify OTP first

        verifyOtp(
                email,
                otp
        );


        Admin admin =
                adminRepository
                        .findByEmail(email)
                        .orElseThrow(
                                () -> new RuntimeException(
                                        "Account not found"
                                )
                        );


        // Encode new password

        admin.setPassword(
                passwordEncoder.encode(
                        newPassword
                )
        );


        adminRepository.save(admin);


        // OTP use hone ke baad remove

        otpStorage.remove(email);
    }


    // =========================
    // OTP DATA
    // =========================

    private record OtpData(
            String otp,
            LocalDateTime expiryTime
    ) {
    }
}

