package com.bank.gui;

import com.bank.exception.BankException;
import com.bank.model.BankAccount;
import com.bank.model.BankUser;
import com.bank.model.CreditAccount;
import com.bank.model.SavingsAccount;
import com.bank.service.BankUserManager;
import com.bank.util.PasswordUtils;

import javax.swing.*;
import java.awt.*;

/**
 * 申请新银行卡对话框。
 */
public class ApplyCardDialog extends JDialog {
    private final BankUserManager userManager;
    private final BankUser user;

    private final JComboBox<String> typeCombo = new JComboBox<>(new String[]{"储蓄卡", "信用卡"});
    private final JPasswordField pwdField = new JPasswordField();
    private final JPasswordField confirmField = new JPasswordField();
    private final JTextField balanceField = new JTextField();
    private final JTextField extraField = new JTextField();
    private final JLabel extraCaption = new JLabel("利率 (例如 0.02 表示 2%)");

    public ApplyCardDialog(JFrame owner, BankUserManager userManager, BankUser user) {
        super(owner, "申请新卡", true);
        this.userManager = userManager;
        this.user = user;

        JPanel root = new JPanel();
        root.setBackground(UiUtils.BG);
        root.setLayout(new BoxLayout(root, BoxLayout.Y_AXIS));
        root.setBorder(BorderFactory.createEmptyBorder(32, 40, 28, 40));

        JLabel title = new JLabel("申请新卡");
        title.setFont(UiUtils.displayFont(28));
        title.setForeground(UiUtils.TEXT);
        title.setAlignmentX(Component.LEFT_ALIGNMENT);
        root.add(title);

        JLabel sub = new JLabel("选择卡片类型并完成设置");
        sub.setFont(UiUtils.bodyFont(14));
        sub.setForeground(UiUtils.TEXT_SECONDARY);
        sub.setBorder(BorderFactory.createEmptyBorder(6, 0, 24, 0));
        sub.setAlignmentX(Component.LEFT_ALIGNMENT);
        root.add(sub);

        addRow(root, "卡片类型", typeCombo);
        addRow(root, "银行卡密码 (6位数字)", pwdField);
        addRow(root, "确认密码", confirmField);
        addRow(root, "初始存入余额", balanceField);

        JPanel extraRow = new JPanel();
        extraRow.setOpaque(false);
        extraRow.setLayout(new BoxLayout(extraRow, BoxLayout.Y_AXIS));
        extraCaption.setFont(UiUtils.labelFont());
        extraCaption.setForeground(UiUtils.TEXT_SECONDARY);
        extraCaption.setAlignmentX(Component.LEFT_ALIGNMENT);
        extraRow.add(extraCaption);
        extraRow.add(Box.createVerticalStrut(6));
        UiUtils.styleField(extraField);
        extraField.setAlignmentX(Component.LEFT_ALIGNMENT);
        extraField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 46));
        extraRow.add(extraField);
        extraRow.setAlignmentX(Component.LEFT_ALIGNMENT);
        extraRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 76));
        root.add(extraRow);
        root.add(Box.createVerticalStrut(14));

        typeCombo.addActionListener(e -> {
            boolean savings = typeCombo.getSelectedIndex() == 0;
            extraCaption.setText(savings ? "利率 (例如 0.02 表示 2%)" : "信用额度");
            extraField.setText("");
        });

        root.add(Box.createVerticalStrut(8));

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        buttons.setOpaque(false);
        buttons.setAlignmentX(Component.LEFT_ALIGNMENT);
        buttons.setMaximumSize(new Dimension(Integer.MAX_VALUE, 48));

        UiUtils.RoundedButton cancel = UiUtils.quietButton("取消");
        cancel.setPreferredSize(new Dimension(88, 40));
        cancel.addActionListener(e -> dispose());
        UiUtils.RoundedButton ok = UiUtils.primaryButton("确认开卡");
        ok.setPreferredSize(new Dimension(120, 40));
        ok.addActionListener(e -> doApply());
        buttons.add(cancel);
        buttons.add(ok);
        root.add(buttons);

        setContentPane(root);
        setSize(440, 560);
        setMinimumSize(new Dimension(420, 520));
        setLocationRelativeTo(owner);
    }

    private void addRow(JPanel root, String label, JComponent field) {
        JPanel row = UiUtils.formRow(label, field);
        row.setAlignmentX(Component.LEFT_ALIGNMENT);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 76));
        root.add(row);
        root.add(Box.createVerticalStrut(14));
    }

    private void doApply() {
        boolean savings = typeCombo.getSelectedIndex() == 0;
        String pwd = new String(pwdField.getPassword());
        String confirm = new String(confirmField.getPassword());
        try {
            PasswordUtils.validatePasswordConfirmation(pwd, confirm);
            double balance = Double.parseDouble(balanceField.getText().trim());
            BankAccount account;
            if (savings) {
                double rate = Double.parseDouble(extraField.getText().trim());
                account = new SavingsAccount(user, pwd, balance, rate);
            } else {
                double limit = Double.parseDouble(extraField.getText().trim());
                account = new CreditAccount(user, pwd, balance, limit);
            }
            user.addAccount(account);
            userManager.saveData();
            JOptionPane.showMessageDialog(this,
                    "开卡成功！\n卡号：" + account.getAccountNumber()
                            + "\n类型：" + UiUtils.cardTypeName(account)
                            + "\n初始余额：" + UiUtils.formatMoney(balance),
                    "开卡成功", JOptionPane.INFORMATION_MESSAGE);
            dispose();
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "请输入有效的数字金额 / 利率 / 额度。", "输入错误", JOptionPane.ERROR_MESSAGE);
        } catch (BankException e) {
            JOptionPane.showMessageDialog(this, "开卡失败：" + e.getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
        }
    }
}
