package com.bank.gui;

import com.bank.exception.BankException;
import com.bank.model.CreditAccount;
import com.bank.service.BankUserManager;

import javax.swing.*;
import java.awt.*;

/**
 * 信用卡管理对话框。
 */
public class CreditFeaturesDialog extends JDialog {
    private final BankUserManager userManager;
    private final CreditAccount account;
    private final JLabel usedValue = new JLabel();
    private final JLabel balanceValue = new JLabel();

    public CreditFeaturesDialog(JDialog owner, BankUserManager userManager, CreditAccount account) {
        super(owner, "信用卡管理", true);
        this.userManager = userManager;
        this.account = account;

        JPanel root = new JPanel();
        root.setBackground(UiUtils.BG);
        root.setLayout(new BoxLayout(root, BoxLayout.Y_AXIS));
        root.setBorder(BorderFactory.createEmptyBorder(32, 36, 28, 36));

        JLabel title = new JLabel("信用卡管理");
        title.setFont(UiUtils.displayFont(26));
        title.setForeground(UiUtils.TEXT);
        title.setAlignmentX(Component.LEFT_ALIGNMENT);
        root.add(title);

        JLabel sub = new JLabel("支付、还款与汇率折算");
        sub.setFont(UiUtils.bodyFont(14));
        sub.setForeground(UiUtils.TEXT_SECONDARY);
        sub.setBorder(BorderFactory.createEmptyBorder(6, 0, 24, 0));
        sub.setAlignmentX(Component.LEFT_ALIGNMENT);
        root.add(sub);

        UiUtils.RoundedPanel info = UiUtils.card();
        info.setLayout(new BoxLayout(info, BoxLayout.Y_AXIS));
        info.setBorder(BorderFactory.createEmptyBorder(8, 24, 8, 24));
        info.setAlignmentX(Component.LEFT_ALIGNMENT);
        info.setMaximumSize(new Dimension(Integer.MAX_VALUE, 120));

        usedValue.setFont(UiUtils.bodyFont(14));
        usedValue.setForeground(UiUtils.TEXT);
        usedValue.setHorizontalAlignment(SwingConstants.RIGHT);
        balanceValue.setFont(UiUtils.bodyFont(14));
        balanceValue.setForeground(UiUtils.TEXT);
        balanceValue.setHorizontalAlignment(SwingConstants.RIGHT);
        updateUsed();

        info.add(metricRow("已用额度", usedValue));
        JSeparator sep = new JSeparator();
        sep.setForeground(UiUtils.DIVIDER);
        sep.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));
        info.add(sep);
        info.add(metricRow("卡内余额", balanceValue));
        root.add(info);
        root.add(Box.createVerticalStrut(24));

        JPanel buttons = new JPanel(new GridLayout(0, 1, 0, 12));
        buttons.setOpaque(false);
        buttons.setAlignmentX(Component.LEFT_ALIGNMENT);
        buttons.setMaximumSize(new Dimension(Integer.MAX_VALUE, 180));
        addAction(buttons, "线上模拟支付", true, this::onlinePay);
        addAction(buttons, "还款", false, this::repay);
        addAction(buttons, "余额折算美元", false, this::convertUsd);
        root.add(buttons);
        root.add(Box.createVerticalStrut(20));

        UiUtils.RoundedButton close = UiUtils.quietButton("关闭");
        close.setAlignmentX(Component.CENTER_ALIGNMENT);
        close.setPreferredSize(new Dimension(120, 36));
        close.setMaximumSize(new Dimension(120, 36));
        close.addActionListener(e -> dispose());
        root.add(close);

        setContentPane(root);
        pack();
        setMinimumSize(new Dimension(400, getHeight()));
        setLocationRelativeTo(owner);
    }

    private JPanel metricRow(String key, JLabel value) {
        JPanel row = new JPanel(new BorderLayout());
        row.setOpaque(false);
        row.setBorder(BorderFactory.createEmptyBorder(12, 0, 12, 0));
        JLabel k = new JLabel(key);
        k.setFont(UiUtils.bodyFont(14));
        k.setForeground(UiUtils.TEXT_SECONDARY);
        row.add(k, BorderLayout.WEST);
        row.add(value, BorderLayout.EAST);
        return row;
    }

    private void addAction(JPanel buttons, String text, boolean primary, Runnable action) {
        UiUtils.RoundedButton b = primary ? UiUtils.primaryButton(text) : UiUtils.secondaryButton(text);
        b.setPreferredSize(new Dimension(0, 44));
        b.addActionListener(e -> action.run());
        buttons.add(b);
    }

    private String promptSecret(String message) {
        JPasswordField pf = new JPasswordField();
        pf.setColumns(14);
        UiUtils.styleField(pf);
        int r = JOptionPane.showConfirmDialog(this, new Object[]{message, pf}, "安全验证",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (r != JOptionPane.OK_OPTION) {
            return null;
        }
        return new String(pf.getPassword());
    }

    private Double promptAmount(String message) {
        String s = JOptionPane.showInputDialog(this, message);
        if (s == null) {
            return null;
        }
        try {
            return Double.parseDouble(s.trim());
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "请输入有效的数字金额。", "输入错误", JOptionPane.ERROR_MESSAGE);
            return null;
        }
    }

    private void updateUsed() {
        usedValue.setText(UiUtils.formatMoney(account.getUsedCredit()));
        balanceValue.setText(UiUtils.formatMoney(account.getBalance()));
    }

    private void onlinePay() {
        String pwd = promptSecret("请输入信用卡密码");
        if (pwd == null) {
            return;
        }
        Double amount = promptAmount("请输入在线支付交易金额");
        if (amount == null) {
            return;
        }
        try {
            account.payOnline(pwd, amount);
            userManager.saveData();
            updateUsed();
            JOptionPane.showMessageDialog(this,
                    "线上支付扣款成功！\n消费金额：" + UiUtils.formatMoney(amount)
                            + "\n卡内余额：" + UiUtils.formatMoney(account.getBalance()),
                    "操作成功", JOptionPane.INFORMATION_MESSAGE);
        } catch (BankException e) {
            JOptionPane.showMessageDialog(this, "支付中断：" + e.getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void repay() {
        Double amount = promptAmount("请输入还款金额");
        if (amount == null) {
            return;
        }
        String pwd = promptSecret("请输入信用卡密码");
        if (pwd == null) {
            return;
        }
        try {
            account.repay(amount, pwd);
            userManager.saveData();
            updateUsed();
            JOptionPane.showMessageDialog(this,
                    "还款成功！\n还款入账：" + UiUtils.formatMoney(amount)
                            + "\n当前余额：" + UiUtils.formatMoney(account.getBalance())
                            + "\n当前已用额度：" + UiUtils.formatMoney(account.getUsedCredit()),
                    "操作成功", JOptionPane.INFORMATION_MESSAGE);
        } catch (BankException e) {
            JOptionPane.showMessageDialog(this, "还款中断：" + e.getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void convertUsd() {
        JOptionPane.showMessageDialog(this,
                "美元余额：" + String.format("%,.2f USD", account.convertToUSD()) + "\n汇率：7.24",
                "汇率折算", JOptionPane.INFORMATION_MESSAGE);
    }
}
