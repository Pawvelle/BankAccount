package com.bank.ui;

import com.bank.exception.AccountLockedException;
import com.bank.exception.BankException;
import com.bank.exception.InvalidPasswordException;
import com.bank.model.BankUser;
import com.bank.service.BankUserManager;

import java.util.List;

public class UserMenuUI {
    private final BankUserManager userManager;
    private final CardMenuUI cardMenuUI;

    public UserMenuUI(BankUserManager userManager, CardMenuUI cardMenuUI) {
        this.userManager = userManager;
        this.cardMenuUI = cardMenuUI;
    }

    public void registerUser() {
        System.out.println();
        System.out.println("========== 注册账户 ==========");
        try {
            String username = ConsoleUtils.readRequiredText("请输入用户名：");
            String birthday = ConsoleUtils.readRequiredText("请输入生日（yyyy-MM-dd）：");
            String phone = ConsoleUtils.readRequiredText("请输入手机号：");
            String email = ConsoleUtils.readRequiredText("请输入邮箱：");
            String password = ConsoleUtils.readPasswordWithConfirmation("请输入登录密码（6位数字）：", "请再次输入登录密码：");

            BankUser user = new BankUser(username, birthday, phone, email, password);
            userManager.addUser(user);
            System.out.println();
            System.out.println("========== 注册成功 ==========");
            System.out.println("你的用户ID是：" + user.getId());
            System.out.println("用户名：" + user.getUsername());
            ConsoleUtils.waitForEnter("按回车键返回主菜单...");
        } catch (BankException e) {
            System.out.println("注册失败：" + e.getMessage());
            ConsoleUtils.waitForEnter("按回车键返回主菜单...");
        } catch (Exception e) {
            System.out.println("注册失败：输入系统异常（" + e.getMessage() + "）");
            ConsoleUtils.waitForEnter("按回车键返回主菜单...");
        }
    }

    public void loginUser() {
        System.out.println();
        System.out.println("========== 登录账户 ==========");
        String id = ConsoleUtils.readRequiredText("请输入用户ID：");
        String password = ConsoleUtils.readRequiredText("请输入登录密码：");

        try {
            BankUser user = userManager.authenticate(id, password);
            System.out.println("登录成功，欢迎你，" + user.getUsername() + "。");
            userCenter(user);
        } catch (AccountLockedException e) {
            System.out.println("\n[安全拦截] " + e.getMessage());
            ConsoleUtils.waitForEnter("按回车键返回主菜单...");
        } catch (InvalidPasswordException e) {
            System.out.println("\n[登录失败] " + e.getMessage());
            ConsoleUtils.waitForEnter("按回车键返回主菜单...");
        } catch (BankException e) {
            System.out.println("\n[登录失败] " + e.getMessage());
            ConsoleUtils.waitForEnter("按回车键返回主菜单...");
        }
    }

    public void showAssetRanking() {
        System.out.println();
        System.out.println("========== 资产排行 ==========");
        List<BankUser> ranking = userManager.getRankingByAssets();
        if (ranking.isEmpty()) {
            System.out.println("当前还没有用户数据。");
            ConsoleUtils.waitForEnter("按回车键返回主菜单...");
            return;
        }

        for (int i = 0; i < ranking.size(); i++) {
            BankUser user = ranking.get(i);
            System.out.println((i + 1) + ". " + user.getUsername() + "（" + user.getId() + "）" +
                    (user.isLocked() ? " [已锁定]" : ""));
            System.out.println("   总资产：" + ConsoleUtils.formatMoney(user.calculateTotalWealth()));
        }
        ConsoleUtils.waitForEnter("按回车键返回主菜单...");
    }

