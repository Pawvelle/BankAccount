package com.bank.gui;

import com.bank.model.BankUser;
import com.bank.service.BankUserManager;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

/**
 * 系统主窗口：使用 CardLayout 在「登录 / 用户中心 / 卡包管理」之间切换导航。
 */
public class MainFrame extends JFrame {
    private final BankUserManager userManager = new BankUserManager();
    private final CardLayout cardLayout = new CardLayout();
    private final JPanel root = new JPanel(cardLayout);

    private final LoginPanel loginPanel = new LoginPanel(this, userManager);
    private final UserCenterPanel userCenterPanel = new UserCenterPanel(this, userManager);
    private final WalletPanel walletPanel = new WalletPanel(this, userManager);

    public MainFrame() {
        super("Bank Account");
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setSize(1120, 760);
        setMinimumSize(new Dimension(980, 680));
        setLocationRelativeTo(null);

        root.setBackground(UiUtils.BG);
        root.add(loginPanel, "login");
        root.add(userCenterPanel, "user");
        root.add(walletPanel, "wallet");
        setContentPane(root);

        // 关闭窗口前保存数据
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                userManager.saveData();
            }
        });

        showLogin();
    }

    public BankUserManager getUserManager() {
        return userManager;
    }

    public void showLogin() {
        cardLayout.show(root, "login");
        setTitle("Bank Account");
    }

    public void showUserCenter(BankUser user) {
        userCenterPanel.refresh(user);
        cardLayout.show(root, "user");
        setTitle("Bank Account — " + user.getUsername());
    }

    public void showWallet(BankUser user) {
        walletPanel.refresh(user);
        cardLayout.show(root, "wallet");
        setTitle("卡包 — " + user.getUsername());
    }
}
