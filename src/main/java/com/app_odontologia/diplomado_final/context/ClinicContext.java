package com.app_odontologia.diplomado_final.context;

public class ClinicContext {
    private static final ThreadLocal<Long> clinicIdHolder = new ThreadLocal<>();

    public static void setClinicId(Long clinicId) {
        clinicIdHolder.set(clinicId);
    }

    public static Long getClinicId() {
        return clinicIdHolder.get();
    }

    public static void clear() {
        clinicIdHolder.remove();
    }
}
