package com.bank.ui;

import com.bank.model.BankUser;
import com.bank.util.PasswordUtils;

import java.util.Scanner;

public class ConsoleUtils {
    private static final Scanner scanner = new Scanner(System.in);
    private static Runnable exitCallback;

    // ANSI Colors & Styles (Minimalist & Calm)
    public static final String RESET = "\u001B[0m";
    public static final String BOLD = "\u001B[1m";
    public static final String DIM = "\u001B[2m";
    
    public static final String RED = "\u001B[31m";
    public static final String GREEN = "\u001B[32m";
    public static final String YELLOW = "\u001B[33m";
    public static final String BLUE = "\u001B[34m";
    public static final String MAGENTA = "\u001B[35m";
    public static final String CYAN = "\u001B[36m";
    public static final String GRAY = "\u001B[90m";
    
    public static final String BRIGHT_RED = "\u001B[91m";
    public static final String BRIGHT_GREEN = "\u001B[92m";
    public static final String BRIGHT_YELLOW = "\u001B[93m";
    public static final String BRIGHT_CYAN = "\u001B[96m";
    public static final String BRIGHT_MAGENTA = "\u001B[95m";

    // Grayscale / Clean text icons (No Emojis)
    public static final String ICON_SUCCESS = BRIGHT_GREEN + "✔" + RESET;
    public static final String ICON_ERROR = BRIGHT_RED + "✖" + RESET;
    public static final String ICON_WARNING = BRIGHT_YELLOW + "⚠" + RESET;
    public static final String ICON_INFO = GRAY + "•" + RESET;
    public static final String ICON_PROMPT = BRIGHT_CYAN + "?" + RESET;
    public static final String ICON_ARROW = BRIGHT_CYAN + "❯" + RESET;
    public static final String ICON_BULLET = GRAY + "•" + RESET;

    public static void setExitCallback(Runnable callback) {
        exitCallback = callback;
    }

    public static String color(String text, String ansiColor) {
        return ansiColor + text + RESET;
    }

    public static String bold(String text) {
        return BOLD + text + RESET;
    }

    public static void showSuccess(String message) {
        System.out.println("  " + ICON_SUCCESS + " " + message);
    }

    public static void showError(String message) {
        System.out.println("  " + ICON_ERROR + " " + color(message, BRIGHT_RED));
    }

    public static void showWarning(String message) {
        System.out.println("  " + ICON_WARNING + " " + color(message, BRIGHT_YELLOW));
    }

    public static void showInfo(String message) {
        System.out.println("  " + ICON_INFO + " " + color(message, GRAY));
    }

    public static void showSpinner(String message, int durationMs) {
        String[] spinnerFrames = {"⠋", "⠙", "⠹", "⠸", "⠼", "⠴", "⠦", "⠧", "⠇", "⠏"};
        int interval = 60;
        int elapsed = 0;
        int i = 0;
        while (elapsed < durationMs) {
            String frame = color(spinnerFrames[i % spinnerFrames.length], GRAY);
            System.out.print("\r  " + frame + " " + color(message, GRAY));
            try {
                Thread.sleep(interval);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
            elapsed += interval;
            i++;
        }
        System.out.print("\r  " + ICON_SUCCESS + " " + message + "\n");
    }

    public static String readPasswordWithConfirmation(String prompt, String confirmPrompt) {
        String password = readRequiredText(prompt);
        String confirmPassword = readRequiredText(confirmPrompt);
        PasswordUtils.validatePasswordConfirmation(password, confirmPassword);
        return password;
    }

    public static String readRequiredText(String prompt) {
        while (true) {
            System.out.print(ICON_PROMPT + " " + color(prompt, GRAY) + " " + ICON_ARROW + " ");
            ensureInputAvailable();
            String text = scanner.nextLine().trim();
            if (!text.isEmpty()) {
                return text;
            }
            showError("输入不能为空，请重新输入。");
        }
    }

    public static int readInt(String prompt) {
        while (true) {
            System.out.print(ICON_PROMPT + " " + color(prompt, GRAY) + " " + ICON_ARROW + " ");
            ensureInputAvailable();
            String input = scanner.nextLine().trim();
            try {
                return Integer.parseInt(input);
            } catch (NumberFormatException e) {
                showError("请输入有效数字。");
            }
        }
    }

    public static double readDouble(String prompt) {
        while (true) {
            System.out.print(ICON_PROMPT + " " + color(prompt, GRAY) + " " + ICON_ARROW + " ");
            ensureInputAvailable();
            String input = scanner.nextLine().trim();
            try {
                return Double.parseDouble(input);
            } catch (NumberFormatException e) {
                showError("请输入有效数字。");
            }
        }
    }

    public static String formatMoney(double amount) {
        return bold(String.format("%,.2f 元", amount));
    }

    public static void showOperationResult(String title, String detail, String prompt) {
        System.out.println();
        System.out.println(color("  ────────────────────────────────────────────────────────", GRAY));
        System.out.println("  " + ICON_SUCCESS + " " + bold(title));
        if (detail != null && !detail.isEmpty()) {
            String[] lines = detail.split("\n");
            for (String line : lines) {
                System.out.println("    " + color(line, GRAY));
            }
        }
        System.out.println(color("  ────────────────────────────────────────────────────────", GRAY));
        System.out.println();
        waitForEnter(prompt);
    }

    public static void showProfileUpdateResult(String message, BankUser user) {
        System.out.println();
        System.out.println(color("  ────────────────────────────────────────────────────────", GRAY));
        System.out.println("  " + ICON_SUCCESS + " " + bold(message));
        System.out.println("  " + bold("当前信息："));
        String[] lines = user.getUserInfo().split("\n");
        for (String line : lines) {
            System.out.println("    " + line);
        }
        System.out.println(color("  ────────────────────────────────────────────────────────", GRAY));
        System.out.println();
        waitForEnter("按回车键返回修改菜单...");
    }

    public static void waitForEnter(String prompt) {
        System.out.print(color("  " + prompt, GRAY));
        ensureInputAvailable();
        scanner.nextLine();
    }

    public static void ensureInputAvailable() {
        if (!scanner.hasNextLine()) {
            System.out.println();
            showInfo("输入已结束，系统自动退出。");
            if (exitCallback != null) {
                exitCallback.run();
            }
            System.exit(0);
        }
    }
}
