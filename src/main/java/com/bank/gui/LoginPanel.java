package com.bank.gui;

import com.bank.exception.BankException;
import com.bank.model.BankUser;
import com.bank.service.BankUserManager;

import javax.swing.*;
import java.awt.*;

/**
 * 登录面板：品牌英雄区 + 极简表单。
 */
public class LoginPanel extends UiUtils.AtmospherePanel {
    private final MainFrame frame;
    private final BankUserManager userManager;
    private final JTextField idField = new JTextField(18);
    private final JPasswordField passwordField = new JPasswordField(18);

    public LoginPanel(MainFrame frame, BankUserManager userManager) {
        this.frame = frame;
        this.userManager = userManager;

        JPanel center = new JPanel(new GridBagLayout());
        center.setOpaque(false);

        JPanel stack = new JPanel();
        stack.setOpaque(false);
        stack.setLayout(new BoxLayout(stack, BoxLayout.Y_AXIS));

        stack.add(buildBrand());
        stack.add(Box.createVerticalStrut(32));
        stack.add(buildForm());
        stack.add(Box.createVerticalStrut(24));
        stack.add(buildFooter());

        center.add(stack);
        add(center, BorderLayout.CENTER);
    }

    private JComponent buildBrand() {
        JPanel brand = new JPanel(new GridBagLayout());
        brand.setOpaque(false);
        brand.setAlignmentX(Component.CENTER_ALIGNMENT);

        GridBagConstraints g = new GridBagConstraints();
        g.gridx = 0;
        g.gridy = 0;

        JLabel name = new JLabel("Bank Account");
        name.setFont(UiUtils.displayFont(48));
        name.setForeground(UiUtils.TEXT);
        brand.add(name, g);

        JLabel tagline = new JLabel("简洁、安全的银行账户管理");
        tagline.setFont(UiUtils.bodyFont(17));
        tagline.setForeground(UiUtils.TEXT_SECONDARY);
        g.gridy = 1;
        g.insets = new Insets(10, 0, 0, 0);
        brand.add(tagline, g);

        return brand;
    }

    private JComponent buildForm() {
        UiUtils.RoundedPanel card = UiUtils.elevatedCard();
        card.setLayout(new BorderLayout());
        card.setAlignmentX(Component.CENTER_ALIGNMENT);

        JPanel form = new JPanel(new GridBagLayout());
        form.setOpaque(false);
        form.setBorder(BorderFactory.createEmptyBorder(36, 40, 32, 40));

        GridBagConstraints g = new GridBagConstraints();
        g.gridx = 0;
        g.fill = GridBagConstraints.HORIZONTAL;
        g.weightx = 1;

        JLabel welcome = new JLabel("登录");
        welcome.setFont(UiUtils.titleFont(24));
        welcome.setForeground(UiUtils.TEXT);
        g.gridy = 0;
        g.insets = new Insets(0, 0, 6, 0);
        form.add(welcome, g);

        JLabel hint = new JLabel("使用你的用户 ID 继续");
        hint.setFont(UiUtils.bodyFont(14));
        hint.setForeground(UiUtils.TEXT_SECONDARY);
        g.gridy = 1;
        g.insets = new Insets(0, 0, 24, 0);
        form.add(hint, g);

        JPanel idRow = UiUtils.formRow("用户 ID", idField);
        g.gridy = 2;
        g.insets = new Insets(0, 0, 14, 0);
        form.add(idRow, g);

        JPanel pwdRow = UiUtils.formRow("密码", passwordField);
        g.gridy = 3;
        g.insets = new Insets(0, 0, 24, 0);
        form.add(pwdRow, g);

        UiUtils.RoundedButton loginBtn = UiUtils.primaryButton("继续");
        loginBtn.setPreferredSize(new Dimension(320, 48));
        loginBtn.addActionListener(e -> doLogin());
        g.gridy = 4;
        g.insets = new Insets(0, 0, 16, 0);
        form.add(loginBtn, g);

        JPanel links = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 0));
        links.setOpaque(false);
        UiUtils.RoundedButton registerBtn = UiUtils.ghostButton("创建账户");
        registerBtn.addActionListener(e -> doRegister());
        UiUtils.RoundedButton rankBtn = UiUtils.quietButton("资产排行");
        rankBtn.addActionListener(e -> new RankingDialog(frame, userManager).setVisible(true));
        links.add(registerBtn);
        links.add(rankBtn);
        g.gridy = 5;
        g.insets = new Insets(0, 0, 0, 0);
        form.add(links, g);

        card.add(form, BorderLayout.CENTER);
        card.setPreferredSize(new Dimension(400, 420));

        JPanel wrap = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
        wrap.setOpaque(false);
        wrap.setAlignmentX(Component.CENTER_ALIGNMENT);
        wrap.add(card);
        return wrap;
    }

    private JComponent buildFooter() {
        JLabel label = new JLabel("数据本地加密保存 · SHA-256");
        label.setFont(UiUtils.captionFont(12));
        label.setForeground(UiUtils.TEXT_SECONDARY);

        JPanel footer = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
        footer.setOpaque(false);
        footer.setAlignmentX(Component.CENTER_ALIGNMENT);
        footer.add(label);
        return footer;
    }

    private void doLogin() {
        String id = idField.getText().trim();
        String password = new String(passwordField.getPassword());
        if (id.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this, "请输入用户ID和密码。", "提示", JOptionPane.WARNING_MESSAGE);
            return;
        }
        try {
            BankUser user = userManager.authenticate(id, password);
            passwordField.setText("");
            frame.showUserCenter(user);
        } catch (BankException e) {
            JOptionPane.showMessageDialog(this, e.getMessage(), "登录失败", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void doRegister() {
        RegisterDialog dialog = new RegisterDialog(frame, userManager);
        dialog.setVisible(true);
        BankUser created = dialog.getCreatedUser();
        if (created != null) {
            idField.setText(created.getId());
            passwordField.setText("");
        }
    }
}
