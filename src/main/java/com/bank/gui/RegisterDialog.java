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

        // 顶层用 BorderLayout：表单区可滚动，按钮区始终可见
        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(UiUtils.BG);

        // ---- 表单区（CENTER，可滚动） ----
        JPanel form = new JPanel();
        form.setBackground(UiUtils.BG);
        form.setLayout(new BoxLayout(form, BoxLayout.Y_AXIS));
        form.setBorder(BorderFactory.createEmptyBorder(28, 40, 12, 40));

        JLabel title = new JLabel("创建账户");
        title.setFont(UiUtils.displayFont(28));
        title.setForeground(UiUtils.TEXT);
        title.setAlignmentX(Component.LEFT_ALIGNMENT);
        form.add(title);

        JLabel sub = new JLabel("填写以下信息完成注册");
        sub.setFont(UiUtils.bodyFont(14));
        sub.setForeground(UiUtils.TEXT_SECONDARY);
        sub.setBorder(BorderFactory.createEmptyBorder(6, 0, 20, 0));
        sub.setAlignmentX(Component.LEFT_ALIGNMENT);
        form.add(sub);

        addRow(form, "用户名", usernameField);
        addRow(form, "生日 (yyyy-MM-dd)", birthdayField);
        addRow(form, "手机号", phoneField);
        addRow(form, "邮箱", emailField);
        addRow(form, "登录密码 (6位数字)", passwordField);
        addRow(form, "确认密码", confirmField);

        JScrollPane scroll = new JScrollPane(form);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        scroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scroll.getViewport().setBackground(UiUtils.BG);
        root.add(scroll, BorderLayout.CENTER);

        // ---- 按钮区（SOUTH，始终可见） ----
        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        buttons.setBackground(UiUtils.BG);
        buttons.setBorder(BorderFactory.createEmptyBorder(8, 40, 16, 40));

        UiUtils.RoundedButton cancel = UiUtils.quietButton("取消");
        cancel.setPreferredSize(new Dimension(88, 40));
        cancel.addActionListener(e -> dispose());
        UiUtils.RoundedButton ok = UiUtils.primaryButton("注册");
        ok.setPreferredSize(new Dimension(100, 40));
        ok.addActionListener(e -> doRegister());
        buttons.add(cancel);
        buttons.add(ok);
        root.add(buttons, BorderLayout.SOUTH);

        setContentPane(root);
        setSize(440, 640);
        setMinimumSize(new Dimension(420, 560));
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
