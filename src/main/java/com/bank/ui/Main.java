package com.bank.ui;

import com.bank.service.BankUserManager;

public class Main {
    private final BankUserManager userManager = new BankUserManager();
    private final CardMenuUI cardMenuUI = new CardMenuUI(userManager);
    private final UserMenuUI userMenuUI = new UserMenuUI(userManager, cardMenuUI);

    public static void main(String[] args) {
        new Main().start();
    }

    private void start() {
        ConsoleUtils.setExitCallback(userManager::saveData);

        boolean running = true;
        printWelcome();
        while (running) {
            printMainMenu();
            int choice = ConsoleUtils.readInt("请选择功能");
            switch (choice) {
                case 1:
                    userMenuUI.registerUser();
                    break;
                case 2:
                    userMenuUI.loginUser();
                    break;
                case 3:
                    userMenuUI.showAssetRanking();
                    break;
                case 4:
                    running = false;
                    ConsoleUtils.showSpinner("正在保存数据并关闭系统...", 600);
                    userManager.saveData(); // 退出前保存数据
                    ConsoleUtils.showSuccess("系统已安全退出，欢迎下次使用。");
                    break;
                default:
                    ConsoleUtils.showError("无效选项，请重新选择。");
                    break;
            }
        }
    }

    private void printWelcome() {
        System.out.println();
        System.out.println("  " + ConsoleUtils.bold("BANK ACCOUNT CLI") + ConsoleUtils.color(" (v2.0.0)", ConsoleUtils.GRAY));
        System.out.println("  " + ConsoleUtils.color("────────────────────────────────────────────────────────", ConsoleUtils.GRAY));
    }

    private void printMainMenu() {
        System.out.println();
        System.out.println("  " + ConsoleUtils.color("1.", ConsoleUtils.GRAY) + " Register account" + ConsoleUtils.color("  (注册账户)", ConsoleUtils.GRAY));
        System.out.println("  " + ConsoleUtils.color("2.", ConsoleUtils.GRAY) + " Login account" + ConsoleUtils.color("     (登录账户)", ConsoleUtils.GRAY));
        System.out.println("  " + ConsoleUtils.color("3.", ConsoleUtils.GRAY) + " View asset ranking" + ConsoleUtils.color(" (资产排行)", ConsoleUtils.GRAY));
        System.out.println("  " + ConsoleUtils.color("4.", ConsoleUtils.GRAY) + " Exit system" + ConsoleUtils.color("        (退出系统)", ConsoleUtils.GRAY));
        System.out.println();
        System.out.println("  " + ConsoleUtils.color("Users loaded: " + userManager.getAllUsers().size(), ConsoleUtils.GRAY));
        System.out.println("  " + ConsoleUtils.color("────────────────────────────────────────────────────────", ConsoleUtils.GRAY));
        System.out.println();
    }
}
