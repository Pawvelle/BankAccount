package com.bank.ui;

import com.bank.model.BankAccount;
import com.bank.model.BankUser;

import java.util.Scanner;

public class ConsoleUtils {
    private static final Scanner scanner = new Scanner(System.in);
    private static Runnable exitCallback;

    public static void setExitCallback(Runnable callback) {
        exitCallback = callback;
    }

    public static String readPasswordWithConfirmation(String prompt, String confirmPrompt) {
        String password = readRequiredText(prompt);
        String confirmPassword = readRequiredText(confirmPrompt);
        if (!BankAccount.validatePassword(password, confirmPassword)) {
            throw new IllegalArgumentException("密码验证失败。");
        }
        return password;
    }

    public static String readRequiredText(String prompt) {
        while (true) {
            System.out.print(prompt);
            ensureInputAvailable();
            String text = scanner.nextLine().trim();
            if (!text.isEmpty()) {
                return text;
            }
            System.out.println("输入不能为空，请重新输入。");
        }
    }

    public static int readInt(String prompt) {
        while (true) {
            System.out.print(prompt);
            ensureInputAvailable();
            String input = scanner.nextLine().trim();
            try {
                return Integer.parseInt(input);
            } catch (NumberFormatException e) {
                System.out.println("请输入有效数字。");
            }
        }
    }

    public static double readDouble(String prompt) {
        while (true) {
            System.out.print(prompt);
            ensureInputAvailable();
            String input = scanner.nextLine().trim();
            try {
                return Double.parseDouble(input);
            } catch (NumberFormatException e) {
                System.out.println("请输入有效数字。");
            }
        }
    }

    public static String formatMoney(double amount) {
        return String.format("%.2f 元", amount);
    }

    public static void showOperationResult(String title, String detail, String prompt) {
        System.out.println();
        System.out.println("========== 操作结果 ==========");
        System.out.println(title);
        System.out.println(detail);
        waitForEnter(prompt);
    }

    public static void showProfileUpdateResult(String message, BankUser user) {
        System.out.println();
        System.out.println("========== 修改结果 ==========");
        System.out.println(message);
        System.out.println("当前信息：");
        System.out.println(user.getUserInfo());
        waitForEnter("按回车键返回修改菜单...");
    }

    public static void waitForEnter(String prompt) {
        System.out.print(prompt);
        ensureInputAvailable();
        scanner.nextLine();
    }

    public static void ensureInputAvailable() {
        if (!scanner.hasNextLine()) {
            System.out.println();
            System.out.println("输入已结束，系统自动退出。");
            if (exitCallback != null) {
                exitCallback.run();
            }
            System.exit(0);
        }
    }
}
