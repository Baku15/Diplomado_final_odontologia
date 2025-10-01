package com.app_odontologia.diplomado_final.util;

import java.security.SecureRandom;

public final class PasswordGenerator {
    private static final String ALPHABET =
            "ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijkmnopqrstuvwxyz23456789!@#$%^&*()-_=+";
    private static final SecureRandom RNG = new SecureRandom();

    private PasswordGenerator() {}

    public static String secureTemp() {
        return secureTemp(14);
    }

    public static String secureTemp(int length) {
        if (length < 12) length = 12;
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            sb.append(ALPHABET.charAt(RNG.nextInt(ALPHABET.length())));
        }
        return sb.toString();
    }
}