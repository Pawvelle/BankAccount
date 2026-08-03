package com.bank.gui;

import com.bank.exception.BankException;
import com.bank.model.BankUser;
import com.bank.service.BankUserManager;

import javax.swing.*;
import java.awt.*;

/**
 * 修改个人资料对话框。
 */
public class EditProfileDialog extends JDialog {
    private final BankUserManager userManager;
    private final BankUser user;

    private final JTextField usernameField = new JTextField();
    private final JTextField birthdayField = new JTextField();
    private final JTextField phoneField = new JTextField();
    private final JTextField emailField = new JTextField();
    private final JPasswordField oldPwdField = new JPasswordField();
    private final JPasswordField newPwdField = new JPasswordField();
    private final JPasswordField confirmPwdField = new JPasswordField();

    public EditProfileDialog(JFrame owner, BankUserManager userManager, BankUser user) {
        super(owner, "个人资料", true);
        this.userManager = userManager;
        this.user = user;

        usernameField.setText(user.getUsername());
        birthdayField.setText(user.getBirthday());
        phoneField.setText(user.getPhone());
        emailField.setText(user.getEmail());

        // 顶层 BorderLayout：表单可滚动，按钮始终可见
        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(UiUtils.BG);

        JPanel form = new JPanel();
        form.setBackground(UiUtils.BG);
        form.setLayout(new BoxLayout(form, BoxLayout.Y_AXIS));
        form.setBorder(BorderFactory.createEmptyBorder(28, 40, 12, 40));

        JLabel title = new JLabel("个人资料");
        title.setFont(UiUtils.displayFont(28));
        title.setForeground(UiUtils.TEXT);
        title.setAlignmentX(Component.LEFT_ALIGNMENT);
        form.add(title);

        JLabel sub = new JLabel("更新你的账户信息");
        sub.setFont(UiUtils.bodyFont(14));
        sub.setForeground(UiUtils.TEXT_SECONDARY);
        sub.setBorder(BorderFactory.createEmptyBorder(6, 0, 20, 0));
        sub.setAlignmentX(Component.LEFT_ALIGNMENT);
        form.add(sub);

        addRow(form, "用户名", usernameField);
        addRow(form, "生日 (yyyy-MM-dd)", birthdayField);
        addRow(form, "手机号", phoneField);
        addRow(form, "邮箱", emailField);

        JLabel sep = new JLabel("修改密码（选填）");
        sep.setFont(UiUtils.captionFont(12));
        sep.setForeground(UiUtils.TEXT_SECONDARY);
        sep.setBorder(BorderFactory.createEmptyBorder(8, 0, 10, 0));
        sep.setAlignmentX(Component.LEFT_ALIGNMENT);
        form.add(sep);

        addRow(form, "旧密码", oldPwdField);
        addRow(form, "新密码 (6位数字)", newPwdField);
        addRow(form, "确认新密码", confirmPwdField);

        JScrollPane scroll = new JScrollPane(form);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        scroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scroll.getViewport().setBackground(UiUtils.BG);
        root.add(scroll, BorderLayout.CENTER);

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        buttons.setBackground(UiUtils.BG);
        buttons.setBorder(BorderFactory.createEmptyBorder(8, 40, 16, 40));

        UiUtils.RoundedButton cancel = UiUtils.quietButton("取消");
        cancel.setPreferredSize(new Dimension(88, 40));
        cancel.addActionListener(e -> dispose());
        UiUtils.RoundedButton save = UiUtils.primaryButton("保存");
        save.setPreferredSize(new Dimension(100, 40));
        save.addActionListener(e -> doSave());
        buttons.add(cancel);
        buttons.add(save);
        root.add(buttons, BorderLayout.SOUTH);

        setContentPane(root);
        setSize(440, 680);
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

    private void doSave() {
        try {
            user.setUsername(usernameField.getText().trim());
            user.setBirthday(birthdayField.getText().trim());
            user.setPhone(phoneField.getText().trim());
            user.setEmail(emailField.getText().trim());

            String oldPwd = new String(oldPwdField.getPassword());
            String newPwd = new String(newPwdField.getPassword());
            String confirmPwd = new String(confirmPwdField.getPassword());
            boolean anyPwdFilled = !oldPwd.isEmpty() || !newPwd.isEmpty() || !confirmPwd.isEmpty();
            if (anyPwdFilled) {
                if (oldPwd.isEmpty() || newPwd.isEmpty() || confirmPwd.isEmpty()) {
                    JOptionPane.showMessageDialog(this,
                            "如要修改密码，旧密码、新密码和确认密码三个都必须填写。",
                            "输入错误", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                user.setNewAccountPassword(oldPwd, newPwd, confirmPwd);
            }

            userManager.saveData();
            JOptionPane.showMessageDialog(this, "个人资料修改成功！", "操作成功", JOptionPane.INFORMATION_MESSAGE);
            dispose();
        } catch (BankException e) {
            JOptionPane.showMessageDialog(this, "修改失败：" + e.getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
        }
    }
}
