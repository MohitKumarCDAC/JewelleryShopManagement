package com.jewellery.jewelleryshop.dto;


import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RegisterRequest {

    private String userName;
    private String email;
    private String password;
    private String confirmPassword;
}
