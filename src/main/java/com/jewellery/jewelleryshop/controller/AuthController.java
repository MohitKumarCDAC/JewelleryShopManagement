package com.jewellery.jewelleryshop.controller;

import com.jewellery.jewelleryshop.dto.LoginRequest;
import com.jewellery.jewelleryshop.dto.LoginResponse;
import com.jewellery.jewelleryshop.services.AdminService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AdminService adminService;


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
}
