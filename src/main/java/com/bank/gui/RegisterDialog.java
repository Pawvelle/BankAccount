package com.bank.gui;

import com.bank.exception.BankException;
import com.bank.model.BankUser;
import com.bank.service.BankUserManager;
import com.bank.util.PasswordUtils;

import javax.swing.*;
import java.awt.*;

/**
 * 注册新账户对话框。
 */
public class RegisterDialog extends JDialog {
    private final BankUserManager userManager;
    private final JTextField usernameField = new JTextField();
    private final JTextField birthdayField = new JTextField();
    private final JTextField phoneField = new JTextField();
    private final JTextField emailField = new JTextField();
    private final JPasswordField passwordField = new JPasswordField();
    private final JPasswordField confirmField = new JPasswordField();

    private BankUser createdUser;

    public RegisterDialog(JFrame owner, BankUserManager userManager) {
        super(owner, "创建账户", true);
        this.userManager = userManager;

        JPanel root = new JPanel();
        root.setBackground(UiUtils.BG);
        root.setLayout(new BoxLayout(root, BoxLayout.Y_AXIS));
        root.setBorder(BorderFactory.createEmptyBorder(32, 40, 28, 40));

        JLabel title = new JLabel("创建账户");
        title.setFont(UiUtils.displayFont(28));
        title.setForeground(UiUtils.TEXT);
        title.setAlignmentX(Component.LEFT_ALIGNMENT);
        root.add(title);

        JLabel sub = new JLabel("填写以下信息完成注册");
        sub.setFont(UiUtils.bodyFont(14));
        sub.setForeground(UiUtils.TEXT_SECONDARY);
        sub.setBorder(BorderFactory.createEmptyBorder(6, 0, 24, 0));
        sub.setAlignmentX(Component.LEFT_ALIGNMENT);
        root.add(sub);

        addRow(root, "用户名", usernameField);
        addRow(root, "生日 (yyyy-MM-dd)", birthdayField);
        addRow(root, "手机号", phoneField);
        addRow(root, "邮箱", emailField);
        addRow(root, "登录密码 (6位数字)", passwordField);
        addRow(root, "确认密码", confirmField);

        root.add(Box.createVerticalStrut(12));

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        buttons.setOpaque(false);
        buttons.setAlignmentX(Component.LEFT_ALIGNMENT);
        buttons.setMaximumSize(new Dimension(Integer.MAX_VALUE, 48));

        UiUtils.RoundedButton cancel = UiUtils.quietButton("取消");
        cancel.setPreferredSize(new Dimension(88, 40));
        cancel.addActionListener(e -> dispose());
        UiUtils.RoundedButton ok = UiUtils.primaryButton("注册");
        ok.setPreferredSize(new Dimension(100, 40));
        ok.addActionListener(e -> doRegister());
        buttons.add(cancel);
        buttons.add(ok);
        root.add(buttons);

        setContentPane(root);
        setSize(440, 620);
        setMinimumSize(new Dimension(420, 580));
        setLocationRelativeTo(owner);
    }

    private void addRow(JPanel root, String label, JComponent field) {
        JPanel row = UiUtils.formRow(label, field);
        row.setAlignmentX(Component.LEFT_ALIGNMENT);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 76));
        root.add(row);
        root.add(Box.createVerticalStrut(14));
    }

    private void doRegister() {
        String username = usernameField.getText().trim();
        String birthday = birthdayField.getText().trim();
        String phone = phoneField.getText().trim();
        String email = emailField.getText().trim();
        String password = new String(passwordField.getPassword());
        String confirm = new String(confirmField.getPassword());

        try {
            PasswordUtils.validatePasswordConfirmation(password, confirm);
            BankUser user = new BankUser(username, birthday, phone, email, password);
            userManager.addUser(user);
            this.createdUser = user;
            JOptionPane.showMessageDialog(this,
                    "注册成功！\n用户ID：" + user.getId() + "\n用户名：" + user.getUsername(),
                    "注册成功", JOptionPane.INFORMATION_MESSAGE);
            dispose();
        } catch (BankException e) {
            JOptionPane.showMessageDialog(this, "注册失败：" + e.getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
        }
    }

    public BankUser getCreatedUser() {
        return createdUser;
    }
}
