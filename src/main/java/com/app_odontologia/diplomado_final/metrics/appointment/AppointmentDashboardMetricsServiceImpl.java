package com.app_odontologia.diplomado_final.metrics.appointment;

import com.app_odontologia.diplomado_final.model.entity.Appointment.AppointmentStatus;
import com.app_odontologia.diplomado_final.model.entity.User;
import com.app_odontologia.diplomado_final.repository.AppointmentRepository;
import com.app_odontologia.diplomado_final.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AppointmentDashboardMetricsServiceImpl
        implements AppointmentDashboardMetricsService {

    private final AppointmentRepository appointmentRepository;
    private final UserRepository userRepository;

    @Override
    public AppointmentDashboardDto getDashboardMetrics(
            Long doctorId,
            AppointmentMetricPeriod period,
            AppointmentMetricRange range
    ) {

        LocalDate from = range.start();
        LocalDate to = range.end();

        // =========================
        // 📊 DONUT
        // =========================
        long scheduled =
                appointmentRepository.countByDoctorIdAndDateBetweenAndStatus(
                        doctorId, from, to, AppointmentStatus.SCHEDULED
                );

        long completed =
                appointmentRepository.countByDoctorIdAndDateBetweenAndStatus(
                        doctorId, from, to, AppointmentStatus.COMPLETED
                );

        long cancelled =
                appointmentRepository.countByDoctorIdAndDateBetweenAndStatus(
                        doctorId, from, to, AppointmentStatus.CANCELLED
                );

        long noShow =
                appointmentRepository.countByDoctorIdAndDateBetweenAndStatus(
                        doctorId, from, to, AppointmentStatus.NO_SHOW
                );

        long total = scheduled + completed + cancelled + noShow;

        AppointmentTodayMetricsDto todayMetrics =
                new AppointmentTodayMetricsDto(
                        total,
                        scheduled,
                        completed,
                        cancelled,
                        noShow,
                        total == 0 ? 0 : completed * 100.0 / total,
                        total == 0 ? 0 : noShow * 100.0 / total
                );

        // =========================
        // 📈 HISTÓRICO (AGRUPADO)
        // =========================
        Map<LocalDate, Long> completedRaw = toMap(
                appointmentRepository.countCompletedByDoctorGroupedByDate(
                        doctorId, from, to
                )
        );

        Map<LocalDate, Long> noShowRaw = toMap(
                appointmentRepository.countNoShowByDoctorGroupedByDate(
                        doctorId, from, to
                )
        );

        Map<LocalDate, Long> completedFinal;
        Map<LocalDate, Long> noShowFinal;

        if (period == AppointmentMetricPeriod.MONTH) {
            completedFinal = groupByWeek(completedRaw);
            noShowFinal = groupByWeek(noShowRaw);
        } else {
            completedFinal = completedRaw;
            noShowFinal = noShowRaw;
        }

        AppointmentHistoricalMetricsDto historicalMetrics =
                new AppointmentHistoricalMetricsDto(
                        completedFinal,
                        noShowFinal
                );

        // =========================
        // 🔮 FUTURO
        // =========================
        LocalDate today = LocalDate.now();

        long nextWeekScheduled =
                appointmentRepository.countByDoctorIdAndDateBetweenAndStatus(
                        doctorId,
                        today.plusDays(1),
                        today.plusDays(7),
                        AppointmentStatus.SCHEDULED
                );

        long nextMonthScheduled =
                appointmentRepository.countByDoctorIdAndDateBetweenAndStatus(
                        doctorId,
                        today.plusDays(1),
                        today.plusMonths(1),
                        AppointmentStatus.SCHEDULED
                );

        AppointmentFutureMetricsDto futureMetrics =
                new AppointmentFutureMetricsDto(
                        nextWeekScheduled,
                        nextMonthScheduled,
                        0,
                        0
                );

        return new AppointmentDashboardDto(
                todayMetrics,
                historicalMetrics,
                futureMetrics
        );
    }

    // =========================
    // 🔹 AGRUPACIÓN POR SEMANA
    // =========================
    private Map<LocalDate, Long> groupByWeek(Map<LocalDate, Long> daily) {

        Map<LocalDate, Long> weekly = new LinkedHashMap<>();

        for (Map.Entry<LocalDate, Long> entry : daily.entrySet()) {

            LocalDate weekStart =
                    entry.getKey()
                            .with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));

            weekly.merge(weekStart, entry.getValue(), Long::sum);
        }

        return weekly;
    }

    // =========================
    // UTIL
    // =========================
    private Map<LocalDate, Long> toMap(List<Object[]> rows) {
        Map<LocalDate, Long> map = new LinkedHashMap<>();
        for (Object[] row : rows) {
            map.put((LocalDate) row[0], (Long) row[1]);
        }
        return map;
    }

    @Override
    public Long resolveDoctorIdByUsername(String username) {
        return userRepository.findByUsername(username)
                .map(User::getId)
                .orElseThrow(() ->
                        new IllegalStateException("Doctor no encontrado: " + username)
                );
    }
}
