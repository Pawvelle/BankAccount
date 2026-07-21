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
        System.out.println("  " + ConsoleUtils.bold("Register Account"));
        System.out.println("  " + ConsoleUtils.color("────────────────────────────────────────────────────────", ConsoleUtils.GRAY));
        try {
            String username = ConsoleUtils.readRequiredText("请输入用户名");
            String birthday = ConsoleUtils.readRequiredText("请输入生日 (yyyy-MM-dd)");
            String phone = ConsoleUtils.readRequiredText("请输入手机号");
            String email = ConsoleUtils.readRequiredText("请输入邮箱");
            String password = ConsoleUtils.readPasswordWithConfirmation("请输入登录密码 (6位数字)", "请再次输入登录密码");

            BankUser user = new BankUser(username, birthday, phone, email, password);
            ConsoleUtils.showSpinner("Creating user credentials database...", 800);
            userManager.addUser(user);

            System.out.println();
            System.out.println("  " + ConsoleUtils.ICON_SUCCESS + " " + ConsoleUtils.bold("Account created successfully"));
            System.out.println("    User ID:   " + ConsoleUtils.color(user.getId(), ConsoleUtils.CYAN));
            System.out.println("    Username:  " + user.getUsername());
            System.out.println();

            ConsoleUtils.waitForEnter("按回车键返回主菜单...");
        } catch (BankException e) {
            ConsoleUtils.showError("注册失败：" + e.getMessage());
            ConsoleUtils.waitForEnter("按回车键返回主菜单...");
        } catch (Exception e) {
            ConsoleUtils.showError("注册失败：输入系统异常（" + e.getMessage() + "）");
            ConsoleUtils.waitForEnter("按回车键返回主菜单...");
        }
    }

    public void loginUser() {
        System.out.println();
        System.out.println("  " + ConsoleUtils.bold("Login Session"));
        System.out.println("  " + ConsoleUtils.color("────────────────────────────────────────────────────────", ConsoleUtils.GRAY));
        String id = ConsoleUtils.readRequiredText("请输入用户ID");
        String password = ConsoleUtils.readRequiredText("请输入登录密码");

        try {
            ConsoleUtils.showSpinner("Authenticating security credentials...", 700);
            BankUser user = userManager.authenticate(id, password);
            ConsoleUtils.showSuccess("登录成功，当前会话已激活。");
            userCenter(user);
        } catch (AccountLockedException e) {
            ConsoleUtils.showError("[安全拦截] " + e.getMessage());
            ConsoleUtils.waitForEnter("按回车键返回主菜单...");
        } catch (InvalidPasswordException e) {
            ConsoleUtils.showError("[登录失败] " + e.getMessage());
            ConsoleUtils.waitForEnter("按回车键返回主菜单...");
        } catch (BankException e) {
            ConsoleUtils.showError("[登录失败] " + e.getMessage());
            ConsoleUtils.waitForEnter("按回车键返回主菜单...");
        }
    }

    public void showAssetRanking() {
        System.out.println();
        System.out.println("  " + ConsoleUtils.bold("Asset Wealth Ranking"));
        System.out.println("  " + ConsoleUtils.color("────────────────────────────────────────────────────────", ConsoleUtils.GRAY));
        List<BankUser> ranking = userManager.getRankingByAssets();
        if (ranking.isEmpty()) {
            ConsoleUtils.showInfo("当前系统内还没有任何用户数据。");
            ConsoleUtils.waitForEnter("按回车键返回主菜单...");
            return;
        }

        for (int i = 0; i < ranking.size(); i++) {
            BankUser user = ranking.get(i);
            String lockedTag = user.isLocked() ? ConsoleUtils.color(" [Locked]", ConsoleUtils.RED) : "";

            System.out.println("  " + (i + 1) + ". " + ConsoleUtils.bold(user.getUsername()) +
                    " (" + user.getId() + ")" + lockedTag);
            System.out.println("     Total Assets: " + ConsoleUtils.formatMoney(user.calculateTotalWealth()));
        }
        System.out.println("  " + ConsoleUtils.color("────────────────────────────────────────────────────────", ConsoleUtils.GRAY));
        ConsoleUtils.waitForEnter("按回车键返回主菜单...");
    }

    private void userCenter(BankUser user) {
        boolean loggedIn = true;
        while (loggedIn) {
            System.out.println();
            System.out.println("  " + ConsoleUtils.bold("User Profile") + ConsoleUtils.color(" (" + user.getId() + ")", ConsoleUtils.GRAY));
            System.out.println("  " + ConsoleUtils.color("────────────────────────────────────────────────────────", ConsoleUtils.GRAY));
            System.out.println("    Name:         " + user.getUsername());
            System.out.println("    Active Cards: " + user.getMyAccounts().size());
            System.out.println("    Total Assets: " + ConsoleUtils.formatMoney(user.calculateTotalWealth()));
            System.out.println("  " + ConsoleUtils.color("────────────────────────────────────────────────────────", ConsoleUtils.GRAY));
            System.out.println("    1. Show basic profile info" + ConsoleUtils.color("     (查看基本信息)", ConsoleUtils.GRAY));
            System.out.println("    2. Manage bank card package" + ConsoleUtils.color("    (卡包管理)", ConsoleUtils.GRAY));
            System.out.println("    3. Edit personal configuration" + ConsoleUtils.color(" (修改个人资料)", ConsoleUtils.GRAY));
            System.out.println("    4. Log out active session" + ConsoleUtils.color("      (退出登录)", ConsoleUtils.GRAY));
            System.out.println();

            int choice = ConsoleUtils.readInt("请选择操作");
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
                    ConsoleUtils.showSpinner("Saving user status to disk...", 500);
                    userManager.saveData();
                    ConsoleUtils.showSuccess("已安全退出当前会话。");
                    break;
                default:
                    ConsoleUtils.showError("无效选项，请重新选择。");
                    break;
            }
        }
    }

    private void showUserInfo(BankUser user) {
        System.out.println();
        System.out.println("  " + ConsoleUtils.bold("Profile Archive") + ConsoleUtils.color(" (" + user.getId() + ")", ConsoleUtils.GRAY));
        System.out.println("  " + ConsoleUtils.color("────────────────────────────────────────────────────────", ConsoleUtils.GRAY));
        String[] lines = user.getUserInfo().split("\n");
        for (String line : lines) {
            System.out.println("    " + line);
        }
        System.out.println("    Security:     " + ConsoleUtils.color("SHA-256 password hash verified", ConsoleUtils.GRAY));
        System.out.println("    Total Wealth: " + ConsoleUtils.formatMoney(user.calculateTotalWealth()));
        System.out.println("  " + ConsoleUtils.color("────────────────────────────────────────────────────────", ConsoleUtils.GRAY));
        System.out.println();
        cardMenuUI.printUserCards(user, false);
        System.out.println();
        ConsoleUtils.waitForEnter("按回车键返回用户中心...");
    }

    private void editBasicInfo(BankUser user) {
        boolean editing = true;
        while (editing) {
            System.out.println();
            System.out.println("  " + ConsoleUtils.bold("Edit Profile Settings"));
            System.out.println("  " + ConsoleUtils.color("────────────────────────────────────────────────────────", ConsoleUtils.GRAY));
            System.out.println("  " + ConsoleUtils.color("1.", ConsoleUtils.GRAY) + " Update username");
            System.out.println("  " + ConsoleUtils.color("2.", ConsoleUtils.GRAY) + " Update birthday");
            System.out.println("  " + ConsoleUtils.color("3.", ConsoleUtils.GRAY) + " Update phone number");
            System.out.println("  " + ConsoleUtils.color("4.", ConsoleUtils.GRAY) + " Update email address");
            System.out.println("  " + ConsoleUtils.color("5.", ConsoleUtils.GRAY) + " Change login password");
            System.out.println("  " + ConsoleUtils.color("6.", ConsoleUtils.GRAY) + " Return to profile");
            System.out.println("  " + ConsoleUtils.color("────────────────────────────────────────────────────────", ConsoleUtils.GRAY));
            System.out.println();

            int choice = ConsoleUtils.readInt("请选择操作");
            try {
                switch (choice) {
                    case 1:
                        user.setUsername(ConsoleUtils.readRequiredText("请输入新用户名"));
                        ConsoleUtils.showSpinner("Updating profile database...", 500);
                        userManager.saveData();
                        ConsoleUtils.showProfileUpdateResult("用户名已成功更新！", user);
                        break;
                    case 2:
                        user.setBirthday(ConsoleUtils.readRequiredText("请输入新生日 (yyyy-MM-dd)"));
                        ConsoleUtils.showSpinner("Updating profile database...", 500);
                        userManager.saveData();
                        ConsoleUtils.showProfileUpdateResult("生日已成功更新！", user);
                        break;
                    case 3:
                        user.setPhone(ConsoleUtils.readRequiredText("请输入新手机号"));
                        ConsoleUtils.showSpinner("Updating profile database...", 500);
                        userManager.saveData();
                        ConsoleUtils.showProfileUpdateResult("手机号已成功更新！", user);
                        break;
                    case 4:
                        user.setEmail(ConsoleUtils.readRequiredText("请输入新邮箱"));
                        ConsoleUtils.showSpinner("Updating profile database...", 500);
                        userManager.saveData();
                        ConsoleUtils.showProfileUpdateResult("邮箱已成功更新！", user);
                        break;
                    case 5:
                        String oldPassword = ConsoleUtils.readRequiredText("请输入旧登录密码");
                        String newPassword = ConsoleUtils.readRequiredText("请输入新登录密码 (6位数字)");
                        String confirmPassword = ConsoleUtils.readRequiredText("请再次输入新登录密码");
                        ConsoleUtils.showSpinner("Resetting password security encryption...", 800);
                        boolean success = user.setNewAccountPassword(oldPassword, newPassword, confirmPassword);
                        if (success) {
                            userManager.saveData();
                            ConsoleUtils.showOperationResult("登录密码修改成功。", "用户ID：" + user.getId() + "\n安全级别：高",
                                    "按回车键返回修改菜单...");
                        }
                        break;
                    case 6:
                        editing = false;
                        userManager.saveData();
                        break;
                    default:
                        ConsoleUtils.showError("无效选项，请重新选择。");
                        break;
                }
            } catch (BankException e) {
                ConsoleUtils.showError("修改失败：" + e.getMessage());
                ConsoleUtils.waitForEnter("按回车键继续...");
            }
        }
    }
}
