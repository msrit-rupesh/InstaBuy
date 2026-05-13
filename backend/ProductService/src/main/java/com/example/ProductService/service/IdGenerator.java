package com.example.ProductService.service;

import java.security.SecureRandom;

public final class IdGenerator {

    private static final char[] ALPHABET = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789".toCharArray();
    private static final int DEFAULT_LENGTH = 10;
    private static final SecureRandom RNG = new SecureRandom();

    private IdGenerator() {}

    public static String nextId() {
        return nextId(DEFAULT_LENGTH);
    }

    public static String nextId(int length) {
        char[] buf = new char[length];
        for (int i = 0; i < length; i++) {
            buf[i] = ALPHABET[RNG.nextInt(ALPHABET.length)];
        }
        return new String(buf);
    }
}