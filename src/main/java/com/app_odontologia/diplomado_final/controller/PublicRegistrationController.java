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

@RestController
@RequestMapping("/api/registration")
@RequiredArgsConstructor
public class PublicRegistrationController {
    private final RegistrationService registrationService;

    @PostMapping
    public ResponseEntity<ApiResponse> create(@Valid @RequestBody RegistrationRequestCreateDto dto) {
        registrationService.create(dto);
        return ResponseEntity.ok(new ApiResponse("Solicitud recibida. Te contactaremos por email."));
    }
}
