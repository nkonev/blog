package com.github.nkonev.blog.utils;

import java.util.Random;

public class CodeGenUtils {

    public static String getRandomCode(int length) {
        Random random = new Random();
        StringBuilder stringBuilder = new StringBuilder();
        String alphabet = "1234567890abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";
        for (int i = 0; i < length; i++) {
            stringBuilder.append(alphabet.charAt(random.nextInt(alphabet.length())));
        }
        return stringBuilder.toString();
    }

}