    private void userCenter(BankUser user) {
        boolean loggedIn = true;
        while (loggedIn) {
            System.out.println();
            System.out.println("====== 用户中心：" + user.getUsername() + " ======");
            System.out.println("用户ID：" + user.getId());
            System.out.println("银行卡数量：" + user.getMyAccounts().size() + " 张");
            System.out.println("当前总资产：" + ConsoleUtils.formatMoney(user.calculateTotalWealth()));
            System.out.println("1. 查看账户信息");
            System.out.println("2. 卡包管理");
            System.out.println("3. 修改基础信息");
            System.out.println("4. 退出登录");
            System.out.println("=================================");

            int choice = ConsoleUtils.readInt("请选择功能：");
            switch (choice) {
                case 1:
                    showUserInfo(user);
                    break;
                case 2:
                    cardMenuUI.walletMenu(user);
                    break;
                case 3:
                    editBasicInfo(user);
                    break;
                case 4:
                    loggedIn = false;
                    userManager.saveData();
                    System.out.println("你已退出当前账户。");
                    break;
                default:
                    System.out.println("无效选项，请重新输入。");
                    break;
            }
        }
    }

    private void showUserInfo(BankUser user) {
        System.out.println();
        System.out.println("========== 账户信息 ==========");
        System.out.println(user.getUserInfo());
        System.out.println("登录密码：****** (SHA-256 哈希安全保护)");
        System.out.println("账户总资产：" + ConsoleUtils.formatMoney(user.calculateTotalWealth()));
        System.out.println("------------------------------");
        cardMenuUI.printUserCards(user, false);
        ConsoleUtils.waitForEnter("按回车键返回用户中心...");
    }

    private void editBasicInfo(BankUser user) {
        boolean editing = true;
        while (editing) {
            System.out.println();
            System.out.println("========== 修改基础信息 ==========");
            System.out.println("1. 修改用户名");
            System.out.println("2. 修改生日");
            System.out.println("3. 修改手机号");
            System.out.println("4. 修改邮箱");
            System.out.println("5. 修改登录密码");
            System.out.println("6. 返回上一级");
            System.out.println("==================================");

            int choice = ConsoleUtils.readInt("请选择功能：");
            try {
                switch (choice) {
                    case 1:
                        user.setUsername(ConsoleUtils.readRequiredText("请输入新用户名："));
                        userManager.saveData();
                        ConsoleUtils.showProfileUpdateResult("用户名已更新。", user);
                        break;
                    case 2:
                        user.setBirthday(ConsoleUtils.readRequiredText("请输入新生日（yyyy-MM-dd）："));
                        userManager.saveData();
                        ConsoleUtils.showProfileUpdateResult("生日已更新。", user);
                        break;
                    case 3:
                        user.setPhone(ConsoleUtils.readRequiredText("请输入新手机号："));
                        userManager.saveData();
                        ConsoleUtils.showProfileUpdateResult("手机号已更新。", user);
                        break;
                    case 4:
                        user.setEmail(ConsoleUtils.readRequiredText("请输入新邮箱："));
                        userManager.saveData();
                        ConsoleUtils.showProfileUpdateResult("邮箱已更新。", user);
                        break;
                    case 5:
                        String oldPassword = ConsoleUtils.readRequiredText("请输入旧登录密码：");
                        String newPassword = ConsoleUtils.readRequiredText("请输入新登录密码：");
                        String confirmPassword = ConsoleUtils.readRequiredText("请再次输入新登录密码：");
                        boolean success = user.setNewAccountPassword(oldPassword, newPassword, confirmPassword);
                        if (success) {
                            userManager.saveData();
                            ConsoleUtils.showOperationResult("登录密码修改成功。", "用户ID：" + user.getId(),
                                    "按回车键返回修改菜单...");
                        }
                        break;
                    case 6:
                        editing = false;
                        userManager.saveData();
                        break;
                    default:
                        System.out.println("无效选项，请重新输入。");
                        break;
                }
            } catch (BankException e) {
                System.out.println("修改失败：" + e.getMessage());
                ConsoleUtils.waitForEnter("按回车键继续...");
            }
        }
    }
}
