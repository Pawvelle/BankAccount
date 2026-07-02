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
            int choice = ConsoleUtils.readInt("请选择功能：");
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
                    userManager.saveData(); // 退出前保存数据
                    System.out.println("系统已退出，欢迎下次使用。");
                    break;
                default:
                    System.out.println("无效选项，请重新输入。");
                    break;
            }
        }
    }

    private void printWelcome() {
        System.out.println("==================================");
        System.out.println("     银行账户 CLI 操作系统");
        System.out.println("==================================");
    }

    private void printMainMenu() {
        System.out.println();
        System.out.println("========== 主菜单 ==========");
        System.out.println("1. 注册账户");
        System.out.println("2. 登录账户");
        System.out.println("3. 资产排行");
        System.out.println("4. 退出系统");
        System.out.println("当前系统用户数：" + userManager.getAllUsers().size());
        System.out.println("============================");
    }
}
