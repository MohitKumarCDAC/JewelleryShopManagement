
package com.jewellery.jewelleryshop.controller;

import com.jewellery.jewelleryshop.dto.ForgotPasswordRequest;
import com.jewellery.jewelleryshop.dto.LoginRequest;
import com.jewellery.jewelleryshop.dto.LoginResponse;
import com.jewellery.jewelleryshop.dto.RegisterRequest;
import com.jewellery.jewelleryshop.dto.ResetPasswordRequest;
import com.jewellery.jewelleryshop.dto.VerifyOtpRequest;
import com.jewellery.jewelleryshop.entity.Admin;
import com.jewellery.jewelleryshop.services.AdminService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AdminService adminService;


    // =========================
    // LOGIN
    // =========================

    @PostMapping("/login")
    public LoginResponse login(
            @RequestBody LoginRequest loginRequest
    ) {

        String token =
                adminService.login(
                        loginRequest.getUserName(),
                        loginRequest.getPassword()
                );

        return new LoginResponse(token);
    }


    // =========================
    // CREATE NEW USER
    // =========================

    @PostMapping("/register")
    public Admin register(
            @RequestBody RegisterRequest registerRequest
    ) {

        if (!registerRequest.getPassword()
                .equals(registerRequest.getConfirmPassword())) {

            throw new RuntimeException(
                    "Password and confirm password do not match"
            );
        }

        return adminService.createAdmin(
                registerRequest.getUserName(),
                registerRequest.getEmail(),
                registerRequest.getPassword()
        );
    }


    // =========================
    // FORGOT PASSWORD
    // SEND OTP
    // =========================

    @PostMapping("/forgot-password")
    public String forgotPassword(
            @RequestBody ForgotPasswordRequest request
    ) {

        adminService.sendForgotPasswordOtp(
                request.getEmail()
        );

        return "OTP sent successfully to your email";
    }


    // =========================
    // VERIFY OTP
    // =========================

    @PostMapping("/verify-otp")
    public String verifyOtp(
            @RequestBody VerifyOtpRequest request
    ) {

        adminService.verifyOtp(
                request.getEmail(),
                request.getOtp()
        );

        return "OTP verified successfully";
    }


    // =========================
    // RESET PASSWORD
    // =========================

    @PostMapping("/reset-password")
    public String resetPassword(
            @RequestBody ResetPasswordRequest request
    ) {

        if (!request.getNewPassword()
                .equals(request.getConfirmPassword())) {

            throw new RuntimeException(
                    "New password and confirm password do not match"
            );
        }

        adminService.resetPassword(
                request.getEmail(),
                request.getOtp(),
                request.getNewPassword()
        );

        return "Password reset successfully";
    }
}

