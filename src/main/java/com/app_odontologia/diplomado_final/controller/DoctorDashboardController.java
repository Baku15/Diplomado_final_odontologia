package com.app_odontologia.diplomado_final.controller;

import com.app_odontologia.diplomado_final.dto.doctor.DoctorDashboardTodayDto;
import com.app_odontologia.diplomado_final.service.AppointmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/doctor/dashboard")
@RequiredArgsConstructor
public class DoctorDashboardController {

    private final AppointmentService appointmentService;

    @GetMapping("/today")
    public DoctorDashboardTodayDto getTodayDashboard(Authentication auth) {
        return appointmentService.getDoctorDashboardToday(auth.getName());
    }
}
