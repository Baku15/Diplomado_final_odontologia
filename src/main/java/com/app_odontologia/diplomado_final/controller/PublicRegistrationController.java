package com.app_odontologia.diplomado_final.controller;

import com.app_odontologia.diplomado_final.dto.ApiResponse;
import com.app_odontologia.diplomado_final.dto.RegistrationRequestCreateDto;
import com.app_odontologia.diplomado_final.service.RegistrationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/public")
@RequiredArgsConstructor
public class PublicRegistrationController {

    private final RegistrationService registrationService;

    @PostMapping("/registration/dentist")
    public ResponseEntity<?> create(@RequestBody RegistrationRequestCreateDto dto) {
        var rr = registrationService.create(dto);

        // Devolvemos JSON clarito
        return ResponseEntity.ok(Map.of(
                "ok", true,
                "id", rr.getId()
        ));
    }
}