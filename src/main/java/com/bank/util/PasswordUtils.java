package com.bank.util;

import com.bank.exception.InvalidPasswordException;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

public class PasswordUtils {
    private static final SecureRandom RANDOM = new SecureRandom();

    public static String generateSalt() {
        byte[] salt = new byte[16];
        RANDOM.nextBytes(salt);
        StringBuilder sb = new StringBuilder();
        for (byte b : salt) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }

    public static String hashPassword(String rawPassword, String salt) {
        if (rawPassword == null) {
            throw new IllegalArgumentException("原始密码不能为空！");
        }
        if (salt == null) {
            throw new IllegalArgumentException("盐值不能为空！");
        }
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            md.update(salt.getBytes());
            byte[] hashedBytes = md.digest(rawPassword.getBytes());
            StringBuilder sb = new StringBuilder();
            for (byte b : hashedBytes) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 散列算法未找到", e);
        }
    }

    public static boolean verifyPassword(String rawPassword, String storedHash, String salt) {
        if (rawPassword == null || storedHash == null || salt == null) {
            return false;
        }
        String calculatedHash = hashPassword(rawPassword, salt);
        return calculatedHash.equals(storedHash);
    }

    public static void validatePasswordFormat(String password) {
        if (password == null || !password.matches("\\d{6}")) {
            throw new InvalidPasswordException("密码必须是6位数字！");
        }
    }

    public static void validatePasswordConfirmation(String password, String confirmPassword) {
        validatePasswordFormat(password);
        if (!password.equals(confirmPassword)) {
            throw new InvalidPasswordException("两次输入的密码不一致！");
        }
    }
}
