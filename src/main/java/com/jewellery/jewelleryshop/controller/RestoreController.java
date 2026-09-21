package com.jewellery.jewelleryshop.controller;

import com.jewellery.jewelleryshop.services.RestoreService;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/backup")
public class RestoreController {

    private final RestoreService restoreService;

    public RestoreController(RestoreService restoreService) {
        this.restoreService = restoreService;
    }

    @PostMapping(
            value = "/restore",
            consumes = "multipart/form-data"
    )
    public ResponseEntity<?> restoreBackup(
            @RequestParam("file") MultipartFile file
    ) {

        try {

            String message =
                    restoreService.restoreFromExcel(file);

            Map<String, Object> response =
                    new HashMap<>();

            response.put("success", true);
            response.put("message", message);

            return ResponseEntity.ok(response);

        } catch (Exception e) {

            Map<String, Object> response =
                    new HashMap<>();

            response.put("success", false);

            String message = e.getMessage();

            if (message == null || message.isBlank()) {
                message = "Restore failed.";
            }

            response.put("message", message);

            return ResponseEntity
                    .badRequest()
                    .body(response);
        }
    }
}