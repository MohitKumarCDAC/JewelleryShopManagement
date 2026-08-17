
package com.jewellery.jewelleryshop.services;

import lombok.RequiredArgsConstructor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;


    // =========================
    // SEND OTP EMAIL
    // =========================

    public void sendOtpEmail(
            String toEmail,
            String otp
    ) {

        SimpleMailMessage message =
                new SimpleMailMessage();

        message.setTo(toEmail);

        message.setSubject(
                "Mohit Jewellers - Password Reset OTP"
        );

        message.setText(
                "Dear User,\n\n"
                        + "Your OTP for resetting your Mohit Jewellers account password is:\n\n"
                        + otp
                        + "\n\n"
                        + "This OTP is valid for 5 minutes.\n\n"
                        + "If you did not request a password reset, "
                        + "please ignore this email.\n\n"
                        + "Regards,\n"
                        + "Mohit Jewellers"
        );

        mailSender.send(message);
    }
}

