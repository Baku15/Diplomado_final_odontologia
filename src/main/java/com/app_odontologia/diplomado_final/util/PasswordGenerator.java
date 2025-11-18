package com.app_odontologia.diplomado_final.util;

import java.security.SecureRandom;

public final class PasswordGenerator {
    private static final String UPPER = "ABCDEFGHJKLMNPQRSTUVWXYZ"; // sin I/O confusas
    private static final String LOWER = "abcdefghijkmnopqrstuvwxyz"; // sin l
    private static final String DIGITS = "23456789";                 // sin 0/1
    private static final String SYMBOLS = "@#$%&*?!";
    private static final String ALL = UPPER + LOWER + DIGITS + SYMBOLS;

    private static final SecureRandom RAND = new SecureRandom();

    private PasswordGenerator() {}

    public static String secureTemp(int length) {
        if (length < 12) length = 12; // mínimo recomendado
        StringBuilder sb = new StringBuilder(length);

        // garantizar al menos 1 de cada tipo
        sb.append(pick(UPPER));
        sb.append(pick(LOWER));
        sb.append(pick(DIGITS));
        sb.append(pick(SYMBOLS));

        for (int i = sb.length(); i < length; i++) {
            sb.append(pick(ALL));
        }

        // mezclar
        return shuffle(sb.toString());
    }

    public static String secureTemp() {
        return secureTemp(14);
    }

    private static char pick(String s) {
        return s.charAt(RAND.nextInt(s.length()));
    }

    private static String shuffle(String input) {
        char[] a = input.toCharArray();
        for (int i = a.length - 1; i > 0; i--) {
            int j = RAND.nextInt(i + 1);
            char tmp = a[i];
            a[i] = a[j];
            a[j] = tmp;
        }
        return new String(a);
    }
}